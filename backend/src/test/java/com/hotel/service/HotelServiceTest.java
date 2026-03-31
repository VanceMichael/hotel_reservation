package com.hotel.service;

import com.hotel.common.BusinessException;
import com.hotel.entity.Hotel;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.HotelMapper;
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
 * HotelService 单元测试
 * 覆盖：酒店 CRUD、删除前活跃预订检查
 */
@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @InjectMocks
    private HotelService hotelService;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private BookingMapper bookingMapper;

    // ==================== create ====================

    @Test
    @DisplayName("创建酒店成功")
    void create_shouldSucceed() {
        Hotel hotel = buildHotel(null, "测试酒店");
        when(hotelMapper.insert(any(Hotel.class))).thenReturn(1);

        assertDoesNotThrow(() -> hotelService.create(hotel));
        verify(hotelMapper).insert(hotel);
    }

    // ==================== update ====================

    @Test
    @DisplayName("更新酒店成功")
    void update_shouldSucceed() {
        Hotel hotel = buildHotel(1L, "测试酒店");
        when(hotelMapper.selectById(1L)).thenReturn(hotel);
        when(hotelMapper.updateById(any(Hotel.class))).thenReturn(1);

        assertDoesNotThrow(() -> hotelService.update(hotel));
        verify(hotelMapper).updateById(hotel);
    }

    @Test
    @DisplayName("更新不存在的酒店应抛出异常")
    void update_hotelNotFound_shouldThrow() {
        Hotel hotel = buildHotel(999L, "不存在的酒店");
        when(hotelMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hotelService.update(hotel));
        assertTrue(ex.getMessage().contains("酒店不存在"));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除无活跃预订的酒店应成功")
    void delete_noActiveBookings_shouldSucceed() {
        when(bookingMapper.countActiveByHotelId(1L)).thenReturn(0);
        when(hotelMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> hotelService.delete(1L));
        verify(hotelMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除有活跃预订的酒店应抛出异常")
    void delete_hasActiveBookings_shouldThrow() {
        when(bookingMapper.countActiveByHotelId(1L)).thenReturn(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hotelService.delete(1L));
        assertTrue(ex.getMessage().contains("5 条未完成的预订"));
        verify(hotelMapper, never()).deleteById(anyLong());
    }

    // ==================== getById ====================

    @Test
    @DisplayName("查询酒店详情")
    void getById_shouldReturnHotel() {
        Hotel hotel = buildHotel(1L, "测试酒店");
        when(hotelMapper.selectById(1L)).thenReturn(hotel);

        Hotel result = hotelService.getById(1L);

        assertNotNull(result);
        assertEquals("测试酒店", result.getName());
    }

    @Test
    @DisplayName("查询不存在的酒店应返回 null")
    void getById_notFound_shouldReturnNull() {
        when(hotelMapper.selectById(999L)).thenReturn(null);

        Hotel result = hotelService.getById(999L);

        assertNull(result);
    }

    // ==================== 辅助方法 ====================

    private Hotel buildHotel(Long id, String name) {
        Hotel hotel = new Hotel();
        hotel.setId(id);
        hotel.setName(name);
        hotel.setAddress("测试地址");
        hotel.setPhone("021-12345678");
        hotel.setRating(new BigDecimal("4.5"));
        hotel.setStatus(1);
        return hotel;
    }
}
