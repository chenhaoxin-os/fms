package com.fms.system.mapper;


import java.util.List;
import com.fms.system.domain.Device;

/**
 * 装置设备Mapper接口
 *
 * @author ruoyi
 */
public interface DeviceMapper
{
    /**
     * 查询装置设备
     *
     * @param device 查询条件
     * @return 装置设备集合
     */
    public List<Device> selectDeviceList(Device device);

    /**
     * 根据ID查询装置设备
     *
     * @param id 装置ID
     * @return 装置设备
     */
    public Device selectDeviceById(Long id);

    /**
     * 检查装置ID唯一性
     *
     * @param deviceId 装置ID
     * @return 结果
     */
    public Device checkDeviceIdUnique(String deviceId);

    /**
     * 新增装置设备
     *
     * @param device 装置设备
     * @return 结果
     */
    public int insertDevice(Device device);

    /**
     * 修改装置设备
     *
     * @param device 装置设备
     * @return 结果
     */
    public int updateDevice(Device device);

    /**
     * 删除装置设备
     *
     * @param id 装置ID
     * @return 结果
     */
    public int deleteDeviceById(Long id);

    /**
     * 批量删除装置设备
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);
}
