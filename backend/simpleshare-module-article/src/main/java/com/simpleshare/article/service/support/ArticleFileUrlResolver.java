package com.simpleshare.article.service.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleshare.article.domain.Article;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.storage.config.CloudStorageConfig;
import com.simpleshare.framework.storage.config.LocalStorageConfig;
import com.simpleshare.framework.storage.config.StorageProfile;
import com.simpleshare.infra.domain.InfraFileConfig;
import com.simpleshare.infra.service.IInfraFileConfigService;
import com.simpleshare.system.service.ITenantStorageService;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes file URLs inside article entities so they always point to the current storage domain.
 */
@Component
public class ArticleFileUrlResolver {

    private static final Pattern RESOURCE_ATTR_PATTERN = Pattern.compile(
            "(?i)(?<attr>\\b(?:srcset|src|href|poster|data-src|data-original|data-url)\\b)(?<eq>\\s*=\\s*)(?<quote>['\"])(?<value>[^\"']*)(?<suffix>['\"])");
    private static final Pattern CSS_URL_PATTERN = Pattern.compile(
            "(?i)(?<prefix>url\\(\\s*)(?<quote>['\"]?)(?<value>[^)\"']+)(?<suffixQuote>['\"]?)(?<suffix>\\s*\\))");
    private static final Set<String> DEFAULT_ROOTS;
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    static {
        Set<String> roots = new HashSet<>(Arrays.asList(
                "upload", "uploads", "articles", "article", "infra", "file", "files",
                "images", "image", "assets", "asset", "media", "resource", "resources",
                "storage", "owner", "qr", "poster", "banner", "avatar", "content"
        ));
        DEFAULT_ROOTS = Collections.unmodifiableSet(roots);
    }

    private final ITenantStorageService tenantStorageService;
    private final IInfraFileConfigService infraFileConfigService;
    private final ObjectMapper objectMapper;

    private final ConcurrentMap<Long, CachedBase> tenantBaseCache = new ConcurrentHashMap<>();
    private volatile CachedBase infraBaseCache;

    public ArticleFileUrlResolver(ITenantStorageService tenantStorageService,
                                  IInfraFileConfigService infraFileConfigService,
                                  ObjectMapper objectMapper) {
        this.tenantStorageService = tenantStorageService;
        this.infraFileConfigService = infraFileConfigService;
        this.objectMapper = objectMapper;
    }

    public void normalize(Article article) {
        if (article == null) {
            return;
        }
        List<FileBase> bases = resolveBases(resolveTenantId(article));
        if (bases.isEmpty()) {
            return;
        }
        applyNormalization(article, bases);
    }

    public void normalize(Collection<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        Map<Long, List<Article>> byTenant = new HashMap<>();
        for (Article article : articles) {
            if (article == null) {
                continue;
            }
            Long tenantId = resolveTenantId(article);
            byTenant.computeIfAbsent(tenantId, key -> new ArrayList<>()).add(article);
        }
        for (Map.Entry<Long, List<Article>> entry : byTenant.entrySet()) {
            List<FileBase> bases = resolveBases(entry.getKey());
            if (bases.isEmpty()) {
                continue;
            }
            for (Article article : entry.getValue()) {
                applyNormalization(article, bases);
            }
        }
    }

    private void applyNormalization(Article article, List<FileBase> bases) {
        article.setCoverImage(resolveSingleUrl(article.getCoverImage(), bases));
        article.setContent(resolveRichText(article.getContent(), bases));
        article.setPreviewContent(resolveRichText(article.getPreviewContent(), bases));
        article.setMemberContent(resolveRichText(article.getMemberContent(), bases));

        if (looksLikeHtml(article.getSummary())) {
            article.setSummary(resolveRichText(article.getSummary(), bases));
        }
        if (looksLikeHtml(article.getRemark())) {
            article.setRemark(resolveRichText(article.getRemark(), bases));
        }
    }

