package com.hotel.aspect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotel.annotation.Log;
import com.hotel.entity.OperationLog;
import com.hotel.mapper.OperationLogMapper;
import com.hotel.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogMapper operationLogMapper;

    /** 专用于日志序列化的 ObjectMapper，与 Spring 全局实例隔离 */
    private static final ObjectMapper LOG_MAPPER = createLogMapper();

    /** 敏感字段名集合（小写匹配） */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "pwd", "secret", "token", "credential", "credentials"
    );

    private static ObjectMapper createLogMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Around("@annotation(com.hotel.annotation.Log)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = point.proceed();
        long duration = System.currentTimeMillis() - startTime;

        try {
            saveLog(point, duration);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }

        return result;
    }

    private void saveLog(ProceedingJoinPoint point, long duration) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Log logAnnotation = signature.getMethod().getAnnotation(Log.class);

        OperationLog operationLog = new OperationLog();
        operationLog.setModule(logAnnotation.module());
        operationLog.setOperation(logAnnotation.operation());
        operationLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
        operationLog.setParams(buildSafeParams(signature.getParameterNames(), point.getArgs()));
        operationLog.setDuration((int) duration);

        if (UserContext.getUser() != null) {
            operationLog.setUserId(UserContext.getUserId());
            operationLog.setUsername(UserContext.getUsername());
        } else if ("用户注册".equals(logAnnotation.operation())) {
            extractUsernameFromArgs(point.getArgs(), operationLog);
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            operationLog.setIp(getIpAddress(attributes.getRequest()));
        }

        operationLogMapper.insert(operationLog);
    }

    // ==================== 参数序列化（标准 JSON） ====================

    /**
     * 将方法参数序列化为标准 JSON 字符串，自动脱敏敏感字段。
     */
    private String buildSafeParams(String[] paramNames, Object[] args) {
        try {
            Map<String, Object> paramMap = new LinkedHashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                String name = paramNames[i];
                Object arg = args[i];

                if (isSensitiveField(name)) {
                    paramMap.put(name, "***");
                } else if (arg == null) {
                    paramMap.put(name, null);
                } else if (isPrimitiveOrWrapper(arg.getClass())) {
                    paramMap.put(name, arg);
                } else {
                    paramMap.put(name, sanitizeObject(arg));
                }
            }
            return LOG_MAPPER.writeValueAsString(paramMap);
        } catch (Exception e) {
            log.debug("参数序列化失败", e);
            return "{}";
        }
    }

    /**
     * 将复杂对象转为 Map，过滤敏感字段后交由 Jackson 序列化。
     */
    private Map<String, Object> sanitizeObject(Object obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (isSensitiveField(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(obj);
                // 对嵌套对象只保留 toString，避免深度递归
                if (value != null && !isPrimitiveOrWrapper(value.getClass())
                        && !(value instanceof Collection) && !(value instanceof Map)) {
                    map.put(field.getName(), value.toString());
                } else {
                    map.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            map.put("_error", "序列化失败");
        }
        return map;
    }

    // ==================== 工具方法 ====================

    private boolean isSensitiveField(String fieldName) {
        return fieldName != null && SENSITIVE_FIELDS.contains(fieldName.toLowerCase());
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == Character.class;
    }

    private void extractUsernameFromArgs(Object[] args, OperationLog operationLog) {
        for (Object arg : args) {
            if (arg != null && arg.getClass().getSimpleName().contains("RegisterRequest")) {
                try {
                    Object val = arg.getClass().getMethod("getUsername").invoke(arg);
                    if (val != null) {
                        operationLog.setUsername(val.toString());
                    }
                } catch (Exception e) {
                    log.debug("获取注册用户名失败", e);
                }
                break;
            }
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
