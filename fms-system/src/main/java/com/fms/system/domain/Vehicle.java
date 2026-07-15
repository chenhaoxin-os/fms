package com.fms.system.domain;

import com.fms.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Vehicle extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "车牌号不能为空")
    private String plate;

    @NotBlank(message = "车辆名称不能为空")
    private String name;

    @NotNull(message = "核载人数不能为空")
    private Integer maxCapacity;

    private Boolean devicePower;   // true-已送电，false-未送电
    private Boolean isRunning;     // true-运行中，false-待命

    private String stableCrew;     // 数据库存储 JSON 字符串
    // 新增字段
    private Long deviceId;
}