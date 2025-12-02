package com.simpleshare.system.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.entity.SysRole;
import com.simpleshare.common.core.domain.model.PageResponse;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.dto.RoleAdminRequest;
import com.simpleshare.system.dto.RoleBatchDeleteRequest;
import com.simpleshare.system.service.ISysRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理后台 REST API
 */
@RestController
@RequestMapping("/system/admin/roles")
@Validated
public class RoleAdminRestController extends BaseController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ISysRoleService roleService;

    public RoleAdminRestController(ISysRoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 获取角色列表
     */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    public AjaxResult list(@RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                           @RequestParam(value = "keyword", required = false) String keyword) {
        int size = Math.max(pageSize, 1);
        int pageNum = Math.max(page, 1);

        PageHelper.startPage(pageNum, size);
        SysRole query = new SysRole();
        if (StringUtils.isNotBlank(keyword)) {
            query.setRoleName(keyword.trim());
        }
        List<SysRole> roles = roleService.selectRoleList(query);
        PageInfo<SysRole> pageInfo = new PageInfo<>(roles);
        List<Map<String, Object>> items = roles.stream()
            .map(this::buildRoleResponse)
            .collect(Collectors.toList());
        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    public AjaxResult detail(@PathVariable("id") Long id) {
        SysRole role = roleService.selectRoleById(id);
        if (role == null) {
            return AjaxResult.error("角色不存在");
        }
        return AjaxResult.success(buildRoleResponse(role));
    }

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    public AjaxResult create(@Valid @RequestBody RoleAdminRequest request) {
        SysRole role = buildRoleEntity(new SysRole(), request);
        if (!roleService.checkRoleNameUnique(role)) {
            return AjaxResult.error("新增角色失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role)) {
            return AjaxResult.error("新增角色失败，角色权限已存在");
        }
        roleService.insertRole(role);
        SysRole saved = roleService.selectRoleById(role.getRoleId());
        return AjaxResult.success(buildRoleResponse(saved));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    public AjaxResult update(@PathVariable("id") Long id,
                             @Valid @RequestBody RoleAdminRequest request) {
        SysRole existing = roleService.selectRoleById(id);
        if (existing == null) {
            return AjaxResult.error("角色不存在");
        }
        roleService.checkRoleAllowed(existing);
        roleService.checkRoleDataScope(id);
        SysRole role = buildRoleEntity(existing, request);
        role.setRoleId(id);
        if (!roleService.checkRoleNameUnique(role)) {
            return AjaxResult.error("修改角色失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role)) {
            return AjaxResult.error("修改角色失败，角色权限已存在");
        }
        roleService.updateRole(role);
        SysRole refreshed = roleService.selectRoleById(id);
        return AjaxResult.success(buildRoleResponse(refreshed));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    public AjaxResult delete(@PathVariable("id") Long id) {
        roleService.checkRoleAllowed(new SysRole(id));
        roleService.checkRoleDataScope(id);
        int rows = roleService.deleteRoleById(id);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 批量删除角色
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    public AjaxResult batchDelete(@Valid @RequestBody RoleBatchDeleteRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return AjaxResult.error("请选择要删除的角色");
        }
        Long[] ids = request.getIds().stream()
            .map(Long::valueOf)
            .toArray(Long[]::new);
        for (Long id : ids) {
            roleService.checkRoleAllowed(new SysRole(id));
            roleService.checkRoleDataScope(id);
        }
        int rows = roleService.deleteRoleByIds(ids);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("批量删除失败");
    }

    private SysRole buildRoleEntity(SysRole target, RoleAdminRequest request) {
        if (request.getName() != null) {
            target.setRoleName(request.getName());
        }
        if (request.getKey() != null) {
            target.setRoleKey(request.getKey());
        }
        if (request.getSort() != null) {
            target.setRoleSort(request.getSort());
        }
        if (request.getStatus() != null) {
            target.setStatus(mapStatus(request.getStatus()));
        }
        if (request.getRemark() != null) {
            target.setRemark(request.getRemark());
        }
        return target;
    }

    private String mapStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return "0";
        }
        String trimmed = status.trim();
        if ("0".equals(trimmed) || "1".equals(trimmed)) {
            return trimmed;
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("inactive") || normalized.startsWith("disabled")) {
            return "1";
        }
        if (normalized.startsWith("active") || normalized.startsWith("enabled")) {
            return "0";
        }
        return "0";
    }

    private Map<String, Object> buildRoleResponse(SysRole role) {
        Map<String, Object> data = new HashMap<>();
        if (role.getRoleId() != null) {
            data.put("id", String.valueOf(role.getRoleId()));
        }
        data.put("name", role.getRoleName());
        data.put("key", role.getRoleKey());
        data.put("sort", role.getRoleSort());
        data.put("status", mapStatus(role.getStatus()));
        data.put("remark", role.getRemark());
        if (role.getCreateTime() != null) {
            data.put("createTime", role.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        if (role.getUpdateTime() != null) {
            data.put("updateTime", role.getUpdateTime().format(DATE_TIME_FORMATTER));
        }
        return data;
    }
}
