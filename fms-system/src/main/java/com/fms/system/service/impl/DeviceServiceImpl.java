package com.fms.system.service.impl;


import java.util.ArrayList;
import java.util.List;

import com.fms.system.domain.EventLog;
import com.fms.system.domain.ReaderEvent;
import com.fms.system.domain.Vehicle;
import com.fms.system.mapper.EventLogMapper;
import com.fms.system.mapper.VehicleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fms.common.constant.UserConstants;
import com.fms.common.exception.ServiceException;
import com.fms.common.utils.StringUtils;
import com.fms.system.domain.Device;
import com.fms.system.domain.vo.DeviceStatisticsVO;
import com.fms.system.mapper.DeviceMapper;
import com.fms.system.service.IDeviceService;

/**
 * 装置设备Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class DeviceServiceImpl implements IDeviceService
{
    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private EventLogMapper eventLogMapper;
    @Override
    public List<Device> selectDeviceList(Device device)
    {
        return deviceMapper.selectDeviceList(device);
    }

    @Override
    public List<Device> selectDeviceListForPage(Device device)
    {
        return deviceMapper.selectDeviceList(device);
    }

    @Override
    public Device selectDeviceById(Long id)
    {
        return deviceMapper.selectDeviceById(id);
    }

    @Override
    @Transactional
    public int insertDevice(Device device)
    {
        // 检查装置ID是否唯一
        if (!checkDeviceIdUnique(device))
        {
            throw new ServiceException("装置ID【" + device.getDeviceId() + "】已存在");
        }
        // 默认最后通讯时间为当前时间
        if (device.getLastComTime() == null)
        {
            device.setLastComTime(java.time.LocalDateTime.now());
        }
        return deviceMapper.insertDevice(device);
    }

    @Override
    @Transactional
    public int updateDevice(Device device)
    {
        // 检查装置ID是否唯一
        if (!checkDeviceIdUnique(device))
        {
            throw new ServiceException("装置ID【" + device.getDeviceId() + "】已存在");
        }
        return deviceMapper.updateDevice(device);
    }

    @Override
    public int changeStatus(Long id, String status)
    {
        Device device = new Device();
        device.setId(id);
        device.setStatus(status);
        return deviceMapper.updateDevice(device);
    }

    @Override
    public int deleteDeviceByIds(Long[] ids) {
        // 用于收集所有有关联车辆的装置名称及对应的车辆名称
        List<String> errorMessages = new ArrayList<>();

        for (Long deviceId : ids) {
            // 查询该装置关联的所有车辆（一个装置可能关联多辆车，但业务上可能一对一，但以防万一用列表）
            Vehicle vehicles = vehicleMapper.selectVehicleByDeviceId(deviceId);
            if (null != vehicles) {
                // 获取装置名称（这里需要先查询装置信息，或者从 deviceId 获取名称）
                Device device = deviceMapper.selectDeviceById(deviceId);
                String deviceName = device != null ? device.getName() : "ID:" + deviceId;
                errorMessages.add("装置【" + deviceName + "】已关联车辆【" + vehicles.getName() + "】");
            }
        }

        // 如果有任何错误，一次性抛出所有提示
        if (!errorMessages.isEmpty()) {
            String fullMessage = String.join("；", errorMessages) + "，请先解绑再删除";
            throw new ServiceException(fullMessage);
        }

        // 全部校验通过，执行删除
        return deviceMapper.deleteDeviceByIds(ids);
    }

    @Override
    public DeviceStatisticsVO getStatistics()
    {
        // 查询所有装置（不分页）
        Device query = new Device();
        List<Device> allDevices = deviceMapper.selectDeviceList(query);
        long total = allDevices.size();
        long online = allDevices.stream().filter(d -> "online".equals(d.getStatus())).count();
        long lowBattery = allDevices.stream().filter(d -> "low_battery".equals(d.getStatus())).count();
        long offline = allDevices.stream().filter(d -> "offline".equals(d.getStatus())).count();
        return new DeviceStatisticsVO(total, online, lowBattery, offline);
    }


    @Override
    public boolean checkDeviceIdUnique(Device device)
    {
        Long id = device.getId() == null ? -1L : device.getId();
        Device info = deviceMapper.checkDeviceIdUnique(device.getDeviceId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public void pushEvent(ReaderEvent readerEvent) {
        EventLog log = new EventLog();

        eventLogMapper.insertEventLog(log);
    }
}
