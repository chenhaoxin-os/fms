package com.fms.system.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.fms.common.core.domain.AjaxResult;
import com.fms.common.exception.ServiceException;
import com.fms.system.domain.Device;
import com.fms.system.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fms.system.domain.Vehicle;
import com.fms.system.mapper.VehicleMapper;
import com.fms.system.service.IVehicleService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleServiceImpl implements IVehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Override
    public List<Vehicle> selectVehicleList(Vehicle vehicle) {
        return vehicleMapper.selectVehicleList(vehicle);
    }

    @Override
    public Vehicle selectVehicleById(Long id) {
        return vehicleMapper.selectVehicleById(id);
    }

    @Override
    public int insertVehicle(Vehicle vehicle) {
        // 校验装置唯一性
        checkDeviceIdUnique(vehicle.getDeviceId(), null);
        return vehicleMapper.insertVehicle(vehicle);
    }

    @Override
    public int updateVehicle(Vehicle vehicle) {
        // 校验装置唯一性（排除自身）
        checkDeviceIdUnique(vehicle.getDeviceId(), vehicle.getId());
        return vehicleMapper.updateVehicle(vehicle);
    }

    @Override
    @Transactional
    public int deleteVehicleById(Long id) {
        Long deviceId = vehicleMapper.selectDeviceIdById(id);
        if (deviceId != null && deviceId != 0) {
            Vehicle vehicle = vehicleMapper.selectVehicleById(id); // 获取名称用于提示
            Device device = deviceMapper.selectDeviceById(deviceId);
            throw new ServiceException("车辆【" + vehicle.getName() + "】已关联装置【" + device.getName() + "】(请先解绑再删除)");
        }
        return vehicleMapper.deleteVehicleById(id);
    }


    @Override
    @Transactional
    public AjaxResult deleteVehicleByIds(Long[] ids) {
        // 用于收集所有有关联车辆的装置名称及对应的车辆名称
        List<String> errorMessages = new ArrayList<>();

        for (Long id : ids) {
            // 查询该装置关联的所有车辆（一个装置可能关联多辆车，但业务上可能一对一，但以防万一用列表）
            Vehicle vehicle = vehicleMapper.selectVehicleById(id); // 获取名称用于提示
            if (null != vehicle.getDeviceId()) {
                // 获取装置名称（这里需要先查询装置信息，或者从 deviceId 获取名称）
                Device device = deviceMapper.selectDeviceById(vehicle.getDeviceId());
                String deviceName = device != null ? device.getName() : "ID:" + vehicle.getDeviceId();
                errorMessages.add("车辆【" + vehicle.getName() + "】已关联装置【" + deviceName + "】");
            }
        }

        // 如果有任何错误，一次性抛出所有提示
        if (!errorMessages.isEmpty()) {
            String fullMessage = String.join("<br/>", errorMessages) + "<br/>请先解绑再删除";
            return AjaxResult.error(fullMessage);
        }
        vehicleMapper.deleteVehicleByIds(ids);
        return AjaxResult.success(vehicleMapper.deleteVehicleByIds(ids));
    }
    /**
     * 校验 deviceId 是否已被其他车辆占用
     * @param deviceId 装置ID
     * @param vehicleId 当前车辆ID（编辑时传入，新增传 null）
     */
    private void checkDeviceIdUnique(Long deviceId, Long vehicleId) {
        if (deviceId == null) {
            return; // 未选择装置，无需校验
        }
        int count = vehicleMapper.countByDeviceId(deviceId, vehicleId);
        if (count > 0) {
            throw new ServiceException("该装置已被其他车辆绑定，请重新选择");
        }
    }
}