    private Long resolveTenantId(Article article) {
        if (article != null && article.getTenantId() != null && article.getTenantId() > 0) {
            return article.getTenantId();
        }
        return 1L;
    }

    private String resolveSingleUrl(String candidate, List<FileBase> bases) {
        if (StringUtils.isEmpty(candidate)) {
            return candidate;
        }
        String trimmed = candidate.trim();
        if (trimmed.isEmpty()) {
            return candidate;
        }
        for (FileBase base : bases) {
            String resolved = rewriteUrl(trimmed, base);
            if (resolved != null && !resolved.equals(candidate)) {
                return resolved;
            }
        }
        return candidate;
    }

    private String resolveRichText(String html, List<FileBase> bases) {
        if (StringUtils.isEmpty(html)) {
            return html;
        }
        if (!containsResourceHint(html)) {
            return html;
        }
        String updated = replaceAttributeUrls(html, bases);
        updated = replaceCssUrls(updated, bases);
        return updated;
    }

    private boolean containsResourceHint(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        String lower = content.toLowerCase(Locale.ROOT);
        return lower.contains("src=") || lower.contains("href=") || lower.contains("url(") || lower.contains("upload/");
    }

    private boolean looksLikeHtml(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("<") && trimmed.contains(">");
    }

    private String replaceAttributeUrls(String html, List<FileBase> bases) {
        Matcher matcher = RESOURCE_ATTR_PATTERN.matcher(html);
        StringBuffer buffer = new StringBuffer();
        boolean changed = false;
        while (matcher.find()) {
            String currentValue = matcher.group("value");
            String replacement = currentValue;
            if (!StringUtils.isEmpty(currentValue)) {
                String attrName = matcher.group("attr");
                String normalizedAttr = attrName != null ? attrName.toLowerCase(Locale.ROOT) : "";
                if ("srcset".equals(normalizedAttr)) {
                    replacement = resolveSrcset(currentValue, bases);
                } else {
                    replacement = resolveSingleUrl(currentValue, bases);
                }
            }
            if (!Objects.equals(currentValue, replacement)) {
                changed = true;
            }
            matcher.appendReplacement(buffer,
                    matcher.group("attr")
                            + matcher.group("eq")
                            + matcher.group("quote")
                            + Matcher.quoteReplacement(replacement)
                            + matcher.group("suffix"));
        }
        if (!changed) {
            return html;
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String resolveSrcset(String value, List<FileBase> bases) {
        String[] parts = value.split(",");
        List<String> rebuilt = new ArrayList<>(parts.length);
        boolean changed = false;
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] tokens = trimmed.split("\\s+");
            if (tokens.length == 0) {
                continue;
            }
            String url = tokens[0];
            String descriptor = trimmed.substring(url.length()).trim();
            String rewritten = resolveSingleUrl(url, bases);
            if (!Objects.equals(url, rewritten)) {
                changed = true;
            }
            rebuilt.add(descriptor.isEmpty() ? rewritten : rewritten + " " + descriptor);
        }
        return changed ? String.join(", ", rebuilt) : value;
    }

