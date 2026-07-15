// EventStatisticsVO.java
package com.fms.system.domain.vo;

/**
 * 事件统计VO
 *
 * @author ruoyi
 */
public class EventStatisticsVO
{
    /** 总事件数 */
    private Long total;

    /** 处置中事件数 */
    private Long processing;

    /** 已结束事件数 */
    private Long finished;

    /** 入场登记数 */
    private Long entranceRegistration;

    /** 分队分组数 */
    private Long teamGrouping;

    /** 车辆日志数 */
    private Long vehicleLog;

    public EventStatisticsVO()
    {
    }

    public EventStatisticsVO(Long total, Long processing, Long finished,
                             Long entranceRegistration, Long teamGrouping, Long vehicleLog)
    {
        this.total = total;
        this.processing = processing;
        this.finished = finished;
        this.entranceRegistration = entranceRegistration;
        this.teamGrouping = teamGrouping;
        this.vehicleLog = vehicleLog;
    }

    // getter/setter
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public Long getProcessing() { return processing; }
    public void setProcessing(Long processing) { this.processing = processing; }

    public Long getFinished() { return finished; }
    public void setFinished(Long finished) { this.finished = finished; }

    public Long getEntranceRegistration() { return entranceRegistration; }
    public void setEntranceRegistration(Long entranceRegistration) { this.entranceRegistration = entranceRegistration; }

    public Long getTeamGrouping() { return teamGrouping; }
    public void setTeamGrouping(Long teamGrouping) { this.teamGrouping = teamGrouping; }

    public Long getVehicleLog() { return vehicleLog; }
    public void setVehicleLog(Long vehicleLog) { this.vehicleLog = vehicleLog; }
}