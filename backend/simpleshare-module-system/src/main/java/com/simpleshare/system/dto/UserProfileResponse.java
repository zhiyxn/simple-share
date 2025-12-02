package com.simpleshare.system.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleshare.common.core.domain.entity.SysUser;
import com.simpleshare.common.core.domain.model.LoginUser;
import com.simpleshare.common.enums.UserTypeEnum;
import com.simpleshare.common.utils.StringUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;

/**
 * Response payload for current user profile.
 */
public class UserProfileResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String username;
    private String email;
    private Boolean emailVerified;
    private String nickname;
    private String avatar;
    private String bio;
    private String website;
    private String github;
    private String twitter;
    private Boolean twoFactorEnabled;
    private String language;
    private String timezone;
    private EmailNotificationSettings emailNotifications;
    private String userType;
    private String vipExpireTime;
    private Boolean vipActive;
    private String[] roles;
    private String[] permissions;
    private String createdAt;
    private String updatedAt;

    public static UserProfileResponse from(SysUser user, LoginUser loginUser) {
        UserProfileResponse response = new UserProfileResponse();
        if (user != null) {
            if (user.getUserId() != null) {
                response.setId(String.valueOf(user.getUserId()));
            }
            response.setUsername(user.getUserName());
            response.setEmail(user.getEmail());
            response.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
            response.setNickname(StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName());
            response.setAvatar(user.getAvatar());
            response.setBio(user.getBio());
            response.setWebsite(user.getWebsite());
            response.setGithub(user.getGithub());
            response.setTwitter(user.getTwitter());
            response.setTwoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()));
            response.setLanguage(StringUtils.isNotEmpty(user.getLanguage()) ? user.getLanguage() : "zh-CN");
            response.setTimezone(StringUtils.isNotEmpty(user.getTimezone()) ? user.getTimezone() : "Asia/Shanghai");
            response.setEmailNotifications(parseEmailNotifications(user.getEmailNotifications()));
            response.setUserType(user.getUserType());
            if (user.getVipExpireTime() != null) {
                response.setVipExpireTime(DATE_TIME_FORMATTER.format(
                    user.getVipExpireTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                ));
            }
            response.setVipActive(user.isVip() && !user.isVipExpired());
            if (user.getCreateTime() != null) {
                response.setCreatedAt(DATE_TIME_FORMATTER.format(user.getCreateTime()));
            }
            if (user.getUpdateTime() != null) {
                response.setUpdatedAt(DATE_TIME_FORMATTER.format(user.getUpdateTime()));
            }
        }

        if (loginUser != null) {
            response.setRoles(determineRoles(loginUser));
            response.setPermissions(toArray(loginUser.getPermissions()));
        }

        return response;
    }

    private static EmailNotificationSettings parseEmailNotifications(String json) {
        if (StringUtils.isBlank(json)) {
            return EmailNotificationSettings.defaults();
        }
        try {
            return OBJECT_MAPPER.readValue(json, EmailNotificationSettings.class);
        } catch (JsonProcessingException e) {
            return EmailNotificationSettings.defaults();
        }
    }

    private static String[] determineRoles(LoginUser loginUser) {
        if (loginUser == null) {
            return new String[]{"user"};
        }

        Set<String> permissions = loginUser.getPermissions();
        if (permissions != null && permissions.contains("*:*:*")) {
            return new String[]{"admin"};
        }

        SysUser sysUser = loginUser.getUser();
        if (sysUser != null && UserTypeEnum.isAdmin(sysUser.getUserType())) {
            return new String[]{"admin"};
        }

        return new String[]{"user"};
    }

    private static String[] toArray(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return new String[0];
        }
        return set.stream().filter(Objects::nonNull).toArray(String[]::new);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public EmailNotificationSettings getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(EmailNotificationSettings emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getVipExpireTime() {
        return vipExpireTime;
    }

    public void setVipExpireTime(String vipExpireTime) {
        this.vipExpireTime = vipExpireTime;
    }

    public Boolean getVipActive() {
        return vipActive;
    }

    public void setVipActive(Boolean vipActive) {
        this.vipActive = vipActive;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class EmailNotificationSettings {
        private boolean comments = true;
        private boolean likes = true;
        private boolean follows = true;
        private boolean newsletter = false;

        public static EmailNotificationSettings defaults() {
            return new EmailNotificationSettings();
        }

        public boolean isComments() {
            return comments;
        }

        public void setComments(boolean comments) {
            this.comments = comments;
        }

        public boolean isLikes() {
            return likes;
        }

        public void setLikes(boolean likes) {
            this.likes = likes;
        }

        public boolean isFollows() {
            return follows;
        }

        public void setFollows(boolean follows) {
            this.follows = follows;
        }

        public boolean isNewsletter() {
            return newsletter;
        }

        public void setNewsletter(boolean newsletter) {
            this.newsletter = newsletter;
        }
    }
}
