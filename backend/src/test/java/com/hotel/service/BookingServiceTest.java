package com.hotel.service;

import com.hotel.common.BusinessException;
import com.hotel.dto.BookingRequest;
import com.hotel.entity.Booking;
import com.hotel.entity.Room;
import com.hotel.enums.BookingStatus;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.util.UserContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BookingService 单元测试
 * 覆盖：日期校验、房间冲突、状态机流转
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private RoomMapper roomMapper;

    private Room availableRoom;

    @BeforeEach
    void setUp() {
        availableRoom = new Room();
        availableRoom.setId(1L);
        availableRoom.setStatus(1);
        availableRoom.setPrice(new BigDecimal("500.00"));

        UserContext.UserInfo userInfo = new UserContext.UserInfo();
        userInfo.setUserId(1L);
        userInfo.setUsername("testuser");
        userInfo.setRole(0);
        UserContext.setUser(userInfo);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== 日期校验 ====================

    @Test
    @DisplayName("入住日期早于今天应抛出异常")
    void create_checkInBeforeToday_shouldThrow() {
        BookingRequest req = buildRequest(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        when(roomMapper.selectById(1L)).thenReturn(availableRoom);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.create(req));
        assertTrue(ex.getMessage().contains("入住日期不能早于今天"));
    }

    @Test
    @DisplayName("入住日期等于离店日期应抛出异常")
    void create_sameDate_shouldThrow() {
        LocalDate date = LocalDate.now().plusDays(5);
        BookingRequest req = buildRequest(date, date);
        when(roomMapper.selectById(1L)).thenReturn(availableRoom);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.create(req));
        assertTrue(ex.getMessage().contains("入住日期必须早于离店日期"));
    }

    @Test
    @DisplayName("入住日期晚于离店日期应抛出异常")
    void create_checkInAfterCheckOut_shouldThrow() {
        BookingRequest req = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(3));
        when(roomMapper.selectById(1L)).thenReturn(availableRoom);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.create(req));
        assertTrue(ex.getMessage().contains("入住日期必须早于离店日期"));
    }

    // ==================== 房间冲突 ====================

    @Test
    @DisplayName("房间日期冲突应抛出异常")
    void create_conflictBooking_shouldThrow() {
        BookingRequest req = buildRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        when(roomMapper.selectById(1L)).thenReturn(availableRoom);
        when(bookingMapper.countConflictBookings(eq(1L), any(), any())).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.create(req));
        assertTrue(ex.getMessage().contains("已被预订"));
    }

    @Test
    @DisplayName("无冲突时应成功创建预订")
    void create_noConflict_shouldSucceed() {
        BookingRequest req = buildRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        when(roomMapper.selectById(1L)).thenReturn(availableRoom);
        when(bookingMapper.countConflictBookings(eq(1L), any(), any())).thenReturn(0);
        when(bookingMapper.insert(any(Booking.class))).thenReturn(1);

        assertDoesNotThrow(() -> bookingService.create(req));
        verify(bookingMapper).insert(any(Booking.class));
    }

    // ==================== 状态机（使用枚举） ====================

    @Test
    @DisplayName("待确认 -> 已确认：合法流转")
    void updateStatus_pendingToConfirmed_shouldSucceed() {
        Booking booking = buildBooking(BookingStatus.PENDING);
        when(bookingMapper.selectById(1L)).thenReturn(booking);
        when(bookingMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> bookingService.updateStatus(1L, BookingStatus.CONFIRMED));
    }

    @Test
    @DisplayName("待确认 -> 已入住：非法流转")
    void updateStatus_pendingToCheckedIn_shouldThrow() {
        Booking booking = buildBooking(BookingStatus.PENDING);
        when(bookingMapper.selectById(1L)).thenReturn(booking);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.updateStatus(1L, BookingStatus.CHECKED_IN));
        assertTrue(ex.getMessage().contains("状态流转不合法"));
    }

    @Test
    @DisplayName("已完成 -> 任何状态：非法流转")
    void updateStatus_completedToAny_shouldThrow() {
        Booking booking = buildBooking(BookingStatus.COMPLETED);
        when(bookingMapper.selectById(1L)).thenReturn(booking);

        assertThrows(BusinessException.class,
                () -> bookingService.updateStatus(1L, BookingStatus.CONFIRMED));
        assertThrows(BusinessException.class,
                () -> bookingService.updateStatus(1L, BookingStatus.PENDING));
    }

    @Test
    @DisplayName("已取消 -> 任何状态：非法流转")
    void updateStatus_cancelledToAny_shouldThrow() {
        Booking booking = buildBooking(BookingStatus.CANCELLED);
        when(bookingMapper.selectById(1L)).thenReturn(booking);

        assertThrows(BusinessException.class,
                () -> bookingService.updateStatus(1L, BookingStatus.PENDING));
    }

    @Test
    @DisplayName("已确认 -> 已取消：合法流转")
    void updateStatus_confirmedToCancelled_shouldSucceed() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED);
        when(bookingMapper.selectById(1L)).thenReturn(booking);
        when(bookingMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> bookingService.updateStatus(1L, BookingStatus.CANCELLED));
    }

    @Test
    @DisplayName("已确认 -> 已入住 -> 已完成：完整流程")
    void updateStatus_fullFlow_shouldSucceed() {
        // 已确认 -> 已入住
        Booking booking1 = buildBooking(BookingStatus.CONFIRMED);
        when(bookingMapper.selectById(1L)).thenReturn(booking1);
        when(bookingMapper.updateById(any())).thenReturn(1);
        assertDoesNotThrow(() -> bookingService.updateStatus(1L, BookingStatus.CHECKED_IN));

        // 已入住 -> 已完成
        Booking booking2 = buildBooking(BookingStatus.CHECKED_IN);
        when(bookingMapper.selectById(2L)).thenReturn(booking2);
        assertDoesNotThrow(() -> bookingService.updateStatus(2L, BookingStatus.COMPLETED));
    }

    // ==================== 房间不可用 ====================

    @Test
    @DisplayName("房间不存在应抛出异常")
    void create_roomNotFound_shouldThrow() {
        BookingRequest req = buildRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        when(roomMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.create(req));
        assertTrue(ex.getMessage().contains("房间不可用"));
    }

    // ==================== 辅助方法 ====================

    private BookingRequest buildRequest(LocalDate checkIn, LocalDate checkOut) {
        BookingRequest req = new BookingRequest();
        req.setRoomId(1L);
        req.setCheckInDate(checkIn);
        req.setCheckOutDate(checkOut);
        req.setRemark("测试");
        return req;
    }

    private Booking buildBooking(BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUserId(1L);
        booking.setRoomId(1L);
        booking.setStatus(status.getCode());
        return booking;
    }
}
