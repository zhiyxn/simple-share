package com.simpleshare.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 租户信息表 sys_tenant
 *
 * @author SimpleShare
 */
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 租户ID */
    @TableId(type = IdType.AUTO)
    private Long tenantId;

    /** 租户编号 */
    @NotBlank(message = "租户编号不能为空")
    @Size(min = 0, max = 30, message = "租户编号长度不能超过30个字符")
    private String tenantCode;

    /** 租户名称 */
    @NotBlank(message = "租户名称不能为空")
    @Size(min = 0, max = 50, message = "租户名称长度不能超过50个字符")
    private String tenantName;

    /** 联系人 */
    @Size(min = 0, max = 20, message = "联系人长度不能超过20个字符")
    private String contactUserName;

    /** 联系电话 */
    @Size(min = 0, max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    /** 企业名称 */
    @Size(min = 0, max = 50, message = "企业名称长度不能超过50个字符")
    private String companyName;

    /** 统一社会信用代码 */
    @Size(min = 0, max = 200, message = "统一社会信用代码长度不能超过200个字符")
    private String licenseNumber;

    /** 地址 */
    @Size(min = 0, max = 200, message = "地址长度不能超过200个字符")
    private String address;

    /** 域名 */
    @Size(min = 0, max = 200, message = "域名长度不能超过200个字符")
    private String domain;

    /** 企业简介 */
    @Size(min = 0, max = 1000, message = "企业简介长度不能超过1000个字符")
    private String intro;

    /** 备注 */
    @Size(min = 0, max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    /** 租户套餐编号 */
    private Long packageId;

    /** 过期时间 */
    private java.util.Date expireTime;

    /** 用户数量（-1不限制） */
    private Long accountCount;

    /** 租户状态（0正常 1停用） */
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getContactUserName() {
        return contactUserName;
    }

    public void setContactUserName(String contactUserName) {
        this.contactUserName = contactUserName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public java.util.Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(java.util.Date expireTime) {
        this.expireTime = expireTime;
    }

    public Long getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(Long accountCount) {
        this.accountCount = accountCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "SysTenant{" +
                "tenantId=" + tenantId +
                ", tenantCode='" + tenantCode + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", contactUserName='" + contactUserName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", companyName='" + companyName + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", address='" + address + '\'' +
                ", domain='" + domain + '\'' +
                ", intro='" + intro + '\'' +
                ", remark='" + remark + '\'' +
                ", packageId=" + packageId +
                ", expireTime=" + expireTime +
                ", accountCount=" + accountCount +
                ", status='" + status + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}