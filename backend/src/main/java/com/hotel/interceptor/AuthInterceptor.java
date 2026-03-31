package com.hotel.interceptor;

import com.hotel.common.BusinessException;
import com.hotel.util.JwtUtil;
import com.hotel.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或token已过期");
        }

        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "token无效");
        }

        UserContext.UserInfo userInfo = new UserContext.UserInfo();
        userInfo.setUserId(jwtUtil.getUserId(token));
        userInfo.setUsername(jwtUtil.getUsername(token));
        userInfo.setRole(jwtUtil.getRole(token));
        UserContext.setUser(userInfo);

        // 管理员接口权限校验
        String uri = request.getRequestURI();
        if (uri.contains("/admin/") && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
