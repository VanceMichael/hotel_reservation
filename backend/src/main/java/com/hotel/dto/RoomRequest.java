package com.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "房间请求")
public class RoomRequest {

    @Schema(description = "所属酒店ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;

    @Schema(description = "房间名称", example = "外滩景观豪华大床房", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "房间名称不能为空")
    @Size(max = 100, message = "房间名称不能超过100个字符")
    private String name;

    @Schema(description = "房型", example = "豪华房", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "房型不能为空")
    @Size(max = 50, message = "房型不能超过50个字符")
    private String roomType;

    @Schema(description = "每晚价格", example = "2888.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal price;

    @Schema(description = "容纳人数（1-10人）", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "容纳人数不能为空")
    @Min(value = 1, message = "容纳人数至少为1人")
    @Max(value = 10, message = "容纳人数不能超过10人")
    private Integer capacity;

    @Schema(description = "状态：0-不可用 1-可用", example = "1", defaultValue = "1")
    private Integer status = 1;
}
