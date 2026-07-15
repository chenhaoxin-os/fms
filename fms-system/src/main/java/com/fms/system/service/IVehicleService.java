package com.fms.system.service;

import java.util.List;

import com.fms.common.core.domain.AjaxResult;
import com.fms.system.domain.Vehicle;

public interface IVehicleService {
    /**
     * 查询车辆列表
     */
    public List<Vehicle> selectVehicleList(Vehicle vehicle);

    /**
     * 根据ID查询车辆
     */
    public Vehicle selectVehicleById(Long id);

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
    public AjaxResult deleteVehicleByIds(Long[] ids);
}