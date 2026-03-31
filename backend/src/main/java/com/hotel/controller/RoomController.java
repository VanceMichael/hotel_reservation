package com.hotel.controller;

import com.hotel.annotation.Log;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.dto.RoomRequest;
import com.hotel.entity.Room;
import com.hotel.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房间管理", description = "房间信息的查询、新增、修改和删除")
@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(
        summary = "查询房间列表",
        description = "分页查询房间列表，支持按酒店ID和状态筛选"
    )
    @GetMapping("/api/rooms")
    public Result<PageResult<Room>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "酒店ID筛选") @RequestParam(required = false) Long hotelId,
            @Parameter(description = "状态：0-不可用 1-可用") @RequestParam(required = false) Integer status) {
        return Result.success(roomService.page(current, size, hotelId, status));
    }

    @Operation(
        summary = "查询房间详情",
        description = "根据房间 ID 获取房间详细信息"
    )
    @GetMapping("/api/rooms/{id}")
    public Result<Room> getById(
            @Parameter(description = "房间ID", required = true) @PathVariable Long id) {
        return Result.success(roomService.getById(id));
    }

    @Operation(
        summary = "查询酒店房间列表",
        description = "获取指定酒店下的所有可用房间"
    )
    @GetMapping("/api/hotels/{hotelId}/rooms")
    public Result<List<Room>> listByHotelId(
            @Parameter(description = "酒店ID", required = true) @PathVariable Long hotelId) {
        return Result.success(roomService.listByHotelId(hotelId));
    }

    @Operation(
        summary = "[管理员] 新增房间",
        description = "为指定酒店创建新房间，需提供房型、价格等必填信息",
        responses = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "500", description = "参数校验失败 / 酒店不存在")
        }
    )
    @Log(module = "房间管理", operation = "新增房间")
    @PostMapping("/api/admin/rooms")
    public Result<Void> create(
            @Parameter(description = "房间信息", required = true)
            @Valid @RequestBody RoomRequest request) {
        Room room = new Room();
        BeanUtils.copyProperties(request, room);
        roomService.create(room);
        return Result.success();
    }

    @Operation(
        summary = "[管理员] 更新房间",
        description = "修改房间信息，可更新房型、价格、状态等",
        responses = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "500", description = "房间不存在 / 参数校验失败")
        }
    )
    @Log(module = "房间管理", operation = "更新房间")
    @PutMapping("/api/admin/rooms/{id}")
    public Result<Void> update(
            @Parameter(description = "房间ID", required = true) @PathVariable Long id,
            @Parameter(description = "房间信息", required = true)
            @Valid @RequestBody RoomRequest request) {
        Room room = new Room();
        BeanUtils.copyProperties(request, room);
        room.setId(id);
        roomService.update(room);
        return Result.success();
    }

    @Operation(
        summary = "[管理员] 删除房间",
        description = "删除指定房间，如有未完成的预订则无法删除",
        responses = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "500", description = "房间不存在 / 存在未完成预订")
        }
    )
    @Log(module = "房间管理", operation = "删除房间")
    @DeleteMapping("/api/admin/rooms/{id}")
    public Result<Void> delete(
            @Parameter(description = "房间ID", required = true) @PathVariable Long id) {
        roomService.delete(id);
        return Result.success();
    }
}
