package com.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "预订请求")
public class BookingRequest {

    @Schema(description = "房间ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @Schema(description = "入住日期（不能早于今天）", example = "2026-03-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入住日期不能为空")
    @FutureOrPresent(message = "入住日期不能早于今天")
    private LocalDate checkInDate;

    @Schema(description = "离店日期（必须晚于入住日期）", example = "2026-03-17", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "离店日期不能为空")
    private LocalDate checkOutDate;

    @Schema(description = "备注信息", example = "需要高楼层房间")
    private String remark;
}
