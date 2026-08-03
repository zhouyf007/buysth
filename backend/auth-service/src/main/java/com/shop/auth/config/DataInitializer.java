package com.shop.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.auth.entity.SysRole;
import com.shop.auth.entity.SysUser;
import com.shop.auth.entity.SysUserRole;
import com.shop.auth.mapper.SysRoleMapper;
import com.shop.auth.mapper.SysUserMapper;
import com.shop.auth.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userMapper.selectCount(null) != null && userMapper.selectCount(null) > 0) {
            return;
        }
        createUser("admin", "123456", "超级管理员", "SUPER_ADMIN");
        createUser("operator", "123456", "运营小李", "OPERATOR");
        createUser("user", "123456", "演示用户", "USER");
        log.info("Demo accounts created: admin/123456, operator/123456, user/123456");
    }

    private void createUser(String username, String rawPassword, String nickname, String roleCode) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setStatus(1);
        userMapper.insert(user);

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        if (role != null) {
            SysUserRole link = new SysUserRole();
            link.setUserId(user.getId());
            link.setRoleId(role.getId());
            userRoleMapper.insert(link);
        }
    }
}

