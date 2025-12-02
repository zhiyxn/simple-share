package com.simpleshare.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simpleshare.common.exception.ServiceException;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.domain.SysEmailVerification;
import com.simpleshare.system.enums.EmailVerificationScene;
import com.simpleshare.system.mapper.SysEmailVerificationMapper;
import com.simpleshare.system.service.IEmailVerificationService;
import com.simpleshare.system.service.ISysTenantConfigService;
import com.simpleshare.system.dto.TenantSettingsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * 邮箱验证码业务实现
 */
@Service
public class EmailVerificationServiceImpl implements IEmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private static final String CONFIG_EMAIL_KEY = "settings_email";
    private static final int DEFAULT_EXPIRE_MINUTES = 10;
    private static final int RESEND_INTERVAL_SECONDS = 60;

    private final SysEmailVerificationMapper verificationMapper;
    private final ISysTenantConfigService tenantConfigService;

    @Autowired(required = false)
    private MailProperties mailProperties;

    @Autowired
    public EmailVerificationServiceImpl(SysEmailVerificationMapper verificationMapper,
                                        ISysTenantConfigService tenantConfigService) {
        this.verificationMapper = verificationMapper;
        this.tenantConfigService = tenantConfigService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysEmailVerification sendCode(Long tenantId, String email, EmailVerificationScene scene,
                                         String ipAddress, String userAgent) {
        validateEmail(email);
        if (scene == null) {
            throw new ServiceException("验证码场景无效");
        }

        LocalDateTime now = LocalDateTime.now();

        // 限制发送频率
        SysEmailVerification recent = verificationMapper.selectOne(new LambdaQueryWrapper<SysEmailVerification>()
                .eq(SysEmailVerification::getTenantId, tenantId)
                .eq(SysEmailVerification::getEmail, email)
                .eq(SysEmailVerification::getScene, scene.getCode())
                .orderByDesc(SysEmailVerification::getCreateTime)
                .last("limit 1"));

        if (recent != null && recent.getCreateTime() != null
                && recent.getCreateTime().isAfter(now.minusSeconds(RESEND_INTERVAL_SECONDS))) {
            throw new ServiceException("验证码发送过于频繁，请稍后再试");
        }

        String code = generateCode();

        SysEmailVerification record = new SysEmailVerification();
        record.setTenantId(tenantId);
        record.setEmail(email);
        record.setScene(scene.getCode());
        record.setCode(code);
        record.setStatus(0);
        record.setRequestId(UUID.randomUUID().toString());
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        record.setExpireTime(now.plusMinutes(DEFAULT_EXPIRE_MINUTES));
        record.setCreateTime(now);
        record.setUpdateTime(now);

        verificationMapper.insert(record);

        sendEmail(tenantId, email, scene, code, record.getExpireTime());

        log.info("发送邮箱验证码成功: tenantId={}, email={}, scene={}", tenantId, email, scene.getCode());
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysEmailVerification verifyCode(Long tenantId, String email, EmailVerificationScene scene, String code) {
        validateEmail(email);
        if (StringUtils.isEmpty(code)) {
            throw new ServiceException("验证码不能为空");
        }

        SysEmailVerification record = verificationMapper.selectOne(new LambdaQueryWrapper<SysEmailVerification>()
                .eq(SysEmailVerification::getTenantId, tenantId)
                .eq(SysEmailVerification::getEmail, email)
                .eq(SysEmailVerification::getScene, scene.getCode())
                .eq(SysEmailVerification::getCode, code)
                .orderByDesc(SysEmailVerification::getCreateTime)
                .last("limit 1"));

        if (record == null) {
            throw new ServiceException("验证码不正确");
        }

        if (record.getStatus() != null && record.getStatus() != 0) {
            throw new ServiceException("验证码已使用或失效");
        }

        if (record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now())) {
            record.setStatus(2);
            record.setUpdateTime(LocalDateTime.now());
            verificationMapper.updateById(record);
            throw new ServiceException("验证码已过期");
        }

        record.setStatus(1);
        record.setVerifiedTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        verificationMapper.updateById(record);

        return record;
    }

    @Override
    public SysEmailVerification findByToken(Long tenantId, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        return verificationMapper.selectOne(new LambdaQueryWrapper<SysEmailVerification>()
                .eq(SysEmailVerification::getTenantId, tenantId)
                .eq(SysEmailVerification::getRequestId, token)
                .orderByDesc(SysEmailVerification::getCreateTime)
                .last("limit 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsConsumed(Long id) {
        if (id == null) {
            return;
        }
        SysEmailVerification record = verificationMapper.selectById(id);
        if (record == null) {
            return;
        }
        record.setStatus(2);
        record.setUpdateTime(LocalDateTime.now());
        record.setVerifiedTime(LocalDateTime.now());
        verificationMapper.updateById(record);
    }

    private void sendEmail(Long tenantId, String email, EmailVerificationScene scene, String code, LocalDateTime expireTime) {
        EmailAccountSettings settings = resolveEmailSettings(tenantId);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settings.getHost());
        mailSender.setPort(settings.getPort());
        mailSender.setUsername(settings.getUsername());
        mailSender.setPassword(settings.getPassword());
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.smtp.host", settings.getHost());
        props.put("mail.smtp.port", Integer.toString(settings.getPort()));

        boolean useSecure = settings.isSecure();
        boolean implicitSsl = useSecure && isImplicitSslPort(settings.getPort());
        if (implicitSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", settings.getHost());
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", Integer.toString(settings.getPort()));
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.starttls.required", "false");
        } else {
            props.put("mail.smtp.ssl.enable", "false");
            if (useSecure) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.trust", settings.getHost());
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            } else {
                props.put("mail.smtp.starttls.enable", "false");
                props.put("mail.smtp.starttls.required", "false");
            }
        }

        mailSender.setProtocol(implicitSsl ? "smtps" : "smtp");
        mailSender.setJavaMailProperties(props);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(email);
            helper.setFrom(settings.getFromAddress(), settings.getSenderName());
            helper.setSubject(buildSubject(scene));
            helper.setText(buildContent(scene, code, expireTime), false);

            if (StringUtils.isNotEmpty(settings.getReplyTo())) {
                helper.setReplyTo(settings.getReplyTo());
            }

            mailSender.send(message);
        } catch (MailException ex) {
            log.error("发送邮件失败: {}", ex.getMessage(), ex);
            throw new ServiceException("邮件发送失败，请检查邮箱配置");
        } catch (Exception e) {
            log.error("构建邮件失败: {}", e.getMessage(), e);
            throw new ServiceException("发送邮件失败");
        }
    }

    private String buildSubject(EmailVerificationScene scene) {
        return "简享" + scene.getDescription() + "验证码";
    }

    private String buildContent(EmailVerificationScene scene, String code, LocalDateTime expireTime) {
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), expireTime);
        if (minutes <= 0) {
            minutes = DEFAULT_EXPIRE_MINUTES;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("您好，您正在进行").append(scene.getDescription()).append("操作。\n\n")
                .append("您的验证码为：").append(code).append("\n")
                .append("该验证码将在").append(minutes).append("分钟后过期，请尽快完成操作。\n\n")
                .append("如果这不是您本人的操作，请忽略本邮件。");
        return builder.toString();
    }

    private void validateEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            throw new ServiceException("邮箱地址不能为空");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ServiceException("邮箱地址格式不正确");
        }
    }

    private String generateCode() {
        int number = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(number);
    }

    private EmailAccountSettings resolveEmailSettings(Long tenantId) {
        Map<String, String> configMap = tenantConfigService.selectConfigMap(tenantId);
        TenantSettingsRequest.EmailSettings emailSettings = null;
        if (configMap != null) {
            String json = configMap.get(CONFIG_EMAIL_KEY);
            if (StringUtils.isNotEmpty(json)) {
                try {
                    emailSettings = JSON.parseObject(json, TenantSettingsRequest.EmailSettings.class);
                } catch (Exception e) {
                    log.warn("解析邮箱配置失败，tenantId={}, error={}", tenantId, e.getMessage());
                }
            }
        }

        log.info("检查邮件配置: mailProperties={}, host={}, username={}",
                mailProperties != null ? "已加载" : "未加载",
                mailProperties != null ? mailProperties.getHost() : "N/A",
                mailProperties != null ? mailProperties.getUsername() : "N/A");

        if (mailProperties != null) {
            Map<String, String> mailProps = mailProperties.getProperties();
            boolean starttlsEnabled = isTruthy(mailProps != null ? mailProps.get("mail.smtp.starttls.enable") : null);
            boolean sslEnabled = isTruthy(mailProps != null ? mailProps.get("mail.smtp.ssl.enable") : null);

            if (emailSettings == null) {
                emailSettings = new TenantSettingsRequest.EmailSettings();
            }

            if (StringUtils.isEmpty(emailSettings.getHost()) && StringUtils.isNotEmpty(mailProperties.getHost())) {
                emailSettings.setHost(mailProperties.getHost());
            }
            if (emailSettings.getPort() == null && mailProperties.getPort() != null) {
                emailSettings.setPort(mailProperties.getPort());
            }
            if (emailSettings.getSecure() == null && (starttlsEnabled || sslEnabled)) {
                emailSettings.setSecure(true);
            } else if (Boolean.FALSE.equals(emailSettings.getSecure()) && sslEnabled) {
                emailSettings.setSecure(true);
            }
            if (needsFallbackUsername(emailSettings.getUsername()) && StringUtils.isNotEmpty(mailProperties.getUsername())) {
                emailSettings.setUsername(mailProperties.getUsername());
            }
            if (needsFallbackPassword(emailSettings.getPassword()) && StringUtils.isNotEmpty(mailProperties.getPassword())) {
                emailSettings.setPassword(mailProperties.getPassword());
            }
            if (StringUtils.isEmpty(emailSettings.getFrom())) {
                if (StringUtils.isNotEmpty(emailSettings.getUsername())) {
                    emailSettings.setFrom(emailSettings.getUsername());
                } else if (StringUtils.isNotEmpty(mailProperties.getUsername())) {
                    emailSettings.setFrom(mailProperties.getUsername());
                }
            }
            if (StringUtils.isEmpty(emailSettings.getSenderName())) {
                emailSettings.setSenderName("SimpleShare");
            }
            if (emailSettings.getPort() == null) {
                if (Boolean.TRUE.equals(emailSettings.getSecure())) {
                    emailSettings.setPort(sslEnabled ? 465 : (starttlsEnabled ? 587 : 465));
                } else if (mailProperties.getPort() != null) {
                    emailSettings.setPort(mailProperties.getPort());
                } else if (starttlsEnabled) {
                    emailSettings.setPort(587);
                } else if (StringUtils.isNotEmpty(emailSettings.getHost())) {
                    emailSettings.setPort(25);
                }
            }

            log.info("合并后的邮箱配置: host={}, port={}, username={}, secure={}, source={}",
                    emailSettings.getHost(),
                    emailSettings.getPort(),
                    emailSettings.getUsername(),
                    emailSettings.getSecure(),
                    configMap != null && configMap.containsKey(CONFIG_EMAIL_KEY) ? "tenant" : "spring.mail");
        }

        if (emailSettings == null || StringUtils.isEmpty(emailSettings.getHost())) {
            log.error("邮箱配置检查失败: emailSettings={}, host={}",
                    emailSettings != null ? "已设置" : "未设置",
                    emailSettings != null ? emailSettings.getHost() : "N/A");
            throw new ServiceException("未配置邮箱服务器，请先在后台完善邮箱设置");
        }

        EmailAccountSettings settings = new EmailAccountSettings();
        boolean secure = Boolean.TRUE.equals(emailSettings.getSecure());
        int port = emailSettings.getPort() != null ? emailSettings.getPort() : (secure ? 465 : 587);
        settings.setHost(emailSettings.getHost());
        settings.setPort(port);
        settings.setUsername(emailSettings.getUsername());
        settings.setPassword(emailSettings.getPassword());
        settings.setSecure(secure);
        settings.setFromAddress(StringUtils.isNotEmpty(emailSettings.getFrom()) ? emailSettings.getFrom() : emailSettings.getUsername());
        settings.setSenderName(StringUtils.isNotEmpty(emailSettings.getSenderName()) ? emailSettings.getSenderName() : "SimpleShare");
        settings.setReplyTo(emailSettings.getReplyTo());

        if (needsFallbackUsername(settings.getUsername()) && mailProperties != null && StringUtils.isNotEmpty(mailProperties.getUsername())) {
            settings.setUsername(mailProperties.getUsername());
        }
        if (needsFallbackPassword(settings.getPassword()) && mailProperties != null && StringUtils.isNotEmpty(mailProperties.getPassword())) {
            settings.setPassword(mailProperties.getPassword());
        }
        if (StringUtils.isEmpty(settings.getFromAddress()) && StringUtils.isNotEmpty(settings.getUsername())) {
            settings.setFromAddress(settings.getUsername());
        }

        if (log.isInfoEnabled()) {
            log.info("使用邮箱服务: host={}, port={}, secure={}", settings.getHost(), settings.getPort(), settings.isSecure());
        }

        if (StringUtils.isEmpty(settings.getUsername()) || StringUtils.isEmpty(settings.getPassword())) {
            throw new ServiceException("邮箱账号或密码未配置");
        }

        return settings;
    }

    private boolean isImplicitSslPort(int port) {
        return port == 465 || port == 994 || port == 995;
    }

    private boolean needsFallbackUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return true;
        }
        String trimmed = username.trim();
        if (trimmed.length() <= 3) {
            return true;
        }
        if ("admin".equalsIgnoreCase(trimmed) || "root".equalsIgnoreCase(trimmed) || "system".equalsIgnoreCase(trimmed)) {
            return true;
        }
        return !trimmed.contains("@");
    }

    private boolean needsFallbackPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return true;
        }
        String trimmed = password.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        return "******".equals(trimmed) || "********".equals(trimmed);
    }

    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return false;
        }
        return "true".equalsIgnoreCase(text)
                || "yes".equalsIgnoreCase(text)
                || "on".equalsIgnoreCase(text)
                || "1".equals(text);
    }

    private static class EmailAccountSettings {
        private String host;
        private int port;
        private String username;
        private String password;
        private boolean secure;
        private String fromAddress;
        private String senderName;
        private String replyTo;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public String getFromAddress() {
            return fromAddress;
        }

        public void setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }
    }
}
