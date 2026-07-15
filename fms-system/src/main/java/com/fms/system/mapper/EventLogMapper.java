// EventLogMapper.java
package com.fms.system.mapper;

import java.util.List;
import com.fms.system.domain.EventLog;

/**
 * 事件日志Mapper接口
 *
 * @author ruoyi
 */
public interface EventLogMapper
{
    /**
     * 查询事件日志列表
     *
     * @param eventLog 查询条件
     * @return 事件日志集合
     */
    public List<EventLog> selectEventLogList(EventLog eventLog);

    /**
     * 根据ID查询事件日志
     *
     * @param id 事件ID
     * @return 事件日志
     */
    public EventLog selectEventLogById(Long id);

    /**
     * 根据事件ID查询事件日志
     *
     * @param eventId 事件唯一标识
     * @return 事件日志
     */
    public EventLog selectEventLogByEventId(String eventId);

    /**
     * 新增事件日志
     *
     * @param eventLog 事件日志
     * @return 结果
     */
    public int insertEventLog(EventLog eventLog);

    /**
     * 修改事件日志
     *
     * @param eventLog 事件日志
     * @return 结果
     */
    public int updateEventLog(EventLog eventLog);

    /**
     * 删除事件日志
     *
     * @param id 事件ID
     * @return 结果
     */
    public int deleteEventLogById(Long id);

    /**
     * 批量删除事件日志
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteEventLogByIds(Long[] ids);
}