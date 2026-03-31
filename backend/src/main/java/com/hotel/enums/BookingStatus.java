package com.hotel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Set;

/**
 * 预订状态枚举
 */
@Getter
public enum BookingStatus {

    PENDING(0, "待确认"),
    CONFIRMED(1, "已确认"),
    CHECKED_IN(2, "已入住"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    @EnumValue  // MyBatis-Plus 数据库存储值
    @JsonValue  // JSON 序列化值
    private final int code;

    private final String description;

    BookingStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static BookingStatus fromCode(int code) {
        for (BookingStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的预订状态码: " + code);
    }

    /**
     * 获取允许流转的目标状态集合
     */
    public Set<BookingStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(CHECKED_IN, CANCELLED);
            case CHECKED_IN -> Set.of(COMPLETED);
            case COMPLETED, CANCELLED -> Set.of();
        };
    }

    /**
     * 校验是否可以流转到目标状态
     */
    public boolean canTransitionTo(BookingStatus target) {
        if (this == target) {
            return true; // 状态未变更，允许
        }
        return getAllowedTransitions().contains(target);
    }
}
