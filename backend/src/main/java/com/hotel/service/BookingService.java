package com.hotel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.dto.BookingRequest;
import com.hotel.entity.Booking;
import com.hotel.entity.Room;
import com.hotel.enums.BookingStatus;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingMapper bookingMapper;
    private final RoomMapper roomMapper;

    public PageResult<Booking> page(int current, int size, Long userId, Integer status) {
        Page<Booking> page = new Page<>(current, size);
        return PageResult.of(bookingMapper.selectPageWithDetail(page, userId, status));
    }

    public PageResult<Booking> myBookings(int current, int size, Integer status) {
        return page(current, size, UserContext.getUserId(), status);
    }

    public Booking getById(Long id) {
        return bookingMapper.selectWithDetail(id);
    }

    public void create(BookingRequest request) {
        Room room = roomMapper.selectById(request.getRoomId());
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException("房间不可用");
        }

        // 校验入住日期不能早于今天
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BusinessException("入住日期不能早于今天");
        }

        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
            request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new BusinessException("入住日期必须早于离店日期");
        }

        // 检查房间在该日期范围内是否有冲突预订
        int conflictCount = bookingMapper.countConflictBookings(
            request.getRoomId(),
            request.getCheckInDate(),
            request.getCheckOutDate()
        );
        if (conflictCount > 0) {
            throw new BusinessException("该房间在所选日期范围内已被预订，请选择其他日期或房间");
        }

        long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setUserId(UserContext.getUserId());
        booking.setRoomId(request.getRoomId());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING.getCode());
        booking.setRemark(request.getRemark());
        bookingMapper.insert(booking);
        log.info("创建预订: userId={}, roomId={}, checkIn={}, checkOut={}",
            booking.getUserId(), booking.getRoomId(),
            request.getCheckInDate(), request.getCheckOutDate());
    }

    public void cancel(Long id) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }
        if (!booking.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException("无权操作");
        }

        BookingStatus currentStatus = BookingStatus.fromCode(booking.getStatus());
        validateStatusTransition(currentStatus, BookingStatus.CANCELLED);

        booking.setStatus(BookingStatus.CANCELLED.getCode());
        bookingMapper.updateById(booking);
        log.info("取消预订: id={}", id);
    }

    public void updateStatus(Long id, BookingStatus newStatus) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        BookingStatus currentStatus = BookingStatus.fromCode(booking.getStatus());
        validateStatusTransition(currentStatus, newStatus);

        booking.setStatus(newStatus.getCode());
        bookingMapper.updateById(booking);
        log.info("更新预订状态: id={}, {} -> {}", id,
            currentStatus.getDescription(), newStatus.getDescription());
    }

    /**
     * 校验状态流转是否合法
     */
    private void validateStatusTransition(BookingStatus current, BookingStatus target) {
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(String.format(
                "状态流转不合法：不能从「%s」变更为「%s」",
                current.getDescription(), target.getDescription()
            ));
        }
    }
}
