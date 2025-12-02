package com.simpleshare.system.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.entity.SysUser;
import com.simpleshare.common.core.domain.model.LoginUser;
import com.simpleshare.common.exception.ServiceException;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.web.service.TokenService;
import com.simpleshare.system.dto.UserProfileResponse;
import com.simpleshare.system.dto.UserProfileUpdateRequest;
import com.simpleshare.system.service.ISysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Controller for current user profile operations.
 */
@RestController
@RequestMapping("/users")
@Validated
public class UserProfileController extends BaseController {

    private final ISysUserService userService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public UserProfileController(ISysUserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取当前登录用户的详细资料
     */
    @GetMapping("/me")
    public AjaxResult getCurrentUser() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error("未登录或登录已过期");
        }

        SysUser sysUser = loginUser.getUser();
        if (sysUser == null || sysUser.getUserId() == null) {
            sysUser = userService.selectUserById(loginUser.getUserId());
        }

        if (sysUser == null) {
            return AjaxResult.error("用户不存在");
        }

        return AjaxResult.success(UserProfileResponse.from(sysUser, loginUser));
    }

    /**
     * 更新当前用户的个人资料（不包含用户名、邮箱等敏感字段）
     */
    @PutMapping("/me")
    public AjaxResult updateCurrentUser(@Valid @RequestBody UserProfileUpdateRequest request) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return AjaxResult.error("未登录或登录已过期");
        }

        SysUser sysUser = userService.selectUserById(loginUser.getUserId());
        if (sysUser == null) {
            return AjaxResult.error("用户不存在");
        }

        if (StringUtils.isNotEmpty(request.getNickname())) {
            sysUser.setNickName(request.getNickname());
        }
        sysUser.setBio(request.getBio());
        sysUser.setWebsite(request.getWebsite());
        sysUser.setGithub(request.getGithub());
        sysUser.setTwitter(request.getTwitter());

        if (StringUtils.isNotEmpty(request.getLanguage())) {
            sysUser.setLanguage(request.getLanguage());
        }
        if (StringUtils.isNotEmpty(request.getTimezone())) {
            sysUser.setTimezone(request.getTimezone());
        }

        if (request.getEmailNotifications() != null) {
            try {
                sysUser.setEmailNotifications(objectMapper.writeValueAsString(request.getEmailNotifications()));
            } catch (JsonProcessingException e) {
                throw new ServiceException("保存邮件通知配置失败: " + e.getMessage());
            }
        }

        sysUser.setUpdateBy(loginUser.getUserId());

        userService.updateUserProfile(sysUser);

        // 更新缓存中的用户信息
        loginUser.setUser(sysUser);
        tokenService.setLoginUser(loginUser);

        return AjaxResult.success("更新成功", UserProfileResponse.from(sysUser, loginUser));
    }
}
