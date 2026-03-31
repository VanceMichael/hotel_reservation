package com.hotel.config;

import com.hotel.enums.BookingStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 枚举类型转换器配置
 * 支持前端传入数字自动转换为枚举类型
 */
@Configuration
public class EnumConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToBookingStatusConverter());
    }

    /**
     * String/Integer -> BookingStatus 转换器
     */
    public static class StringToBookingStatusConverter implements Converter<String, BookingStatus> {
        @Override
        public BookingStatus convert(@NonNull String source) {
            try {
                int code = Integer.parseInt(source);
                return BookingStatus.fromCode(code);
            } catch (NumberFormatException e) {
                // 尝试按枚举名称匹配
                return BookingStatus.valueOf(source.toUpperCase());
            }
        }
    }
}
