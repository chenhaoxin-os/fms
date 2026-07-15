// IEventLogService.java
package com.fms.system.service;

import java.util.List;
import com.fms.system.domain.EventLog;
import com.fms.system.domain.vo.EventStatisticsVO;

/**
 * 事件日志Service接口
 *
 * @author ruoyi
 */
public interface IEventLogService
{
    /**
     * 查询事件日志列表
     *
     * @param eventLog 查询条件
     * @return 事件日志集合
     */
    public List<EventLog> selectEventLogList(EventLog eventLog);

    /**
     * 查询事件日志列表（分页使用）
     *
     * @param eventLog 查询条件
     * @return 事件日志集合
     */
    public List<EventLog> selectEventLogListForPage(EventLog eventLog);

    /**
     * 根据ID查询事件日志
     *
     * @param id 事件ID
     * @return 事件日志
     */
    public EventLog selectEventLogById(Long id);

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
     * 修改事件状态
     *
     * @param id 事件ID
     * @param status 状态
     * @return 结果
     */
    public int changeStatus(Long id, String status);

    /**
     * 批量删除事件日志
     *
     * @param ids 需要删除的事件ID
     * @return 结果
     */
    public int deleteEventLogByIds(Long[] ids);

    /**
     * 获取事件统计信息
     *
     * @return 统计VO
     */
    public EventStatisticsVO getStatistics();

    /**
     * 校验事件ID是否唯一
     *
     * @param eventLog 事件信息
     * @return 结果 true唯一 false不唯一
     */
    public boolean checkEventIdUnique(EventLog eventLog);
}