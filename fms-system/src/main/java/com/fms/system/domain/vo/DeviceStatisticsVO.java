package com.fms.system.domain.vo;



/**
 * 装置统计信息
 *
 * @author ruoyi
 */
public class DeviceStatisticsVO
{
    private Long total;      // 总装置数
    private Long online;     // 在线数
    private Long lowBattery; // 低电量预警数
    private Long offline;    // 离线数

    public DeviceStatisticsVO() {}

    public DeviceStatisticsVO(Long total, Long online, Long lowBattery, Long offline)
    {
        this.total = total;
        this.online = online;
        this.lowBattery = lowBattery;
        this.offline = offline;
    }

    // getter / setter
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getOnline() { return online; }
    public void setOnline(Long online) { this.online = online; }
    public Long getLowBattery() { return lowBattery; }
    public void setLowBattery(Long lowBattery) { this.lowBattery = lowBattery; }
    public Long getOffline() { return offline; }
    public void setOffline(Long offline) { this.offline = offline; }
}
