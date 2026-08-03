package com.shop.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.LoginResponse;
import com.shop.auth.dto.ProfileRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserVO;
import com.shop.auth.entity.SysMenu;
import com.shop.auth.entity.SysRole;
import com.shop.auth.entity.SysRoleMenu;
import com.shop.auth.entity.SysUser;
import com.shop.auth.entity.SysUserRole;
import com.shop.auth.mapper.SysMenuMapper;
import com.shop.auth.mapper.SysRoleMapper;
import com.shop.auth.mapper.SysRoleMenuMapper;
import com.shop.auth.mapper.SysUserMapper;
import com.shop.auth.mapper.SysUserRoleMapper;
import com.shop.common.cache.RedisCache;
import com.shop.common.exception.BizException;
import com.shop.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisCache redisCache;

    @Transactional
    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BizException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null || request.getNickname().isBlank()
                ? request.getUsername() : request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        userMapper.insert(user);

        SysRole userRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, "USER"));
        if (userRole != null) {
            SysUserRole userRoleLink = new SysUserRole();
            userRoleLink.setUserId(user.getId());
            userRoleLink.setRoleId(userRole.getId());
            userRoleMapper.insert(userRoleLink);
        }
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(403, "账号已被禁用");
        }
        List<String> roles = findRoles(user.getId());
        List<String> permissions = findPermissions(user.getId());
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getUsername());
        cacheToken(accessToken, "auth:token:", 30);
        cacheToken(refreshToken, "auth:refresh:", 7 * 24);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(30 * 60L);
        response.setUser(buildUserVO(user, roles, permissions));
        return response;
    }

    public LoginResponse refresh(String refreshToken) {
        Claims claims = jwtUtil.parse(refreshToken);
        if (!"refresh".equals(claims.get("type"))) {
            throw new BizException(401, "无效的刷新令牌");
        }
        String jti = claims.getId();
        String cached = redisCache.get("auth:refresh:" + jti);
        if (cached == null || !cached.equals(claims.getSubject())) {
            throw new BizException(401, "刷新令牌已失效");
        }
        redisCache.delete("auth:refresh:" + jti);
        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        List<String> roles = findRoles(userId);
        List<String> permissions = findPermissions(userId);
        String accessToken = jwtUtil.createAccessToken(userId, user.getUsername(), roles);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, user.getUsername());
        cacheToken(accessToken, "auth:token:", 30);
        cacheToken(newRefreshToken, "auth:refresh:", 7 * 24);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(30 * 60L);
        response.setUser(buildUserVO(user, roles, permissions));
        return response;
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            removeToken(accessToken, "auth:token:");
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            removeToken(refreshToken, "auth:refresh:");
        }
    }

    public UserVO getMe(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return buildUserVO(user, findRoles(userId), findPermissions(userId));
    }

    @Transactional
    public UserVO updateProfile(Long userId, ProfileRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
        return buildUserVO(user, findRoles(userId), findPermissions(userId));
    }

    private void cacheToken(String token, String prefix, int expireHours) {
        Claims claims = jwtUtil.parse(token);
        long ttlSeconds = Duration.between(new Date().toInstant(), claims.getExpiration().toInstant()).getSeconds();
        if (ttlSeconds <= 0) {
            ttlSeconds = expireHours * 3600L;
        }
        redisCache.set(prefix + claims.getId(), claims.getSubject(), ttlSeconds);
    }

    private void removeToken(String token, String prefix) {
        try {
            Claims claims = jwtUtil.parse(token);
            redisCache.delete(prefix + claims.getId());
        } catch (Exception ignored) {
            // 过期或非法令牌无需清理
        }
    }

    private List<String> findRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream().map(SysRole::getCode).collect(Collectors.toList());
    }

    private List<String> findPermissions(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = menuIdsOfRoles(roleIds);
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .isNotNull(SysMenu::getPermissionCode)
                        .in(SysMenu::getId, menuIds))
                .stream().map(SysMenu::getPermissionCode).distinct().collect(Collectors.toList());
    }

    private List<Long> menuIdsOfRoles(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
    }

    private UserVO buildUserVO(SysUser user, List<String> roles, List<String> permissions) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        return vo;
    }

    public SysUser getByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public void updateRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            roleIds.forEach(roleId -> {
                SysUserRole link = new SysUserRole();
                link.setUserId(userId);
                link.setRoleId(roleId);
                userRoleMapper.insert(link);
            });
        }
    }

    public List<String> roleNames(Long userId) {
        return findRoles(userId);
    }

    public SysUser getById(Long userId) {
        return userMapper.selectById(userId);
    }

    public String roleNamesJoined(Long userId) {
        return String.join(",", findRoles(userId));
    }

    public List<SysRole> allRoles() {
        return roleMapper.selectList(null);
    }
}
