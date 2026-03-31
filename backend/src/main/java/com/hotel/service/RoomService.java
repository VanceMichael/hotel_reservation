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

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;
    private final BookingMapper bookingMapper;

    public PageResult<Room> page(int current, int size, Long hotelId, Integer status) {
        Page<Room> page = new Page<>(current, size);
        return PageResult.of(roomMapper.selectPageWithHotel(page, hotelId, status));
    }

    public Room getById(Long id) {
        return roomMapper.selectWithHotel(id);
    }

    public List<Room> listByHotelId(Long hotelId) {
        return roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .eq(Room::getHotelId, hotelId)
                .eq(Room::getStatus, 1)
                .orderByAsc(Room::getPrice));
    }

    public void create(Room room) {
        roomMapper.insert(room);
        log.info("创建房间: {}", room.getName());
    }

    public void update(Room room) {
        if (roomMapper.selectById(room.getId()) == null) {
            throw new BusinessException("房间不存在");
        }
        roomMapper.updateById(room);
        log.info("更新房间: {}", room.getName());
    }

    public void delete(Long id) {
        // 检查是否有活跃预订
        int activeCount = bookingMapper.countActiveByRoomId(id);
        if (activeCount > 0) {
            throw new BusinessException("该房间有 " + activeCount + " 条未完成的预订，无法删除");
        }
        roomMapper.deleteById(id);
        log.info("删除房间: id={}", id);
    }
}
