package com.hotel.controller;

import com.hotel.annotation.Log;
import com.hotel.common.Result;
import com.hotel.dto.LoginRequest;
import com.hotel.dto.RegisterRequest;
import com.hotel.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理", description = "用户登录、注册、登出及信息获取")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "用户登录",
        description = "使用用户名和密码登录，成功后返回 JWT Token",
        responses = {
            @ApiResponse(responseCode = "200", description = "登录成功，返回 token 和用户信息"),
            @ApiResponse(responseCode = "500", description = "用户名或密码错误 / 账号已被禁用")
        }
    )
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "登录凭据", required = true)
            @Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(
        summary = "用户注册",
        description = "注册新用户账号，默认为普通用户角色",
        responses = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "500", description = "用户名已存在")
        }
    )
    @Log(module = "认证", operation = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(
            @Parameter(description = "注册信息", required = true)
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Operation(
        summary = "用户登出",
        description = "退出登录（客户端需自行清除 Token）"
    )
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @Operation(
        summary = "获取当前用户信息",
        description = "根据 Token 获取当前登录用户的详细信息",
        responses = {
            @ApiResponse(responseCode = "200", description = "返回用户信息"),
            @ApiResponse(responseCode = "401", description = "未登录或 Token 已过期")
        }
    )
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.success(authService.getUserInfo());
    }
}
