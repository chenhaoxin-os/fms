// EventLogServiceImpl.java
package com.fms.system.service.impl;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fms.common.constant.UserConstants;
import com.fms.common.exception.ServiceException;
import com.fms.common.utils.StringUtils;
import com.fms.system.domain.EventLog;
import com.fms.system.domain.vo.EventStatisticsVO;
import com.fms.system.mapper.EventLogMapper;
import com.fms.system.service.IEventLogService;

/**
 * 事件日志Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class EventLogServiceImpl implements IEventLogService
{
    @Autowired
    private EventLogMapper eventLogMapper;

    @Override
    public List<EventLog> selectEventLogList(EventLog eventLog)
    {
        return eventLogMapper.selectEventLogList(eventLog);
    }

    @Override
    public List<EventLog> selectEventLogListForPage(EventLog eventLog)
    {
        return eventLogMapper.selectEventLogList(eventLog);
    }

    @Override
    public EventLog selectEventLogById(Long id)
    {
        return eventLogMapper.selectEventLogById(id);
    }

    @Override
    @Transactional
    public int insertEventLog(EventLog eventLog)
    {
        // 检查事件ID是否唯一
        if (!checkEventIdUnique(eventLog))
        {
            throw new ServiceException("新增事件【" + eventLog.getEventName() + "】失败，事件ID已存在");
        }
        // 如果没有传入事件ID，自动生成
        if (StringUtils.isEmpty(eventLog.getEventId()))
        {
            eventLog.setEventId(generateEventId());
        }
        // 默认状态为已结束
        if (StringUtils.isEmpty(eventLog.getStatus()))
        {
            eventLog.setStatus("已结束");
        }
        // 默认事件时间为当前时间
        if (eventLog.getEventTime() == null)
        {
            eventLog.setEventTime(new Date());
        }
        return eventLogMapper.insertEventLog(eventLog);
    }

    @Override
    @Transactional
    public int updateEventLog(EventLog eventLog)
    {
        // 检查事件ID是否唯一
        if (!checkEventIdUnique(eventLog))
        {
            throw new ServiceException("修改事件【" + eventLog.getEventName() + "】失败，事件ID已存在");
        }
        return eventLogMapper.updateEventLog(eventLog);
    }

    @Override
    public int changeStatus(Long id, String status)
    {
        EventLog eventLog = new EventLog();
        eventLog.setId(id);
        eventLog.setStatus(status);
        return eventLogMapper.updateEventLog(eventLog);
    }

    @Override
    public int deleteEventLogByIds(Long[] ids)
    {
        return eventLogMapper.deleteEventLogByIds(ids);
    }

    @Override
    public EventStatisticsVO getStatistics()
    {
        // 查询所有事件（不分页）
        EventLog query = new EventLog();
        List<EventLog> allEvents = eventLogMapper.selectEventLogList(query);

        long total = allEvents.size();
        long processing = allEvents.stream().filter(e -> "处置中".equals(e.getStatus())).count();
        long finished = allEvents.stream().filter(e -> "已结束".equals(e.getStatus())).count();
        long entranceRegistration = allEvents.stream().filter(e -> "入场登记".equals(e.getEventType())).count();
        long teamGrouping = allEvents.stream().filter(e -> "分队分组".equals(e.getEventType())).count();
        long vehicleLog = allEvents.stream().filter(e -> "车辆日志".equals(e.getEventType())).count();

        return new EventStatisticsVO(total, processing, finished, entranceRegistration, teamGrouping, vehicleLog);
    }

    @Override
    public boolean checkEventIdUnique(EventLog eventLog)
    {
        Long id = eventLog.getId() == null ? -1L : eventLog.getId();
        EventLog info = eventLogMapper.selectEventLogByEventId(eventLog.getEventId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 生成唯一事件ID
     * 格式：EV + yyyyMMdd + 4位序号
     */
    private String generateEventId()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());

        // 查询今日最大序号
        EventLog query = new EventLog();
        query.setEventId(dateStr);
        List<EventLog> todayEvents = eventLogMapper.selectEventLogList(query);

        int maxSeq = 0;
        for (EventLog event : todayEvents)
        {
            String eventId = event.getEventId();
            if (eventId != null && eventId.length() >= 12)
            {
                try
                {
                    int seq = Integer.parseInt(eventId.substring(8));
                    if (seq > maxSeq)
                    {
                        maxSeq = seq;
                    }
                }
                catch (NumberFormatException e)
                {
                    // 忽略格式异常
                }
            }
        }

        return "EV" + dateStr + String.format("%04d", maxSeq + 1);
    }
}