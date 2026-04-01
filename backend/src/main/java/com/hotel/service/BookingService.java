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

/**
 * 预订服务类
 * 提供预订相关的业务逻辑处理，包括创建、取消、状态更新等操作
 */
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
     * @param size 每页数量
     * @param userId 用户ID（可选，用于筛选特定用户的预订）
     * @param status 预订状态（可选，用于筛选特定状态的预订）
     * @return 分页结果
     */
    public PageResult<Booking> page(int current, int size, Long userId, Integer status) {
        Page<Booking> page = new Page<>(current, size);
        return PageResult.of(bookingMapper.selectPageWithDetail(page, userId, status));
    }

    /**
     * 查询当前登录用户的预订列表
     * 
     * @param current 当前页码
     * @param size 每页数量
     * @param status 预订状态（可选）
     * @return 分页结果
     */
    public PageResult<Booking> myBookings(int current, int size, Integer status) {
        return page(current, size, UserContext.getUserId(), status);
    }

    /**
     * 根据预订ID查询预订详情
     * 
     * @param id 预订ID
     * @return 预订详情对象
     */
    public Booking getById(Long id) {
        return bookingMapper.selectWithDetail(id);
    }

    /**
     * 创建新预订
     * 会进行房间可用性校验、日期校验和冲突预订检查
     * 
     * @param request 预订请求参数
     * @throws BusinessException 当房间不可用、日期无效或存在冲突预订时抛出
     */
    public void create(BookingRequest request) {
        Room room = roomMapper.selectById(request.getRoomId());
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException("房间不可用");
        }

        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BusinessException("入住日期不能早于今天");
        }

        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
            request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new BusinessException("入住日期必须早于离店日期");
        }

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
     * 只有待确认和已确认状态的预订可以被取消
     * 
     * @param id 预订ID
     * @throws BusinessException 当预订不存在、无权操作或状态不允许取消时抛出
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
     * 遵循预订状态流转规则：待确认→已确认→已入住→已完成，待确认/已确认可取消
     * 当状态从已入住变更为已完成时，会正确释放房间的可用状态
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
    }

    /**
     * 校验预订状态流转是否合法
     * 
     * @param current 当前状态
     * @param target 目标状态
     * @throws BusinessException 当状态流转不合法时抛出
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
