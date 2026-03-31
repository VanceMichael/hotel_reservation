package com.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.common.BusinessException;
import com.hotel.entity.User;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 覆盖：用户查询、状态更新、删除（含活跃预订检查）
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BookingMapper bookingMapper;

    // ==================== getById ====================

    @Test
    @DisplayName("查询用户详情应隐藏密码")
    void getById_shouldHidePassword() {
        User user = buildUser(1L, "testuser");
        user.setPassword("hashed_password");
        when(userMapper.selectById(1L)).thenReturn(user);

        User result = userService.getById(1L);

        assertNotNull(result);
        assertNull(result.getPassword(), "密码应被隐藏");
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("查询不存在的用户应返回 null")
    void getById_notFound_shouldReturnNull() {
        when(userMapper.selectById(999L)).thenReturn(null);

        User result = userService.getById(999L);

        assertNull(result);
    }

    // ==================== updateStatus ====================

    @Test
    @DisplayName("更新用户状态成功")
    void updateStatus_shouldSucceed() {
        User user = buildUser(1L, "testuser");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.updateStatus(1L, 0));
        verify(userMapper).updateById(argThat(u -> u.getStatus() == 0));
    }

    @Test
    @DisplayName("更新不存在用户的状态应抛出异常")
    void updateStatus_userNotFound_shouldThrow() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateStatus(999L, 0));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除无活跃预订的用户应成功")
    void delete_noActiveBookings_shouldSucceed() {
        when(bookingMapper.countActiveByUserId(1L)).thenReturn(0);
        when(userMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> userService.delete(1L));
        verify(userMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除有活跃预订的用户应抛出异常")
    void delete_hasActiveBookings_shouldThrow() {
        when(bookingMapper.countActiveByUserId(1L)).thenReturn(3);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.delete(1L));
        assertTrue(ex.getMessage().contains("3 条未完成的预订"));
        verify(userMapper, never()).deleteById(anyLong());
    }

    // ==================== 辅助方法 ====================

    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setStatus(1);
        user.setRole(0);
        return user;
    }
}
