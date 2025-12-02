package com.simpleshare.common.core.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多租户Entity基类
 *
 * @author SimpleShare
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;
}