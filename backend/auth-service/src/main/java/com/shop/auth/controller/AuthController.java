package com.shop.auth.controller;

import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.LoginResponse;
import com.shop.auth.dto.ProfileRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserVO;
import com.shop.auth.service.AuthService;
import com.shop.common.context.UserContext;
import com.shop.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        return Result.ok(authService.refresh(body.get("refreshToken")));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                               @RequestBody(required = false) Map<String, String> body) {
        String accessToken = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7) : null;
        String refreshToken = body == null ? null : body.get("refreshToken");
        authService.logout(accessToken, refreshToken);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.getMe(UserContext.getUserId()));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody ProfileRequest request) {
        return Result.ok(authService.updateProfile(UserContext.getUserId(), request));
    }
}
