package com.hotel.controller;

import com.hotel.annotation.Log;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.dto.HotelRequest;
import com.hotel.entity.Hotel;
import com.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "酒店管理", description = "酒店信息的查询、新增、修改和删除")
@RestController
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(
        summary = "查询酒店列表",
        description = "分页查询酒店列表，支持按名称和状态筛选。普通用户默认只能看到上架酒店"
    )
    @GetMapping("/api/hotels")
    public Result<PageResult<Hotel>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "酒店名称（模糊搜索）") @RequestParam(required = false) String name,
            @Parameter(description = "状态：0-下架 1-上架") @RequestParam(required = false) Integer status) {
        return Result.success(hotelService.page(current, size, name, status));
    }

    @Operation(
        summary = "查询酒店详情",
        description = "根据酒店 ID 获取酒店详细信息"
    )
    @GetMapping("/api/hotels/{id}")
    public Result<Hotel> getById(
            @Parameter(description = "酒店ID", required = true) @PathVariable Long id) {
        return Result.success(hotelService.getById(id));
    }

    @Operation(
        summary = "[管理员] 新增酒店",
        description = "创建新酒店，需提供名称、地址等必填信息",
        responses = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "500", description = "参数校验失败")
        }
    )
    @Log(module = "酒店管理", operation = "新增酒店")
    @PostMapping("/api/admin/hotels")
    public Result<Void> create(
            @Parameter(description = "酒店信息", required = true)
            @Valid @RequestBody HotelRequest request) {
        Hotel hotel = new Hotel();
        BeanUtils.copyProperties(request, hotel);
        hotelService.create(hotel);
        return Result.success();
    }

    @Operation(
        summary = "[管理员] 更新酒店",
        description = "修改酒店信息，可更新名称、地址、状态等",
        responses = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "500", description = "酒店不存在 / 参数校验失败")
        }
    )
    @Log(module = "酒店管理", operation = "更新酒店")
    @PutMapping("/api/admin/hotels/{id}")
    public Result<Void> update(
            @Parameter(description = "酒店ID", required = true) @PathVariable Long id,
            @Parameter(description = "酒店信息", required = true)
            @Valid @RequestBody HotelRequest request) {
        Hotel hotel = new Hotel();
        BeanUtils.copyProperties(request, hotel);
        hotel.setId(id);
        hotelService.update(hotel);
        return Result.success();
    }

    @Operation(
        summary = "[管理员] 删除酒店",
        description = "删除指定酒店，如有未完成的预订则无法删除",
        responses = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "500", description = "酒店不存在 / 存在未完成预订")
        }
    )
    @Log(module = "酒店管理", operation = "删除酒店")
    @DeleteMapping("/api/admin/hotels/{id}")
    public Result<Void> delete(
            @Parameter(description = "酒店ID", required = true) @PathVariable Long id) {
        hotelService.delete(id);
        return Result.success();
    }
}
