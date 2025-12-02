package com.simpleshare.system.dto;

/**
 * Request payload for creating or updating users from the admin interface.
 */
public class UserAdminRequest {

    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String password;
    private String avatar;
    private String remark;
    // 用户类型：user/member 或 0/3 兼容
    private String userType;
    // 会员到期时间（可选，格式：yyyy-MM-dd HH:mm:ss）
    private String vipExpireTime;
    // 发布文章是否需要审核（true 需要，false 无需）
    private Boolean articleReviewRequired;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