    private String replaceCssUrls(String html, List<FileBase> bases) {
        Matcher matcher = CSS_URL_PATTERN.matcher(html);
        StringBuffer buffer = new StringBuffer();
        boolean changed = false;
        while (matcher.find()) {
            String currentValue = matcher.group("value");
            String replacement = resolveSingleUrl(currentValue, bases);
            if (!Objects.equals(currentValue, replacement)) {
                changed = true;
            }
            matcher.appendReplacement(buffer,
                    matcher.group("prefix")
                            + matcher.group("quote")
                            + Matcher.quoteReplacement(replacement)
                            + matcher.group("suffixQuote")
                            + matcher.group("suffix"));
        }
        if (!changed) {
            return html;
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private List<FileBase> resolveBases(Long tenantId) {
        List<FileBase> bases = new ArrayList<>(2);
        FileBase tenantBase = getTenantBase(tenantId);
        if (tenantBase != null) {
            bases.add(tenantBase);
        }
        FileBase infraBase = getInfraBase();
        if (infraBase != null && (tenantBase == null || !infraBase.baseUrl.equals(tenantBase.baseUrl))) {
            bases.add(infraBase);
        }
        return bases;
    }

    private FileBase getTenantBase(Long tenantId) {
        Long key = tenantId != null ? tenantId : 1L;
        CachedBase cached = tenantBaseCache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.base;
        }
        FileBase resolved = fetchTenantBase(key);
        if (resolved != null) {
            tenantBaseCache.put(key, new CachedBase(resolved));
        }
        return resolved;
    }

    private FileBase fetchTenantBase(Long tenantId) {
        try {
            StorageProfile profile = tenantStorageService.getStorageProfile(tenantId);
            if (profile == null) {
                return null;
            }
            Set<String> prefixes = new HashSet<>(DEFAULT_ROOTS);
            String baseUrl;
            if (profile.isLocal()) {
                LocalStorageConfig local = profile.getLocal();
                baseUrl = normalizeBase(local != null ? local.getBaseUrl() : null);
                appendPrefix(prefixes, local != null ? local.getPathPrefix() : null);
            } else {
                CloudStorageConfig cloud = profile.getCloud();
                if (cloud == null) {
                    return null;
                }
                baseUrl = normalizeBase(cloud.getDomain());
                if (StringUtils.isEmpty(baseUrl)) {
                    baseUrl = buildEndpointBase(cloud.getBucket(), cloud.getEndpoint());
                }
                appendPrefix(prefixes, cloud.getPathPrefix());
            }
            if (StringUtils.isEmpty(baseUrl)) {
                return null;
            }
            return new FileBase(baseUrl, prefixes);
        } catch (Exception ex) {
            return null;
        }
    }

    private FileBase getInfraBase() {
        CachedBase cached = infraBaseCache;
        if (cached != null && !cached.isExpired()) {
            return cached.base;
        }
        FileBase resolved = fetchInfraBase();
        if (resolved != null) {
            infraBaseCache = new CachedBase(resolved);
        }
        return resolved;
    }

    private FileBase fetchInfraBase() {
        try {
            InfraFileConfig config = infraFileConfigService.getMasterConfig();
            if (config == null || StringUtils.isEmpty(config.getConfig())) {
                return null;
            }
            Map<String, Object> configMap = objectMapper.readValue(
                    config.getConfig(),
                    new TypeReference<Map<String, Object>>() {
                    });
            String domain = asString(configMap.get("domain"));
            String endpoint = asString(configMap.get("endpoint"));
            String bucket = asString(configMap.get("bucket"));
            String pathPrefix = asString(configMap.get("pathPrefix"));

            String baseUrl = normalizeBase(domain);
            if (StringUtils.isEmpty(baseUrl)) {
                baseUrl = buildEndpointBase(bucket, endpoint);
            }
            if (StringUtils.isEmpty(baseUrl)) {
                return null;
            }

            Set<String> prefixes = new HashSet<>(DEFAULT_ROOTS);
            appendPrefix(prefixes, pathPrefix);
            return new FileBase(baseUrl, prefixes);
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildEndpointBase(String bucket, String endpoint) {
        if (StringUtils.isEmpty(bucket) || StringUtils.isEmpty(endpoint)) {
            return null;
        }
        String normalizedEndpoint = endpoint.trim();
        if (!normalizedEndpoint.startsWith("http")) {
            normalizedEndpoint = "https://" + normalizedEndpoint;
        }
        String normalized = bucket.trim() + "." + normalizedEndpoint.replaceFirst("^https?://", "");
        return normalizeBase("https://" + normalized);
    }

    private void appendPrefix(Set<String> prefixes, String rawPrefix) {
        if (StringUtils.isEmpty(rawPrefix)) {
            return;
        }
        String cleaned = rawPrefix.trim();
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (!cleaned.isEmpty()) {
            prefixes.add(cleaned.toLowerCase(Locale.ROOT));
        }
    }

    private String normalizeBase(String candidate) {
        if (StringUtils.isEmpty(candidate)) {
            return null;
        }
        String value = candidate.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return trimTrailingSlash(value);
        }
        if (value.startsWith("//")) {
            return "https:" + trimTrailingSlash(value);
        }
        if (value.startsWith("/")) {
            return trimTrailingSlash(value);
        }
        return "https://" + trimTrailingSlash(value);
    }

    private String trimTrailingSlash(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        String trimmed = value.trim();
        while (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        return String.valueOf(value).trim();
    }

    private String rewriteUrl(String candidate, FileBase base) {
        UrlDetails details = UrlDetails.parse(candidate);
        if (details == null || details.dataUri) {
            return null;
        }
        String normalizedPath = normalizePath(details.path);
        if (StringUtils.isEmpty(normalizedPath)) {
            return null;
        }
        if (!isManagedPath(normalizedPath, base)) {
            return null;
        }
        return base.buildUrl(normalizedPath, details.query, details.fragment);
    }

    private boolean isManagedPath(String path, FileBase base) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        if (normalized.isEmpty()) {
            return false;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        for (String prefix : base.managedRoots) {
            if (lower.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }

    private static final class CachedBase {
        private final FileBase base;
        private final long timestamp;

        private CachedBase(FileBase base) {
            this.base = base;
            this.timestamp = System.currentTimeMillis();
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL.toMillis();
        }
    }

    private static final class FileBase {
        private final String baseUrl;
        private final Set<String> managedRoots;
        private final String host;

        private FileBase(String baseUrl, Set<String> managedRoots) {
            this.baseUrl = baseUrl;
            this.managedRoots = managedRoots;
            this.host = resolveHost(baseUrl);
        }

        private String buildUrl(String path, String query, String fragment) {
            String normalizedBase = baseUrl;
            if (!normalizedBase.startsWith("http") && !normalizedBase.startsWith("/")) {
                normalizedBase = "https://" + normalizedBase;
            }
            if (normalizedBase.endsWith("/")) {
                normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
            }
            String normalizedPath = path.startsWith("/") ? path : "/" + path;
            StringBuilder builder = new StringBuilder(normalizedBase).append(normalizedPath);
            if (StringUtils.isNotEmpty(query)) {
                builder.append('?').append(query);
            }
            if (StringUtils.isNotEmpty(fragment)) {
                builder.append('#').append(fragment);
            }
            return builder.toString();
        }

        private String resolveHost(String base) {
            try {
                URI uri = new URI(base.startsWith("http") ? base : "https://" + base);
                return uri.getHost();
            } catch (URISyntaxException ex) {
                return null;
            }
        }
    }

    private static final class UrlDetails {
        private final boolean dataUri;
        private final String host;
        private final String path;
        private final String query;
        private final String fragment;

        private UrlDetails(boolean dataUri, String host, String path, String query, String fragment) {
            this.dataUri = dataUri;
            this.host = host;
            this.path = path;
            this.query = query;
            this.fragment = fragment;
        }

        private static UrlDetails parse(String raw) {
            if (StringUtils.isEmpty(raw)) {
                return null;
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("data:") || trimmed.startsWith("blob:")) {
                return new UrlDetails(true, null, null, null, null);
            }
            String candidate = trimmed;
            if (trimmed.startsWith("//")) {
                candidate = "https:" + trimmed;
            }
            try {
                URI uri = new URI(candidate);
                String host = uri.getHost();
                String query = uri.getQuery();
                String fragment = uri.getFragment();
                String path = uri.getPath();
                if (!uri.isAbsolute()) {
                    host = null;
                }
                return new UrlDetails(false, host, path, query, fragment);
            } catch (URISyntaxException ex) {
                return null;
            }
        }
    }
}
