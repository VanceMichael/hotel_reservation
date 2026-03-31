package com.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "酒店请求")
public class HotelRequest {

    @Schema(description = "酒店名称", example = "上海外滩华尔道夫酒店", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "酒店名称不能为空")
    @Size(max = 100, message = "酒店名称不能超过100个字符")
    private String name;

    @Schema(description = "酒店地址", example = "上海市黄浦区中山东一路2号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "酒店地址不能为空")
    @Size(max = 255, message = "酒店地址不能超过255个字符")
    private String address;

    @Schema(description = "联系电话", example = "021-63229988")
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;

    @Schema(description = "封面图片（URL 或 Base64）")
    private String coverImage;

    @Schema(description = "酒店描述", example = "坐落于外滩核心地段，尽享黄浦江美景")
    @Size(max = 1000, message = "酒店描述不能超过1000个字符")
    private String description;

    @Schema(description = "状态：0-下架 1-上架", example = "1", defaultValue = "1")
    private Integer status = 1;
}
