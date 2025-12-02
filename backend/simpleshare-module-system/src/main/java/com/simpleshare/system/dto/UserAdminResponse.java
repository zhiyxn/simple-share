package com.simpleshare.system.dto;

import com.simpleshare.common.core.domain.entity.SysRole;
import com.simpleshare.common.core.domain.entity.SysUser;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Response model for admin user management pages.
 */
public class UserAdminResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String avatar;
    private String remark;
    private String createdAt;
    private String updatedAt;
    private String lastLoginAt;
    private String lastLoginIp;
    private String userType;
    private String vipExpireTime;
    private Boolean articleReviewRequired;

    public static UserAdminResponse from(SysUser user) {
        UserAdminResponse response = new UserAdminResponse();
        if (user.getUserId() != null) {
            response.setId(String.valueOf(user.getUserId()));
        }
        response.setUsername(user.getUserName());
        response.setNickname(user.getNickName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhonenumber());
        response.setStatus(mapStatus(user.getStatus()));
        response.setAvatar(user.getAvatar());
        response.setRemark(user.getRemark());
        if (user.getCreateTime() != null) {
            response.setCreatedAt(FORMATTER.format(user.getCreateTime()));
        }
        if (user.getUpdateTime() != null) {
            response.setUpdatedAt(FORMATTER.format(user.getUpdateTime()));
        }
        if (user.getLoginDate() != null) {
            response.setLastLoginAt(FORMATTER.format(user.getLoginDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        }
        response.setLastLoginIp(user.getLoginIp());
        response.setUserType(user.getUserType());
        if (user.getVipExpireTime() != null) {
            response.setVipExpireTime(FORMATTER.format(user.getVipExpireTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        }
        response.setArticleReviewRequired(user.getArticleReviewRequired());
        response.setRole(resolveRole(user.getRoles()));
        return response;
    }

    private static String resolveRole(List<SysRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return "user";
        }
        String key = roles.get(0).getRoleKey();
        if (key == null) {
            return "user";
        }
        // 直接返回数据库中的角色值，不做任何转换
        return key;
    }

    private static String mapStatus(String status) {
        if ("0".equals(status)) {
            return "active";
        }
        if ("1".equals(status)) {
            return "inactive";
        }
        return "active";
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
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

    public Boolean getArticleReviewRequired() {
        return articleReviewRequired;
    }

    public void setArticleReviewRequired(Boolean articleReviewRequired) {
        this.articleReviewRequired = articleReviewRequired;
    }
}
