package com.hotel.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.annotation.Log;
import com.hotel.entity.OperationLog;
import com.hotel.mapper.OperationLogMapper;
import com.hotel.util.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LogAspect 单元测试
 * 验证：参数输出为标准 JSON、敏感字段脱敏
 */
@ExtendWith(MockitoExtension.class)
class LogAspectTest {

    @InjectMocks
    private LogAspect logAspect;

    @Mock
    private OperationLogMapper operationLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RequestContextHolder.resetRequestAttributes();
        UserContext.UserInfo userInfo = new UserContext.UserInfo();
        userInfo.setUserId(1L);
        userInfo.setUsername("admin");
        userInfo.setRole(1);
        UserContext.setUser(userInfo);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("日志参数应为合法 JSON 格式")
    void params_shouldBeValidJson() throws Throwable {
        ProceedingJoinPoint point = mockJoinPoint(
                new String[]{"id", "name"},
                new Object[]{1L, "测试酒店"});
        when(operationLogMapper.insert(any())).thenReturn(1);

        logAspect.around(point);

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogMapper).insert(captor.capture());

        String params = captor.getValue().getParams();
        assertDoesNotThrow(() -> objectMapper.readTree(params),
                "params 应为合法 JSON: " + params);

        JsonNode node = objectMapper.readTree(params);
        assertEquals("1", node.get("id").asText());
        assertEquals("测试酒店", node.get("name").asText());
    }

    @Test
    @DisplayName("敏感字段 password 应被脱敏")
    void params_shouldMaskPassword() throws Throwable {
        ProceedingJoinPoint point = mockJoinPoint(
                new String[]{"username", "password"},
                new Object[]{"admin", "123456"});
        when(operationLogMapper.insert(any())).thenReturn(1);

        logAspect.around(point);

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogMapper).insert(captor.capture());

        String params = captor.getValue().getParams();
        JsonNode node = objectMapper.readTree(params);
        assertEquals("***", node.get("password").asText());
        assertFalse(params.contains("123456"));
    }

    @Test
    @DisplayName("复杂对象中的敏感字段应被过滤")
    void params_complexObject_shouldFilterSensitive() throws Throwable {
        TestDto dto = new TestDto();
        dto.username = "admin";
        dto.password = "secret123";
        dto.email = "admin@test.com";

        ProceedingJoinPoint point = mockJoinPoint(
                new String[]{"request"},
                new Object[]{dto});
        when(operationLogMapper.insert(any())).thenReturn(1);

        logAspect.around(point);

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogMapper).insert(captor.capture());

        String params = captor.getValue().getParams();
        assertDoesNotThrow(() -> objectMapper.readTree(params));
        assertFalse(params.contains("secret123"), "密码不应出现在日志中");
    }

    // ==================== 辅助 ====================

    private ProceedingJoinPoint mockJoinPoint(String[] paramNames, Object[] args) throws Throwable {
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Log logAnnotation = mock(Log.class);

        when(point.getSignature()).thenReturn(signature);
        when(point.proceed()).thenReturn(null);
        when(point.getArgs()).thenReturn(args);
        when(signature.getParameterNames()).thenReturn(paramNames);
        when(signature.getDeclaringTypeName()).thenReturn("com.hotel.controller.TestController");
        when(signature.getName()).thenReturn("testMethod");

        java.lang.reflect.Method method = mock(java.lang.reflect.Method.class);
        when(signature.getMethod()).thenReturn(method);
        when(method.getAnnotation(Log.class)).thenReturn(logAnnotation);
        when(logAnnotation.module()).thenReturn("测试");
        when(logAnnotation.operation()).thenReturn("测试操作");

        return point;
    }

    static class TestDto {
        public String username;
        public String password;
        public String email;
    }
}
