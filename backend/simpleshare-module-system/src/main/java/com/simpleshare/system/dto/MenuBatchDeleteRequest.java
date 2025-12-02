package com.simpleshare.system.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 菜单批量删除请求DTO
 */
public class MenuBatchDeleteRequest {

    /** 菜单ID列表 */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> menuIds;

    public MenuBatchDeleteRequest() {
    }

    public MenuBatchDeleteRequest(List<Long> menuIds) {
        this.menuIds = menuIds;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}