package com.simpleshare.framework.web.service;

import com.simpleshare.common.constant.CacheConstants;
import com.simpleshare.common.constant.Constants;
import com.simpleshare.common.utils.JwtUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.common.utils.uuid.IdUtils;
import com.simpleshare.framework.redis.RedisCache;
import com.simpleshare.common.core.domain.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * token验证处理
 *
 * @author SimpleShare
 */
@Component
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    // Refresh Token有效期（默认43200分钟 = 30天）
    @Value("${token.refreshExpireTime:43200}")
    private int refreshExpireTime;

    // Refresh Token前缀
    @Value("${token.refreshPrefix:refresh-}")
    private String refreshPrefix;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        log.info("TokenService获取用户信息 - 请求路径: {}, Token: {}",
                 request.getRequestURI(),
                 token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");

        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                log.info("Token解析成功 - Claims: {}", claims.keySet());

                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                log.info("从token中获取UUID: {}", uuid);

                String userKey = getTokenKey(uuid);
                log.info("Redis缓存key: {}", userKey);

                // 从Redis获取JSON字符串并反序列化为LoginUser对象
                String cachedJson = redisCache.getCacheObject(userKey);
                log.debug("Redis缓存原始数据: {}", cachedJson);
                if (StringUtils.isEmpty(cachedJson)) {
                    log.warn("Redis中未找到用户信息，key: {}", userKey);
                    return null;
                }

                try {
                    LoginUser loginUser = objectMapper.readValue(cachedJson, LoginUser.class);
                    log.info("从Redis成功获取用户信息: {}", loginUser.getUser().getUserName());

                    // 验证token是否需要刷新
                    verifyToken(loginUser);

                    return loginUser;
                } catch (Exception e) {
                    log.error("反序列化LoginUser失败: {}，原始数据: {}", e.getMessage(), cachedJson, e);
                    // 删除无效的缓存
                    redisCache.deleteObject(userKey);
                    return null;
                }
            } catch (ExpiredJwtException e) {
                log.warn("JWT token已过期，尝试从Redis恢复: {}", e.getMessage());
                // 对于过期的JWT token，尝试从token中提取uuid
                try {
                    // 尝试直接从token中解析claims（忽略过期）
                    String uuid = extractUuidFromExpiredToken(token);
                    if (StringUtils.isNotEmpty(uuid)) {
                        String userKey = getTokenKey(uuid);
                        String cachedJson = redisCache.getCacheObject(userKey);
                log.debug("Redis缓存原始数据: {}", cachedJson);

                        if (StringUtils.isNotEmpty(cachedJson)) {
                            try {
                                LoginUser loginUser = objectMapper.readValue(cachedJson, LoginUser.class);
                                // 检查Redis中的用户信息是否仍然有效
                                if (loginUser.getExpireTime() > System.currentTimeMillis()) {
                                    log.info("从Redis成功恢复用户信息: {}", loginUser.getUser().getUserName());
                                    // 刷新token
                                    refreshToken(loginUser);
                                    return loginUser;
                                } else {
                                    log.warn("Redis中的用户信息已过期: {}", loginUser.getUser().getUserName());
                                    redisCache.deleteObject(userKey);
                                }
                            } catch (Exception jsonException) {
                                log.error("反序列化过期token的LoginUser失败: {}", jsonException.getMessage(), jsonException);
                                redisCache.deleteObject(userKey);
                            }
                        }
                    }
                } catch (Exception extractException) {
                    log.error("从过期token提取uuid失败: {}", extractException.getMessage(), extractException);
                }
            } catch (Exception e) {
                log.error("解析token失败: {}, token: {}", e.getMessage(), token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null", e);
            }
        } else {
            log.warn("请求中未找到token，请求路径: {}", request.getRequestURI());
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser) {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createToken(LoginUser loginUser) {
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setUserAgent(loginUser);

        log.info("创建token - 用户: {}, token UUID: {}", loginUser.getUser().getUserName(), token);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        String jwtToken = createToken(claims);

        // 验证Redis保存是否成功
        String userKey = getTokenKey(token);
        String cachedUser = redisCache.getCacheObject(userKey);
        log.info("创建token完成 - JWT: {}, Redis key: {}, 缓存状态: {}",
                 jwtToken.substring(0, 20) + "...", userKey, cachedUser != null ? "成功" : "失败");

        return jwtToken;
    }

    /**
     * 创建刷新令牌
     *
     * @param loginUser 用户信息
     * @return 刷新令牌
     */
    public String createRefreshToken(LoginUser loginUser) {
        String refreshUuid = IdUtils.fastUUID();
        cacheRefreshToken(refreshUuid, loginUser);
        return refreshPrefix + refreshUuid;
    }

    /**
     * 通过刷新令牌获取登录用户信息
     */
    public LoginUser getLoginUserByRefreshToken(String refreshToken) {
        String refreshUuid = extractRefreshUuid(refreshToken);
        if (StringUtils.isEmpty(refreshUuid)) {
            return null;
        }
        String refreshKey = CacheConstants.REFRESH_TOKEN_KEY + refreshUuid;
        String cachedJson = redisCache.getCacheObject(refreshKey);
        if (StringUtils.isEmpty(cachedJson)) {
            log.warn("未找到刷新令牌对应的用户信息，key: {}", refreshKey);
            return null;
        }
        try {
            return objectMapper.readValue(cachedJson, LoginUser.class);
        } catch (Exception e) {
            log.error("反序列化刷新令牌用户信息失败: {}", e.getMessage(), e);
            redisCache.deleteObject(refreshKey);
            return null;
        }
    }

    /**
     * 使刷新令牌失效
     */
    public void invalidateRefreshToken(String refreshToken) {
        String refreshUuid = extractRefreshUuid(refreshToken);
        if (StringUtils.isNotEmpty(refreshUuid)) {
            String refreshKey = CacheConstants.REFRESH_TOKEN_KEY + refreshUuid;
            redisCache.deleteObject(refreshKey);
        }
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param loginUser
     * @return 令牌
     */
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser序列化为JSON字符串并缓存
        String userKey = getTokenKey(loginUser.getToken());
        try {
            String loginUserJson = objectMapper.writeValueAsString(loginUser);
            log.info("保存用户信息到Redis - key: {}, 过期时间: {}分钟, 数据大小: {}",
                     userKey, expireTime, loginUserJson.length());
            redisCache.setCacheObject(userKey, loginUserJson, expireTime, java.util.concurrent.TimeUnit.MINUTES);

            // 立即验证是否保存成功
            String cachedData = redisCache.getCacheObject(userKey);
            if (cachedData != null) {
                log.info("Redis保存成功 - key: {}, 数据长度: {}", userKey, cachedData.length());
            } else {
                log.error("Redis保存失败 - key: {}, 保存后立即查询为空", userKey);
            }
        } catch (Exception e) {
            log.error("序列化LoginUser失败: {}", e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("缓存用户信息失败", e);
        }
    }

    /**
     * 设置用户代理信息
     *
     * @param loginUser 登录信息
     */
    public void setUserAgent(LoginUser loginUser) {
        // UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        // String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        // loginUser.setIpaddr(ip);
        // loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        // loginUser.setBrowser(userAgent.getBrowser().getName());
        // loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims) {
        String token = JwtUtils.createToken(claims, secret, expireTime);
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token) {
        log.info("解析token - 使用的secret: {}, 长度: {}", secret.substring(0, 5) + "...", secret.length());
        try {
            Claims claims = JwtUtils.parseToken(token, secret);
            log.info("JWT token解析成功");
            return claims;
        } catch (Exception e) {
            log.error("JWT token解析失败，secret长度: {}, 错误: {}", secret.length(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid) {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    /**
     * 从过期的JWT token中提取uuid
     *
     * @param token 过期的JWT token
     * @return uuid
     */
    private String extractUuidFromExpiredToken(String token) {
        try {
            // 使用兼容的方式来解析过期的token
            io.jsonwebtoken.JwtParser parser = io.jsonwebtoken.Jwts.parser()
                    .setSigningKey(JwtUtils.getSigningKey(secret));

            // 尝试解析，如果过期会抛出ExpiredJwtException
            io.jsonwebtoken.Jws<io.jsonwebtoken.Claims> jws = parser.parseClaimsJws(token);
            io.jsonwebtoken.Claims claims = jws.getBody();

            return (String) claims.get(Constants.LOGIN_USER_KEY);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 对于过期的token，我们可以从异常中获取claims
            try {
                io.jsonwebtoken.Claims claims = e.getClaims();
                return (String) claims.get(Constants.LOGIN_USER_KEY);
            } catch (Exception ex) {
                log.error("从过期token异常中提取uuid失败: {}", ex.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("从过期token提取uuid失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 清除所有token缓存
     */
    public void clearAllTokenCache() {
        try {
            Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisCache.deleteObject(keys);
            }
            Collection<String> refreshKeys = redisCache.keys(CacheConstants.REFRESH_TOKEN_KEY + "*");
            if (refreshKeys != null && !refreshKeys.isEmpty()) {
                redisCache.deleteObject(refreshKeys);
            }
        } catch (Exception e) {
            // 记录日志但不抛出异常
            System.err.println("清除token缓存失败: " + e.getMessage());
        }
    }

    private void cacheRefreshToken(String refreshUuid, LoginUser loginUser) {
        String refreshKey = CacheConstants.REFRESH_TOKEN_KEY + refreshUuid;
        try {
            String loginUserJson = objectMapper.writeValueAsString(loginUser);
            redisCache.setCacheObject(refreshKey, loginUserJson, refreshExpireTime, java.util.concurrent.TimeUnit.MINUTES);
            log.info("保存Refresh Token到Redis - key: {}, 过期时间: {}分钟", refreshKey, refreshExpireTime);
        } catch (Exception e) {
            log.error("缓存Refresh Token失败: {}", e.getMessage(), e);
            throw new RuntimeException("缓存刷新令牌失败", e);
        }
    }

    private String extractRefreshUuid(String refreshToken) {
        if (StringUtils.isEmpty(refreshToken)) {
            return null;
        }
        if (!refreshToken.startsWith(refreshPrefix)) {
            return null;
        }
        return refreshToken.substring(refreshPrefix.length());
    }
}
