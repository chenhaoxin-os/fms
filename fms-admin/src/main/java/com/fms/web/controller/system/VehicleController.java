package com.fms.web.controller.system;

import java.util.List;

import com.fms.system.domain.vo.VehicleVO;
import com.fms.web.controller.tool.VehicleConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.fms.common.annotation.Log;
import com.fms.common.core.controller.BaseController;
import com.fms.common.core.domain.AjaxResult;
import com.fms.common.core.page.TableDataInfo;
import com.fms.common.enums.BusinessType;
import com.fms.system.domain.Vehicle;
import com.fms.system.service.IVehicleService;

@RestController
@RequestMapping("/system/vehicle")
public class VehicleController extends BaseController {

    @Autowired
    private IVehicleService vehicleService;
    @Autowired
    private  VehicleConverter vehicleConverter;
    /**
     * 查询车辆列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(Vehicle vehicle) {
        startPage();
        List<Vehicle> list = vehicleService.selectVehicleList(vehicle);
        return getDataTable(list);
    }

    /**
     * 查询车辆详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(vehicleService.selectVehicleById(id));
    }

    /**
     * 新增车辆
     */
    @Log(title = "车辆管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody VehicleVO vehicle) {
        vehicle.setCreateBy(getUsername());
        return toAjax(vehicleService.insertVehicle(vehicleConverter.toEntity(vehicle)));
    }

    /**
     * 修改车辆
     */
    @Log(title = "车辆管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody VehicleVO vehicle) {
        vehicle.setUpdateBy(getUsername());
        return toAjax(vehicleService.updateVehicle(vehicleConverter.toEntity(vehicle)));
    }

    /**
     * 删除车辆（单个或批量）
     */
    @Log(title = "车辆管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return vehicleService.deleteVehicleByIds(ids);
    }
    /**
     * 解绑装置（将 vehicle.device_id 置为 null）
     */
    @Log(title = "车辆管理", businessType = BusinessType.UPDATE)
    @PutMapping("/unbindDevice/{id}")
    public AjaxResult unbindDevice(@PathVariable Long id) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setDeviceId(null);
        vehicle.setUpdateBy(getUsername());
        return toAjax(vehicleService.updateVehicle(vehicle));
    }
}