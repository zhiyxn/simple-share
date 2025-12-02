package com.simpleshare.system.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;

/**
 * Request payload for updating current user profile.
 */
public class UserProfileUpdateRequest {

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @Size(max = 200, message = "个人简介不能超过200个字符")
    private String bio;

    @Size(max = 255, message = "个人网站地址过长")
    private String website;

    @Size(max = 255, message = "Github 链接过长")
    private String github;

    @Size(max = 255, message = "Twitter 链接过长")
    private String twitter;

    @Size(max = 20, message = "语言编码过长")
    private String language;

    @Size(max = 50, message = "时区编码过长")
    private String timezone;

    @Valid
    private EmailNotificationSettings emailNotifications;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public static class EmailNotificationSettings {
        private boolean comments = true;
        private boolean likes = true;
        private boolean follows = true;
        private boolean newsletter = false;

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
