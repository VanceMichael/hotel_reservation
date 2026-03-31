package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
    @Select("SELECT b.*, u.username, r.name as room_name, h.name as hotel_name " +
            "FROM booking b " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "LEFT JOIN room r ON b.room_id = r.id " +
            "LEFT JOIN hotel h ON r.hotel_id = h.id " +
            "WHERE b.id = #{id}")
    Booking selectWithDetail(@Param("id") Long id);
    
    @Select("<script>" +
            "SELECT b.*, u.username, r.name as room_name, h.name as hotel_name " +
            "FROM booking b " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "LEFT JOIN room r ON b.room_id = r.id " +
            "LEFT JOIN hotel h ON r.hotel_id = h.id " +
            "<where>" +
            "<if test='userId != null'>AND b.user_id = #{userId}</if>" +
            "<if test='status != null'>AND b.status = #{status}</if>" +
            "</where>" +
            "ORDER BY b.created_at DESC" +
            "</script>")
    IPage<Booking> selectPageWithDetail(Page<Booking> page, @Param("userId") Long userId, @Param("status") Integer status);
    
    // 查询用户的活跃预订数量（状态：待确认0、已确认1、已入住2）
    @Select("SELECT COUNT(*) FROM booking WHERE user_id = #{userId} AND status IN (0, 1, 2)")
    int countActiveByUserId(@Param("userId") Long userId);
    
    // 查询房间的活跃预订数量
    @Select("SELECT COUNT(*) FROM booking WHERE room_id = #{roomId} AND status IN (0, 1, 2)")
    int countActiveByRoomId(@Param("roomId") Long roomId);
    
    // 查询酒店的活跃预订数量（通过房间关联）
    @Select("SELECT COUNT(*) FROM booking b " +
            "INNER JOIN room r ON b.room_id = r.id " +
            "WHERE r.hotel_id = #{hotelId} AND b.status IN (0, 1, 2)")
    int countActiveByHotelId(@Param("hotelId") Long hotelId);
    
    // 检查房间在指定日期范围内是否有冲突预订（排除已取消和已完成的预订）
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE room_id = #{roomId} " +
            "AND status IN (0, 1, 2) " +
            "AND check_in_date < #{checkOutDate} " +
            "AND check_out_date > #{checkInDate}")
    int countConflictBookings(@Param("roomId") Long roomId, 
                              @Param("checkInDate") LocalDate checkInDate, 
                              @Param("checkOutDate") LocalDate checkOutDate);
    
    // 检查房间在指定日期范围内是否有冲突预订（排除指定预订ID，用于更新场景）
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE room_id = #{roomId} " +
            "AND id != #{excludeId} " +
            "AND status IN (0, 1, 2) " +
            "AND check_in_date < #{checkOutDate} " +
            "AND check_out_date > #{checkInDate}")
    int countConflictBookingsExclude(@Param("roomId") Long roomId, 
                                     @Param("checkInDate") LocalDate checkInDate, 
                                     @Param("checkOutDate") LocalDate checkOutDate,
                                     @Param("excludeId") Long excludeId);
}
