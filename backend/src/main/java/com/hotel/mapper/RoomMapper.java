package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    @Select("SELECT r.*, h.name as hotel_name FROM room r LEFT JOIN hotel h ON r.hotel_id = h.id WHERE r.id = #{id}")
    Room selectWithHotel(@Param("id") Long id);
    
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
