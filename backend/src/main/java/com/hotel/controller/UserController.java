package com.hotel.controller;

import com.hotel.annotation.Log;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.entity.User;
import com.hotel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "[管理员] 用户信息查询、状态管理和删除")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "查询用户列表",
        description = "分页查询所有用户，支持按用户名模糊搜索"
    )
    @GetMapping
    public Result<PageResult<User>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户名（模糊搜索）") @RequestParam(required = false) String username) {
        return Result.success(userService.page(current, size, username));
    }

    @Operation(
        summary = "查询用户详情",
        description = "根据用户 ID 获取用户详细信息"
    )
    @GetMapping("/{id}")
    public Result<User> getById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(
        summary = "更新用户状态",
        description = "启用或禁用用户账号",
        responses = {
            @ApiResponse(responseCode = "200", description = "状态更新成功"),
            @ApiResponse(responseCode = "500", description = "用户不存在")
        }
    )
    @Log(module = "用户管理", operation = "更新状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态：0-禁用 1-启用", required = true) @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(
        summary = "删除用户",
        description = "删除指定用户，如有未完成的预订则无法删除",
        responses = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "500", description = "用户不存在 / 存在未完成预订")
        }
    )
    @Log(module = "用户管理", operation = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
