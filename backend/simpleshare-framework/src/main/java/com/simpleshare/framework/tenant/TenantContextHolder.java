package com.simpleshare.framework.tenant;

/**
 * 租户上下文持有者
 *
 * @author SimpleShare
 */
public class TenantContextHolder {
    
    private static final ThreadLocal<String> TENANT_ID_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }
    
    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    public static String getTenantId() {
        return TENANT_ID_HOLDER.get();
    }
    
    /**
     * 清除当前租户ID
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
    }
    
    /**
     * 判断是否存在租户ID
     *
     * @return true存在，false不存在
     */
    public static boolean hasTenantId() {
        return TENANT_ID_HOLDER.get() != null;
    }
}