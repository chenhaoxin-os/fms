package com.fms.system.domain.vo;

import com.fms.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VehicleVO extends BaseEntity {
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

    private List<CrewMember> stableCrew; // 内部嵌套集合
    // 新增字段
    private Long deviceId;


    public static class CrewMember {
        private String name;

        // 无参构造（必须）
        public CrewMember() {}

        public CrewMember(String name) {
            this.name = name;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}