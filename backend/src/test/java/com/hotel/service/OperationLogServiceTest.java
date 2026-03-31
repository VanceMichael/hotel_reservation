package com.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.PageResult;
import com.hotel.entity.OperationLog;
import com.hotel.mapper.OperationLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OperationLogService 单元测试
 * 覆盖：日志分页查询、条件筛选
 */
@ExtendWith(MockitoExtension.class)
class OperationLogServiceTest {

    @InjectMocks
    private OperationLogService operationLogService;

    @Mock
    private OperationLogMapper operationLogMapper;

    @Test
    @DisplayName("分页查询操作日志")
    void page_shouldReturnLogs() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(
                buildLog(1L, "admin", "用户管理"),
                buildLog(2L, "admin", "酒店管理")
        ));
        mockPage.setTotal(2);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        PageResult<OperationLog> result = operationLogService.page(1, 10, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
    }

    @Test
    @DisplayName("按用户名筛选操作日志")
    void page_filterByUsername_shouldWork() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(buildLog(1L, "admin", "用户管理")));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        PageResult<OperationLog> result = operationLogService.page(1, 10, "admin", null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(operationLogMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("按模块筛选操作日志")
    void page_filterByModule_shouldWork() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(buildLog(1L, "admin", "酒店管理")));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        PageResult<OperationLog> result = operationLogService.page(1, 10, null, "酒店管理");

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("空结果查询")
    void page_emptyResult_shouldReturnEmptyList() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList());
        mockPage.setTotal(0);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        PageResult<OperationLog> result = operationLogService.page(1, 10, "nonexistent", null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    // ==================== 辅助方法 ====================

    private OperationLog buildLog(Long id, String username, String module) {
        OperationLog log = new OperationLog();
        log.setId(id);
        log.setUsername(username);
        log.setModule(module);
        log.setOperation("测试操作");
        log.setMethod("com.hotel.controller.TestController.test");
        log.setParams("{}");
        log.setIp("127.0.0.1");
        log.setDuration(50);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
