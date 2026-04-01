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

    /**
     * 分页查询预订列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param userId 用户ID，null表示查询所有用户
     * @param status 预订状态，null表示查询所有状态
     * @return 分页结果
     */
    public PageResult<Booking> page(int current, int size, Long userId, Integer status) {
        Page<Booking> page = new Page<>(current, size);
        return PageResult.of(bookingMapper.selectPageWithDetail(page, userId, status));
    }

    /**
     * 查询当前用户的预订列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param status 预订状态，null表示查询所有状态
     * @return 分页结果
     */
    public PageResult<Booking> myBookings(int current, int size, Integer status) {
        return page(current, size, UserContext.getUserId(), status);
    }

    /**
     * 根据ID查询预订详情
     *
     * @param id 预订ID
     * @return 预订详情
     */
    public Booking getById(Long id) {
        return bookingMapper.selectWithDetail(id);
    }

    /**
     * 创建预订
     *
     * @param request 预订请求参数
     * @throws BusinessException 当房间不可用、日期不合法或存在冲突预订时抛出
     */
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

    /**
     * 取消预订
     *
     * @param id 预订ID
     * @throws BusinessException 当预订不存在、无权限或状态流转不合法时抛出
     */
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

    /**
     * 更新预订状态
     * <p>
     * 状态流转时同步更新房间的可用状态：
     * 1. 当状态从已确认(CONFIRMED)变更为已入住(CHECKED_IN)时，自动将房间标记为不可用
     * 2. 当状态从已入住(CHECKED_IN)变更为已完成(COMPLETED)时，自动将房间恢复为可用状态
     * 房间可用性通过双重机制保障：房间本身状态 + 冲突检测SQL过滤已完成和已取消状态的预订。
     * </p>
     *
     * @param id 预订ID
     * @param newStatus 目标状态
     * @throws BusinessException 当预订不存在或状态流转不合法时抛出
     */
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

        Room room = roomMapper.selectById(booking.getRoomId());
        if (room != null) {
            if (currentStatus == BookingStatus.CONFIRMED && newStatus == BookingStatus.CHECKED_IN) {
                room.setStatus(0);
                roomMapper.updateById(room);
                log.info("客人已入住，标记房间为不可用: bookingId={}, roomId={}, roomName={}", id, room.getId(), room.getName());
            }

            if (currentStatus == BookingStatus.CHECKED_IN && newStatus == BookingStatus.COMPLETED) {
                room.setStatus(1);
                roomMapper.updateById(room);
                log.info("预订已完成，释放房间可用状态: bookingId={}, roomId={}, roomName={}, 该房间后续日期可正常预订", id, room.getId(), room.getName());
            }
        }
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
