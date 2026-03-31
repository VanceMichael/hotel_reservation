package com.hotel.controller;

import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.entity.OperationLog;
import com.hotel.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志", description = "[管理员] 系统操作日志查询")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(
        summary = "查询操作日志",
        description = "分页查询系统操作日志，支持按用户名和模块筛选"
    )
    @GetMapping
    public Result<PageResult<OperationLog>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "操作人用户名") @RequestParam(required = false) String username,
            @Parameter(description = "模块名称") @RequestParam(required = false) String module) {
        return Result.success(operationLogService.page(current, size, username, module));
    }
}
