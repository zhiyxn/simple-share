package com.simpleshare.system.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.entity.SysRole;
import com.simpleshare.common.core.domain.entity.SysUser;
import com.simpleshare.common.core.domain.model.PageResponse;
import com.simpleshare.common.enums.UserTypeEnum;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.dto.ResetPasswordResponse;
import com.simpleshare.system.dto.UserAdminRequest;
import com.simpleshare.system.dto.UserAdminResponse;
import com.simpleshare.system.dto.UserBatchDeleteRequest;
import com.simpleshare.system.dto.UserBatchStatusRequest;
import com.simpleshare.system.dto.UserStatusUpdateRequest;
import com.simpleshare.system.service.ISysRoleService;
import com.simpleshare.system.service.ISysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户管理后台REST API
 */
@RestController
@RequestMapping("/system/admin/users")
@Validated
public class UserAdminRestController extends BaseController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ISysUserService userService;
    private final ISysRoleService roleService;

    public UserAdminRestController(ISysUserService userService, ISysRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * 获取用户列表
     */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    public AjaxResult list(@RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                           @RequestParam(value = "limit", required = false) Integer limit,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "role", required = false) String role,
                           @RequestParam(value = "status", required = false) String status) {
        int size = limit != null ? limit : pageSize;
        if (size <= 0) {
            size = 10;
        }
        int pageNum = Math.max(page, 1);

        PageHelper.startPage(pageNum, size);

        SysUser query = new SysUser();
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId != null) {
            query.setTenantId(tenantId);
        }
        if (StringUtils.isNotBlank(search)) {
            query.setUserName(search.trim());
        }
        if (StringUtils.isNotBlank(status)) {
            query.setStatus(mapStatusToCode(status));
        }
        List<SysUser> users = userService.selectUserList(query);
        if (StringUtils.isNotBlank(role)) {
            String normalizedRole = role.toLowerCase(Locale.ROOT).trim();
            users = users.stream()
                .filter(user -> userMatchesRole(user, normalizedRole))
                .collect(Collectors.toList());
        }
        PageInfo<SysUser> pageInfo = new PageInfo<>(users);
        List<UserAdminResponse> items = users.stream()
            .map(this::enrichUserWithRoles)
            .map(UserAdminResponse::from)
            .collect(Collectors.toList());

        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    public AjaxResult detail(@PathVariable("id") Long id) {
        SysUser user = userService.selectUserById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        return AjaxResult.success(UserAdminResponse.from(enrichUserWithRoles(user)));
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    public AjaxResult create(@Valid @RequestBody UserAdminRequest request) {
        SysUser user = buildUserEntity(new SysUser(), request);
        if (!userService.checkUserNameUnique(user)) {
            return AjaxResult.error("新增用户失败，用户名已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return AjaxResult.error("用户不存在");
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return AjaxResult.error("手机号已存在");
        }
        if (StringUtils.isEmpty(request.getPassword())) {
            String generated = generatePassword(12);
            user.setPassword(SecurityUtils.encryptPassword(generated));
            AjaxResult result = doInsertUser(user, request.getRole());
            if (result.isSuccess()) {
                result.put("password", generated);
            }
            return result;
        }
        user.setPassword(SecurityUtils.encryptPassword(request.getPassword()));
        return doInsertUser(user, request.getRole());
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public AjaxResult update(@PathVariable("id") Long id,
                             @Valid @RequestBody UserAdminRequest request) {
        SysUser existing = userService.selectUserById(id);
        if (existing == null) {
            return AjaxResult.error("用户不存在");
        }
        SysUser user = buildUserEntity(existing, request);
        user.setUserId(id);
        if (StringUtils.isNotEmpty(request.getPassword())) {
            user.setPassword(SecurityUtils.encryptPassword(request.getPassword()));
        }
        if (!userService.checkUserNameUnique(user)) {
            return AjaxResult.error("新增用户失败，用户名已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return AjaxResult.error("用户不存在");
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return AjaxResult.error("手机号已存在");
        }
        userService.checkUserDataScope(id);
        applyRole(user, request.getRole());
        int rows = userService.updateUser(user);
        if (rows > 0) {
            SysUser refreshed = userService.selectUserById(id);
            return AjaxResult.success(UserAdminResponse.from(enrichUserWithRoles(refreshed)));
        }
        return AjaxResult.error("失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    public AjaxResult delete(@PathVariable("id") Long id) {
        userService.checkUserAllowed(new SysUser(id));
        userService.checkUserDataScope(id);
        int rows = userService.deleteUserById(id);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 批量删除用户
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    public AjaxResult batchDelete(@Valid @RequestBody UserBatchDeleteRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return AjaxResult.error("用户不存在");
        }
        Long[] ids = request.getIds().stream()
            .map(Long::valueOf)
            .toArray(Long[]::new);
        for (Long id : ids) {
            userService.checkUserAllowed(new SysUser(id));
            userService.checkUserDataScope(id);
        }
        int rows = userService.deleteUserByIds(ids);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("批量删除失败");
    }

    /**
     * 状态修改
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public AjaxResult updateStatus(@PathVariable("id") Long id,
                                   @Valid @RequestBody UserStatusUpdateRequest request) {
        SysUser user = userService.selectUserById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        user.setStatus(mapStatusToCode(request.getStatus()));
        userService.updateUserStatus(user);
        SysUser refreshed = userService.selectUserById(id);
        return AjaxResult.success(UserAdminResponse.from(enrichUserWithRoles(refreshed)));
    }

    /**
     * 批量状态修改
     */
    @PostMapping("/batch/status")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public AjaxResult batchUpdateStatus(@Valid @RequestBody UserBatchStatusRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return AjaxResult.error("用户不存在");
        }
        String statusCode = mapStatusToCode(request.getStatus());
        for (String idStr : request.getIds()) {
            Long id = Long.valueOf(idStr);
            SysUser user = userService.selectUserById(id);
            if (user == null) {
                continue;
            }
            user.setStatus(statusCode);
            userService.updateUserStatus(user);
        }
        return AjaxResult.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    public AjaxResult resetPassword(@PathVariable("id") Long id) {
        SysUser user = userService.selectUserById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        String newPassword = generatePassword(12);
        userService.resetUserPwd(user.getUserName(), SecurityUtils.encryptPassword(newPassword));
        return AjaxResult.success(new ResetPasswordResponse(newPassword));
    }

    /**
     * 设置用户为VIP
     */
    @PostMapping("/{id}/set-vip")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public AjaxResult setUserVip(@PathVariable("id") Long id,
                                 @RequestParam(value = "days", defaultValue = "30") int days) {
        SysUser user = userService.selectUserById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        userService.setUserVip(id, days);
        SysUser refreshed = userService.selectUserById(id);
        return AjaxResult.success(UserAdminResponse.from(enrichUserWithRoles(refreshed)));
    }

    /**
     * 取消用户VIP
     */
    @PostMapping("/{id}/cancel-vip")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public AjaxResult cancelUserVip(@PathVariable("id") Long id) {
        SysUser user = userService.selectUserById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        userService.cancelUserVip(id);
        SysUser refreshed = userService.selectUserById(id);
        return AjaxResult.success(UserAdminResponse.from(enrichUserWithRoles(refreshed)));
    }

    /**
     * 延长用户VIP
     */
    @PostMapping("/{id}/extend-vip")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public AjaxResult extendUserVip(@PathVariable("id") Long id,
                                    @RequestParam(value = "days", defaultValue = "30") int days) {
        SysUser user = userService.selectUserById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        userService.extendUserVip(id, days);
        SysUser refreshed = userService.selectUserById(id);
        return AjaxResult.success(UserAdminResponse.from(enrichUserWithRoles(refreshed)));
    }

    /**
     * 获取即将过期的VIP用户列表
     */
    @GetMapping("/expiring-vip")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    public AjaxResult getExpiringVipUsers(@RequestParam(value = "days", defaultValue = "7") int days,
                                          @RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<SysUser> users = userService.selectExpiringVipUsers(days);
        PageInfo<SysUser> pageInfo = new PageInfo<>(users);
        List<UserAdminResponse> items = users.stream()
            .map(this::enrichUserWithRoles)
            .map(UserAdminResponse::from)
            .collect(Collectors.toList());
        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * 获取已过期的VIP用户列表
     */
    @GetMapping("/expired-vip")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    public AjaxResult getExpiredVipUsers(@RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<SysUser> users = userService.selectExpiredVipUsers();
        PageInfo<SysUser> pageInfo = new PageInfo<>(users);
        List<UserAdminResponse> items = users.stream()
            .map(this::enrichUserWithRoles)
            .map(UserAdminResponse::from)
            .collect(Collectors.toList());
        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * 处理过期VIP用户（批量降级）
     */
    @PostMapping("/process-expired-vip")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public AjaxResult processExpiredVipUsers() {
        int processedCount = userService.processExpiredVipUsers();
        return AjaxResult.success("已处理 " + processedCount + " 个过期VIP用户");
    }

    private AjaxResult doInsertUser(SysUser user, String roleKey) {
        applyRole(user, roleKey);
        int rows = userService.insertUser(user);
        if (rows > 0) {
            SysUser saved = userService.selectUserById(user.getUserId());
            return AjaxResult.success(UserAdminResponse.from(enrichUserWithRoles(saved)));
        }
        return AjaxResult.error("失败");
    }

    private SysUser buildUserEntity(SysUser target, UserAdminRequest request) {
        target.setUserName(request.getUsername());
        target.setNickName(Optional.ofNullable(request.getNickname()).orElse(request.getUsername()));
        target.setEmail(request.getEmail());
        target.setPhonenumber(request.getPhone());
        target.setAvatar(request.getAvatar());
        target.setRemark(request.getRemark());
        target.setStatus(mapStatusToCode(request.getStatus()));
        if (request.getArticleReviewRequired() != null) {
            target.setArticleReviewRequired(request.getArticleReviewRequired());
        } else if (target.getArticleReviewRequired() == null) {
            target.setArticleReviewRequired(Boolean.TRUE);
        }

        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId != null) {
            target.setTenantId(tenantId);
        }

        String type = request.getUserType();
        if (StringUtils.isNotEmpty(type)) {
            String norm = type.trim().toLowerCase(Locale.ROOT);
            if ("3".equals(norm) || "vip".equals(norm) || "member".equals(norm)) {
                target.setUserType(UserTypeEnum.VIP.getCode());
            } else if ("1".equals(norm) || "admin".equals(norm) || "administrator".equals(norm) || "manager".equals(norm)) {
                target.setUserType(UserTypeEnum.ADMIN.getCode());
            } else if ("0".equals(norm) || "user".equals(norm) || "normal".equals(norm)) {
                target.setUserType(UserTypeEnum.NORMAL.getCode());
            } else {
                target.setUserType(UserTypeEnum.NORMAL.getCode());
            }
        } else if (target.getUserType() == null) {
            target.setUserType(UserTypeEnum.NORMAL.getCode());
        }

        if (UserTypeEnum.isVip(target.getUserType())) {
            if (StringUtils.isNotEmpty(request.getVipExpireTime())) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(request.getVipExpireTime(), DATE_TIME_FORMATTER);
                    target.setVipExpireTime(java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                } catch (Exception ignore) {
                    // ignore parse errors and keep existing value
                }
            }
        } else {
            target.setVipExpireTime(null);
        }

        return target;
    }

    private void applyRole(SysUser user, String roleKey) {
        Long[] roleIds = resolveRoleIds(roleKey);
        user.setRoleIds(roleIds);
        if (user.getUserId() != null && roleIds.length > 0) {
            userService.insertUserAuth(user.getUserId(), roleIds);
        }
    }

    private Long[] resolveRoleIds(String roleKey) {
        if (StringUtils.isBlank(roleKey)) {
            return new Long[0];
        }
        // 直接根据前端传递的角色值精确匹配数据库中的角色
        List<SysRole> roles = roleService.selectRoleAll();
        return roles.stream()
            .filter(role -> roleKey.equals(role.getRoleKey()))
            .sorted(Comparator.comparing(SysRole::getRoleSort, Comparator.nullsLast(Integer::compareTo)))
            .limit(1)
            .map(SysRole::getRoleId)
            .toArray(Long[]::new);
    }



    private boolean userMatchesRole(SysUser user, String normalizedRole) {
        SysUser enriched = enrichUserWithRoles(user);
        String role = UserAdminResponse.from(enriched).getRole();
        return normalizedRole.equalsIgnoreCase(role);
    }

    private SysUser enrichUserWithRoles(SysUser user) {
        if (user.getUserId() != null) {
            List<SysRole> roles = roleService.selectRolesByUserId(user.getUserId());
            user.setRoles(roles);
        }
        return user;
    }

    private String mapStatusToCode(String status) {
        if (StringUtils.isBlank(status)) {
            return "0";
        }
        String normalized = status.toLowerCase(Locale.ROOT).trim();
        if (normalized.startsWith("active")) {
            return "0";
        }
        if (normalized.startsWith("suspend")) {
            return "1";
        }
        if (normalized.startsWith("pending")) {
            return "0";
        }
        return "1";
    }

    private String generatePassword(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(PASSWORD_ALPHABET.length());
            builder.append(PASSWORD_ALPHABET.charAt(index));
        }
        return builder.toString();
    }
}

