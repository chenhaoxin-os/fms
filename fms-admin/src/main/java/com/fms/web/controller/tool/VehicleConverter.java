package com.fms.web.controller.tool;

import com.alibaba.fastjson2.JSON;
import com.fms.system.domain.Vehicle;
import com.fms.system.domain.vo.VehicleVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class VehicleConverter {


    /**
     * VO → Entity
     */
    public Vehicle toEntity(VehicleVO vo) {
        if (vo == null) {
            return null;
        }
        Vehicle entity = new Vehicle();
        entity.setId(vo.getId());
        entity.setPlate(vo.getPlate());
        entity.setName(vo.getName());
        entity.setMaxCapacity(vo.getMaxCapacity());
        entity.setDevicePower(vo.getDevicePower());
        entity.setIsRunning(vo.getIsRunning());   // 注意 VO 中的 getter 是 getRunning()
        entity.setDeviceId(vo.getDeviceId());
        // 关键：将 List<CrewMember> 转为 JSON 字符串
        if (vo.getStableCrew() != null && !vo.getStableCrew().isEmpty()) {
            String crewJson = JSON.toJSONString(vo.getStableCrew());
            entity.setStableCrew(crewJson);
        } else {
            entity.setStableCrew(null);  // 或 "[]"
        }

        return entity;
    }

    /**
     * Entity → VO
     */
    public VehicleVO toVO(Vehicle entity) {
        if (entity == null) {
            return null;
        }
        VehicleVO vo = new VehicleVO();
        vo.setId(entity.getId());
        vo.setPlate(entity.getPlate());
        vo.setName(entity.getName());
        vo.setMaxCapacity(entity.getMaxCapacity());
        vo.setDevicePower(entity.getDevicePower());
        vo.setIsRunning(entity.getIsRunning());
        vo.setDeviceId(entity.getDeviceId());
        // 关键：将 JSON 字符串反序列化为 List<CrewMember>
        String crewJson = entity.getStableCrew();
        if (crewJson != null && !crewJson.isEmpty()) {
            // 注意：如果希望返回空列表而不是 null，可设置默认值
            List<VehicleVO.CrewMember> crewList = JSON.parseArray(crewJson, VehicleVO.CrewMember.class);
            vo.setStableCrew(crewList);
        } else {
            vo.setStableCrew(Collections.emptyList());  // 避免前端处理 null
        }

        return vo;
    }
}
