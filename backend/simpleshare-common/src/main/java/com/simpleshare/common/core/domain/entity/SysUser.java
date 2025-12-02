package com.simpleshare.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;
import com.simpleshare.common.enums.UserTypeEnum;
import com.simpleshare.common.enums.UserStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 用户对象 sys_user
 *
 * @author SimpleShare
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 部门ID */
    private Long deptId;

    /** 用户账号 */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    private String userName;

    /** 用户昵称 */
    @NotBlank(message = "用户昵称不能为空")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    /** 用户邮箱 */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /** 手机号码 */
    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    private String phonenumber;

    /** 用户性别 */
    private String sex;

    /** 用户头像 */
    private String avatar;

    /** 邮箱是否已验证 */
    private Boolean emailVerified;

    /** 个人简介 */
    private String bio;

    /** 个人网站 */
    private String website;

    /** Github 链接 */
    private String github;

    /** Twitter 链接 */
    private String twitter;

    /** 是否启用两步验证 */
    private Boolean twoFactorEnabled;

    /** 首选语言 */
    private String language;

    /** 时区 */
    private String timezone;

    /** 邮件通知配置（JSON 字符串） */
    private String emailNotifications;

    /** 密码 */
    private String password;

    /** 帐号状态（0正常 1停用） */
    private String status;

    /** 发布文章是否需要审核（true 需要，false 无需） */
    private Boolean articleReviewRequired;

    /** 用户类型（0普通 1管理员 3会员） */
    private String userType;

    /** 会员过期时间 */
    private Date vipExpireTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 租户ID */
    private Long tenantId;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    private Date loginDate;

    /** 部门对象 */
    @TableField(exist = false)
    private SysDept dept;

    /** 角色对象 */
    @TableField(exist = false)
    private List<SysRole> roles;

    /** 角色组 */
    @TableField(exist = false)
    private Long[] roleIds;

    /** 岗位组 */
    @TableField(exist = false)
    private Long[] postIds;

    /** 角色ID */
    @TableField(exist = false)
    private Long roleId;

    public SysUser() {
    }

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 判断是否为会员
     */
    public boolean isVip() {
        return UserTypeEnum.isVip(this.userType);
    }

    /**
     * 判断是否为管理员类型
     */
    public boolean isAdminType() {
        return UserTypeEnum.isAdmin(this.userType);
    }

    /**
     * 判断账号是否正常
     */
    public boolean isNormal() {
        return UserStatusEnum.isNormal(this.status);
    }

    /**
     * 判断会员是否过期
     */
    public boolean isVipExpired() {
        if (!isVip() || vipExpireTime == null) {
            return false;
        }
        return vipExpireTime.before(new Date());
    }

    /**
     * 获取会员剩余天数
     */
    public long getVipRemainingDays() {
        if (!isVip() || vipExpireTime == null || isVipExpired()) {
            return 0;
        }
        long diffInMillies = vipExpireTime.getTime() - System.currentTimeMillis();
        return diffInMillies / (24 * 60 * 60 * 1000);
    }

    /**
     * 设置会员过期时间（从现在开始的天数）
     */
    public void setVipExpireAfterDays(int days) {
        if (days <= 0) {
            this.vipExpireTime = null;
            return;
        }
        LocalDateTime expireDateTime = LocalDateTime.now().plusDays(days);
        this.vipExpireTime = Date.from(expireDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取用户类型描述
     */
    public String getUserTypeDesc() {
        return UserTypeEnum.getByCode(this.userType).getInfo();
    }

    /**
     * 获取用户状态描述
     */
    public String getStatusDesc() {
        return UserStatusEnum.getByCode(this.status).getInfo();
    }
}
