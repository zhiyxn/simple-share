package com.simpleshare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 数据层
 *
 * @author SimpleShare
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据条件分页查询用户列表
     *
     * @param sysUser 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUserList(SysUser sysUser);

    /**
     * 根据条件分页查询已配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectAllocatedList(SysUser user);

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUnallocatedList(SysUser user);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    public SysUser selectUserByUserName(@Param("userName") String userName, @Param("tenantId") Long tenantId);

    /**
     * 通过邮箱查询用户
     * @param email 邮箱
     * @param tenantId 租户ID
     * @return 用户对象
     */
    public SysUser selectUserByEmail(@Param("email") String email, @Param("tenantId") Long tenantId);

    /**
     * 通过手机号查询用户
     *
     * @param phone 手机号
     * @param tenantId 租户ID
     * @return 用户信息
     */
    public SysUser selectUserByPhone(@Param("phone") String phone, @Param("tenantId") Long tenantId);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public SysUser selectUserById(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public int insertUser(SysUser user);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public int updateUser(SysUser user);

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    public int updateUserAvatar(@Param("userName") String userName, @Param("avatar") String avatar);

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    public int resetUserPwd(@Param("userName") String userName, @Param("password") String password);

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    public int deleteUserByIds(Long[] userIds);

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    public int checkUserNameUnique(@Param("userName") String userName, @Param("tenantId") Long tenantId);

    /**
     * 校验手机号码是否唯一
     *
     * @param phonenumber 手机号码
     * @return 结果
     */
    public int checkPhoneUnique(@Param("phonenumber") String phonenumber, @Param("tenantId") Long tenantId);

    /**
     * 校验email是否唯一
     *
     * @param email 用户邮箱
     * @return 结果
     */
    public int checkEmailUnique(@Param("email") String email, @Param("tenantId") Long tenantId);

    /**
     * 查询即将过期的会员用户
     *
     * @param days 提前天数
     * @return 用户列表
     */
    public List<SysUser> selectExpiringVipUsers(@Param("days") int days, @Param("tenantId") Long tenantId);

    /**
     * 查询已过期的会员用户
     *
     * @return 用户列表
     */
    public List<SysUser> selectExpiredVipUsers(@Param("tenantId") Long tenantId);

    /**
     * 根据用户ID集合批量查询用户
     *
     * @param userIds  用户ID集合
     * @param tenantId 租户ID
     * @return 用户列表
     */
    List<SysUser> selectUsersByIds(@Param("userIds") List<Long> userIds, @Param("tenantId") Long tenantId);
}
