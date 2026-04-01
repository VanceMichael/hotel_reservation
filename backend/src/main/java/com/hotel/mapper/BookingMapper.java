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

/**
 * 预订数据访问层接口
 * 提供预订相关的数据库操作方法
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
    
    /**
     * 根据预订ID查询预订详情，关联查询用户名、房间名和酒店名
     * 
     * @param id 预订ID
     * @return 预订详情对象
     */
    @Select("SELECT b.*, u.username, r.name as room_name, h.name as hotel_name " +
            "FROM booking b " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "LEFT JOIN room r ON b.room_id = r.id " +
            "LEFT JOIN hotel h ON r.hotel_id = h.id " +
            "WHERE b.id = #{id}")
    Booking selectWithDetail(@Param("id") Long id);
    
    /**
     * 分页查询预订列表，支持按用户ID和状态筛选
     * 
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param status 预订状态（可选）
     * @return 分页结果
     */
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
    
    /**
     * 查询用户的活跃预订数量
     * 活跃状态包括：待确认(0)、已确认(1)、已入住(2)
     * 
     * @param userId 用户ID
     * @return 活跃预订数量
     */
    @Select("SELECT COUNT(*) FROM booking WHERE user_id = #{userId} AND status IN (0, 1, 2)")
    int countActiveByUserId(@Param("userId") Long userId);
    
    /**
     * 查询房间的活跃预订数量
     * 活跃状态包括：待确认(0)、已确认(1)、已入住(2)
     * 
     * @param roomId 房间ID
     * @return 活跃预订数量
     */
    @Select("SELECT COUNT(*) FROM booking WHERE room_id = #{roomId} AND status IN (0, 1, 2)")
    int countActiveByRoomId(@Param("roomId") Long roomId);
    
    /**
     * 查询酒店的活跃预订数量（通过房间关联）
     * 活跃状态包括：待确认(0)、已确认(1)、已入住(2)
     * 
     * @param hotelId 酒店ID
     * @return 活跃预订数量
     */
    @Select("SELECT COUNT(*) FROM booking b " +
            "INNER JOIN room r ON b.room_id = r.id " +
            "WHERE r.hotel_id = #{hotelId} AND b.status IN (0, 1, 2)")
    int countActiveByHotelId(@Param("hotelId") Long hotelId);
    
    /**
     * 检查房间在指定日期范围内是否有冲突预订
     * 排除已取消(4)和已完成(3)的预订，只考虑活跃状态(0,1,2)的预订
     * 同时排除离店日期早于或等于今天的预订，即使状态是活跃的
     * 
     * @param roomId 房间ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @return 冲突预订数量
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE room_id = #{roomId} " +
            "AND status IN (0, 1, 2) " +
            "AND check_out_date > CURRENT_DATE " +
            "AND check_in_date < #{checkOutDate} " +
            "AND check_out_date > #{checkInDate}")
    int countConflictBookings(@Param("roomId") Long roomId, 
                              @Param("checkInDate") LocalDate checkInDate, 
                              @Param("checkOutDate") LocalDate checkOutDate);
    
    /**
     * 检查房间在指定日期范围内是否有冲突预订（排除指定预订ID）
     * 用于更新预订时的冲突检查，排除当前正在更新的预订
     * 排除已取消(4)和已完成(3)的预订，只考虑活跃状态(0,1,2)的预订
     * 同时排除离店日期早于或等于今天的预订，即使状态是活跃的
     * 
     * @param roomId 房间ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @param excludeId 需要排除的预订ID
     * @return 冲突预订数量
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE room_id = #{roomId} " +
            "AND id != #{excludeId} " +
            "AND status IN (0, 1, 2) " +
            "AND check_out_date > CURRENT_DATE " +
            "AND check_in_date < #{checkOutDate} " +
            "AND check_out_date > #{checkInDate}")
    int countConflictBookingsExclude(@Param("roomId") Long roomId, 
                                     @Param("checkInDate") LocalDate checkInDate, 
                                     @Param("checkOutDate") LocalDate checkOutDate,
                                     @Param("excludeId") Long excludeId);
}
