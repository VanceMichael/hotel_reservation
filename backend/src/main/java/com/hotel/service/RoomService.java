package com.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.entity.Room;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 房间服务类
 * 提供房间相关的业务逻辑处理，包括查询、创建、更新和删除等操作
 * 注意：房间状态（Room.status）表示房间本身的可用性（如维修中、清洁中），
 *       而房间是否可预订是通过预订冲突检查来判断的
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;
    private final BookingMapper bookingMapper;

    /**
     * 分页查询房间列表
     * 
     * @param current 当前页码
     * @param size 每页数量
     * @param hotelId 酒店ID（可选，用于筛选特定酒店的房间）
     * @param status 房间状态（可选，用于筛选特定状态的房间）：0-不可用，1-可用
     * @return 分页结果
     */
    public PageResult<Room> page(int current, int size, Long hotelId, Integer status) {
        Page<Room> page = new Page<>(current, size);
        return PageResult.of(roomMapper.selectPageWithHotel(page, hotelId, status));
    }

    /**
     * 根据房间ID查询房间详情
     * 
     * @param id 房间ID
     * @return 房间详情对象
     */
    public Room getById(Long id) {
        return roomMapper.selectWithHotel(id);
    }

    /**
     * 查询指定酒店的所有可用房间列表
     * 只返回状态为可用（status=1）的房间
     * 
     * @param hotelId 酒店ID
     * @return 可用房间列表
     */
    public List<Room> listByHotelId(Long hotelId) {
        return roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .eq(Room::getHotelId, hotelId)
                .eq(Room::getStatus, 1)
                .orderByAsc(Room::getPrice));
    }

    /**
     * 创建新房间
     * 
     * @param room 房间信息
     */
    public void create(Room room) {
        roomMapper.insert(room);
        log.info("创建房间: {}", room.getName());
    }

    /**
     * 更新房间信息
     * 
     * @param room 房间信息
     * @throws BusinessException 当房间不存在时抛出
     */
    public void update(Room room) {
        if (roomMapper.selectById(room.getId()) == null) {
            throw new BusinessException("房间不存在");
        }
        roomMapper.updateById(room);
        log.info("更新房间: {}", room.getName());
    }

    /**
     * 删除房间
     * 会检查是否有活跃预订，如果有则无法删除
     * 
     * @param id 房间ID
     * @throws BusinessException 当房间有未完成的预订时抛出
     */
    public void delete(Long id) {
        int activeCount = bookingMapper.countActiveByRoomId(id);
        if (activeCount > 0) {
            throw new BusinessException("该房间有 " + activeCount + " 条未完成的预订，无法删除");
        }
        roomMapper.deleteById(id);
        log.info("删除房间: id={}", id);
    }
}
