package com.fms.system.mapper;

import java.util.List;
import com.fms.system.domain.Vehicle;
import org.apache.ibatis.annotations.Param;

public interface VehicleMapper {
    /**
     * 查询车辆列表
     */
    public List<Vehicle> selectVehicleList(Vehicle vehicle);

    /**
     * 根据ID查询车辆
     */
    public Vehicle selectVehicleById(Long id);

    /**
     * 根据ID查询车辆
     */
    public Vehicle selectVehicleByDeviceId(Long id);


    /**
     * 新增车辆
     */
    public int insertVehicle(Vehicle vehicle);

    /**
     * 修改车辆
     */
    public int updateVehicle(Vehicle vehicle);

    /**
     * 删除车辆（物理删除）
     */
    public int deleteVehicleById(Long id);

    /**
     * 批量删除
     */
    public int deleteVehicleByIds(Long[] ids);

    /**
     * 统计该 deviceId 被多少车辆使用（排除自身）
     * @param deviceId 装置ID
     * @param excludeId 需排除的车辆ID（编辑时使用）
     * @return 数量
     */
    public int countByDeviceId(@Param("deviceId") Long deviceId, @Param("excludeId") Long excludeId);

    // 根据ID查询车辆（只查 deviceId 字段，提高效率）
    public Long selectDeviceIdById(Long id);
}