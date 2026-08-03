package com.shop.auth.controller;

import com.shop.api.dto.UserDTO;
import com.shop.auth.entity.SysUser;
import com.shop.auth.service.AuthService;
import com.shop.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AuthService authService;

    @GetMapping("/{userId}")
    public Result<UserDTO> getUser(@PathVariable Long userId) {
        SysUser user = authService.getById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setRoles(authService.roleNames(userId));
        return Result.ok(dto);
    }
}

