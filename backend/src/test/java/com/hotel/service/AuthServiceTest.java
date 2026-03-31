package com.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.common.BusinessException;
import com.hotel.dto.LoginRequest;
import com.hotel.dto.RegisterRequest;
import com.hotel.entity.User;
import com.hotel.mapper.UserMapper;
import com.hotel.util.JwtUtil;
import com.hotel.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;


    @Test
    @DisplayName("正确凭据应登录成功并返回 token")
    void login_validCredentials_shouldReturnToken() {
        User user = buildUser("admin", 1, 1);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(jwtUtil.generateToken(eq(1L), eq("admin"), eq(1))).thenReturn("mock-token");

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        Map<String, Object> result = authService.login(req);
        assertEquals("mock-token", result.get("token"));
        assertNotNull(result.get("user"));
    }

    @Test
    @DisplayName("用户不存在应抛出异常")
    void login_userNotFound_shouldThrow() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setUsername("nonexistent");
        req.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(req));
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("密码错误应抛出异常")
    void login_wrongPassword_shouldThrow() {
        User user = buildUser("admin", 1, 1);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong-password");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(req));
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("已禁用账号应抛出异常")
    void login_disabledUser_shouldThrow() {
        User user = buildUser("sunqi", 0, 0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        LoginRequest req = new LoginRequest();
        req.setUsername("sunqi");
        req.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(req));
        assertTrue(ex.getMessage().contains("账号已被禁用"));
    }

    @Test
    @DisplayName("用户名已存在应注册失败")
    void register_duplicateUsername_shouldThrow() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(req));
        assertTrue(ex.getMessage().contains("用户名已存在"));
    }

    @Test
    @DisplayName("新用户名应注册成功")
    void register_newUsername_shouldSucceed() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("123456");
        req.setPhone("13800000000");

        assertDoesNotThrow(() -> authService.register(req));
        verify(userMapper).insert(argThat(user ->
                "newuser".equals(user.getUsername()) && user.getRole() == 0));
    }

    private User buildUser(String username, int role, int status) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode("123456"));
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
