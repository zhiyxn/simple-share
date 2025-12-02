package com.simpleshare.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 角色和部门关联 sys_role_dept
 *
 * @author SimpleShare
 */
@TableName("sys_role_dept")
public class SysRoleDept {
    /** 角色ID */
    private Long roleId;

    /** 部门ID */
    private Long deptId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    @Override
    public String toString() {
        return "SysRoleDept{" +
                "roleId=" + roleId +
                ", deptId=" + deptId +
                '}';
    }
}