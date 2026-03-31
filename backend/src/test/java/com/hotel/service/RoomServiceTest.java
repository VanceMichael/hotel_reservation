package com.hotel.service;

import com.hotel.common.BusinessException;
import com.hotel.entity.Room;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.RoomMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RoomService 单元测试
 * 覆盖：房间 CRUD、删除前活跃预订检查
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private BookingMapper bookingMapper;

    // ==================== create ====================

    @Test
    @DisplayName("创建房间成功")
    void create_shouldSucceed() {
        Room room = buildRoom(null, "豪华大床房");
        when(roomMapper.insert(any(Room.class))).thenReturn(1);

        assertDoesNotThrow(() -> roomService.create(room));
        verify(roomMapper).insert(room);
    }

    // ==================== update ====================

    @Test
    @DisplayName("更新房间成功")
    void update_shouldSucceed() {
        Room room = buildRoom(1L, "豪华大床房");
        when(roomMapper.selectById(1L)).thenReturn(room);
        when(roomMapper.updateById(any(Room.class))).thenReturn(1);

        assertDoesNotThrow(() -> roomService.update(room));
        verify(roomMapper).updateById(room);
    }

    @Test
    @DisplayName("更新不存在的房间应抛出异常")
    void update_roomNotFound_shouldThrow() {
        Room room = buildRoom(999L, "不存在的房间");
        when(roomMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> roomService.update(room));
        assertTrue(ex.getMessage().contains("房间不存在"));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除无活跃预订的房间应成功")
    void delete_noActiveBookings_shouldSucceed() {
        when(bookingMapper.countActiveByRoomId(1L)).thenReturn(0);
        when(roomMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> roomService.delete(1L));
        verify(roomMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除有活跃预订的房间应抛出异常")
    void delete_hasActiveBookings_shouldThrow() {
        when(bookingMapper.countActiveByRoomId(1L)).thenReturn(2);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> roomService.delete(1L));
        assertTrue(ex.getMessage().contains("2 条未完成的预订"));
        verify(roomMapper, never()).deleteById(anyLong());
    }

    // ==================== getById ====================

    @Test
    @DisplayName("查询房间详情")
    void getById_shouldReturnRoom() {
        Room room = buildRoom(1L, "豪华大床房");
        when(roomMapper.selectWithHotel(1L)).thenReturn(room);

        Room result = roomService.getById(1L);

        assertNotNull(result);
        assertEquals("豪华大床房", result.getName());
    }

    // ==================== 辅助方法 ====================

    private Room buildRoom(Long id, String name) {
        Room room = new Room();
        room.setId(id);
        room.setName(name);
        room.setHotelId(1L);
        room.setRoomType("豪华房");
        room.setPrice(new BigDecimal("888.00"));
        room.setCapacity(2);
        room.setStatus(1);
        return room;
    }
}
