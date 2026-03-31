package com.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.entity.Hotel;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.HotelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelMapper hotelMapper;
    private final BookingMapper bookingMapper;

    public PageResult<Hotel> page(int current, int size, String name, Integer status) {
        Page<Hotel> page = new Page<>(current, size);
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Hotel::getName, name);
        }
        if (status != null) {
            wrapper.eq(Hotel::getStatus, status);
        }
        wrapper.orderByDesc(Hotel::getCreatedAt);
        return PageResult.of(hotelMapper.selectPage(page, wrapper));
    }

    public Hotel getById(Long id) {
        return hotelMapper.selectById(id);
    }

    public void create(Hotel hotel) {
        hotelMapper.insert(hotel);
        log.info("创建酒店: {}", hotel.getName());
    }

    public void update(Hotel hotel) {
        if (hotelMapper.selectById(hotel.getId()) == null) {
            throw new BusinessException("酒店不存在");
        }
        hotelMapper.updateById(hotel);
        log.info("更新酒店: {}", hotel.getName());
    }

    public void delete(Long id) {
        // 检查是否有活跃预订
        int activeCount = bookingMapper.countActiveByHotelId(id);
        if (activeCount > 0) {
            throw new BusinessException("该酒店有 " + activeCount + " 条未完成的预订，无法删除");
        }
        hotelMapper.deleteById(id);
        log.info("删除酒店: id={}", id);
    }
}
