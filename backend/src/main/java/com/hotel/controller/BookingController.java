package com.hotel.controller;

import com.hotel.annotation.Log;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.dto.BookingRequest;
import com.hotel.entity.Booking;
import com.hotel.enums.BookingStatus;
import com.hotel.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "预订管理", description = "房间预订的创建、查询、取消及状态管理")
@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(
        summary = "查询我的预订",
        description = "分页查询当前登录用户的预订记录，可按状态筛选"
    )
    @GetMapping("/api/bookings")
    public Result<PageResult<Booking>> myBookings(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "预订状态：0-待确认 1-已确认 2-已入住 3-已完成 4-已取消")
            @RequestParam(required = false) Integer status) {
        return Result.success(bookingService.myBookings(current, size, status));
    }

    @Operation(
        summary = "查询预订详情",
        description = "根据预订 ID 获取预订详细信息，包含房间和酒店名称"
    )
    @GetMapping("/api/bookings/{id}")
    public Result<Booking> getById(
            @Parameter(description = "预订ID", required = true) @PathVariable Long id) {
        return Result.success(bookingService.getById(id));
    }

    @Operation(
        summary = "创建预订",
        description = "预订指定房间，系统自动计算总价并检查日期冲突",
        responses = {
            @ApiResponse(responseCode = "200", description = "预订创建成功"),
            @ApiResponse(responseCode = "500", description = "房间不可用 / 日期冲突 / 日期校验失败")
        }
    )
    @Log(module = "预订管理", operation = "创建预订")
    @PostMapping("/api/bookings")
    public Result<Void> create(
            @Parameter(description = "预订信息", required = true)
            @Valid @RequestBody BookingRequest request) {
        bookingService.create(request);
        return Result.success();
    }

    @Operation(
        summary = "取消预订",
        description = "取消指定预订，仅待确认和已确认状态可取消",
        responses = {
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "500", description = "预订不存在 / 无权操作 / 状态不允许取消")
        }
    )
    @Log(module = "预订管理", operation = "取消预订")
    @PutMapping("/api/bookings/{id}/cancel")
    public Result<Void> cancel(
            @Parameter(description = "预订ID", required = true) @PathVariable Long id) {
        bookingService.cancel(id);
        return Result.success();
    }

    @Operation(
        summary = "[管理员] 查询所有预订",
        description = "分页查询所有用户的预订记录，支持按用户ID和状态筛选"
    )
    @GetMapping("/api/admin/bookings")
    public Result<PageResult<Booking>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long userId,
            @Parameter(description = "预订状态：0-待确认 1-已确认 2-已入住 3-已完成 4-已取消")
            @RequestParam(required = false) Integer status) {
        return Result.success(bookingService.page(current, size, userId, status));
    }

    @Operation(
        summary = "[管理员] 更新预订状态",
        description = "更新预订状态，需遵循状态流转规则：待确认→已确认→已入住→已完成，待确认/已确认可取消",
        responses = {
            @ApiResponse(responseCode = "200", description = "状态更新成功"),
            @ApiResponse(responseCode = "500", description = "预订不存在 / 状态流转不合法")
        }
    )
    @Log(module = "预订管理", operation = "更新状态")
    @PutMapping("/api/admin/bookings/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "预订ID", required = true) @PathVariable Long id,
            @Parameter(description = "目标状态", required = true) @RequestParam BookingStatus status) {
        bookingService.updateStatus(id, status);
        return Result.success();
    }
}
