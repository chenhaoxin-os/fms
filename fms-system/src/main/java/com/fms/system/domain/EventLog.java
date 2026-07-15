// EventLog.java
package com.fms.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fms.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 事件日志对象 event_log
 *
 * @author ruoyi
 */
public class EventLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 事件唯一标识 */
    private String eventId;

    /** 事件发生时间 */
    private Date eventTime;

    /** 事件名称 */
    private String eventName;

    /** 事件类型：入场登记/分队分组/车辆日志 */
    private String eventType;

    /** 调度力量 */
    private String dispatchForce;

    /** 状态：处置中/已结束 */
    private String status;

    /** 操作人 */
    private String operator;

    /** 关联车辆ID */
    private Long vehicleId;

    /** 事件附加JSON数据 */
    private String jsonData;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getEventId()
    {
        return eventId;
    }

    public void setEventId(String eventId)
    {
        this.eventId = eventId;
    }

    public Date getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Date eventTime)
    {
        this.eventTime = eventTime;
    }

    public String getEventName()
    {
        return eventName;
    }

    public void setEventName(String eventName)
    {
        this.eventName = eventName;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getDispatchForce()
    {
        return dispatchForce;
    }

    public void setDispatchForce(String dispatchForce)
    {
        this.dispatchForce = dispatchForce;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public Long getVehicleId()
    {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId)
    {
        this.vehicleId = vehicleId;
    }

    public String getJsonData()
    {
        return jsonData;
    }

    public void setJsonData(String jsonData)
    {
        this.jsonData = jsonData;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("eventId", getEventId())
                .append("eventTime", getEventTime())
                .append("eventName", getEventName())
                .append("eventType", getEventType())
                .append("dispatchForce", getDispatchForce())
                .append("status", getStatus())
                .append("operator", getOperator())
                .append("vehicleId", getVehicleId())
                .append("jsonData", getJsonData())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}