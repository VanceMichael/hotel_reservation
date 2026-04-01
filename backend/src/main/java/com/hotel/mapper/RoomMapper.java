package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 房间数据访问层接口
 * 提供房间相关的数据库操作方法
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    
    /**
     * 根据房间ID查询房间详情，关联查询酒店名称
     * 
     * @param id 房间ID
     * @return 房间详情对象
     */
    @Select("SELECT r.*, h.name as hotel_name FROM room r LEFT JOIN hotel h ON r.hotel_id = h.id WHERE r.id = #{id}")
    Room selectWithHotel(@Param("id") Long id);
    
    /**
     * 分页查询房间列表，支持按酒店ID和状态筛选
     * 
     * @param page 分页参数
     * @param hotelId 酒店ID（可选）
     * @param status 房间状态（可选）：0-不可用，1-可用
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT r.*, h.name as hotel_name FROM room r LEFT JOIN hotel h ON r.hotel_id = h.id " +
            "<where>" +
            "<if test='hotelId != null'>AND r.hotel_id = #{hotelId}</if>" +
            "<if test='status != null'>AND r.status = #{status}</if>" +
            "</where>" +
            "ORDER BY r.created_at DESC" +
            "</script>")
    IPage<Room> selectPageWithHotel(Page<Room> page, @Param("hotelId") Long hotelId, @Param("status") Integer status);
}
