package com.fms.system.service;


import com.fms.system.domain.Device;
import com.fms.system.domain.vo.DeviceStatisticsVO;

import java.util.List;

/**
 * 装置设备Service接口
 *
 * @author ruoyi
 */
public interface IDeviceService
{
    /**
     * 查询装置设备列表
     *
     * @param device 查询条件
     * @return 装置设备集合
     */
    public List<Device> selectDeviceList(Device device);

    /**
     * 查询装置设备（分页使用）
     *
     * @param device 查询条件
     * @return 装置设备集合
     */
    public List<Device> selectDeviceListForPage(Device device);

    /**
     * 根据ID查询装置设备
     *
     * @param id 装置ID
     * @return 装置设备
     */
    public Device selectDeviceById(Long id);

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
     * 修改装置状态
     *
     * @param id 装置ID
     * @param status 状态
     * @return 结果
     */
    public int changeStatus(Long id, String status);

    /**
     * 批量删除装置设备
     *
     * @param ids 需要删除的装置ID
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);

    /**
     * 获取统计数据（总/在线/低电量/离线）
     *
     * @return 统计VO
     */
    public DeviceStatisticsVO getStatistics();


    /**
     * 校验装置ID是否唯一
     *
     * @param device 装置信息
     * @return 结果 true唯一 false不唯一
     */
    public boolean checkDeviceIdUnique(Device device);
}
