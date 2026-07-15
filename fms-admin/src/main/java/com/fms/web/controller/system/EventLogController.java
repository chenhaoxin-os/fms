// EventLogController.java
package com.fms.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.fms.common.annotation.Log;
import com.fms.common.core.controller.BaseController;
import com.fms.common.core.domain.AjaxResult;
import com.fms.common.core.page.TableDataInfo;
import com.fms.common.enums.BusinessType;
import com.fms.common.utils.poi.ExcelUtil;
import com.fms.system.domain.EventLog;
import com.fms.system.domain.vo.EventStatisticsVO;
import com.fms.system.service.IEventLogService;

/**
 * 事件日志管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/event")
public class EventLogController extends BaseController
{
    @Autowired
    private IEventLogService eventLogService;

    /**
     * 查询事件列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(EventLog eventLog)
    {
        startPage();
        List<EventLog> list = eventLogService.selectEventLogListForPage(eventLog);
        return getDataTable(list);
    }

    /**
     * 获取事件统计信息
     */
    @GetMapping("/statistics")
    public AjaxResult statistics()
    {
        EventStatisticsVO vo = eventLogService.getStatistics();
        return success(vo);
    }

    /**
     * 查询事件详情
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(eventLogService.selectEventLogById(id));
    }

    /**
     * 新增事件
     */
    @Log(title = "事件管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody EventLog eventLog)
    {
        if (!eventLogService.checkEventIdUnique(eventLog))
        {
            return error("新增事件【" + eventLog.getEventName() + "】失败，事件ID已存在");
        }
        return toAjax(eventLogService.insertEventLog(eventLog));
    }

    /**
     * 修改事件
     */
    @Log(title = "事件管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody EventLog eventLog)
    {
        if (!eventLogService.checkEventIdUnique(eventLog))
        {
            return error("修改事件【" + eventLog.getEventName() + "】失败，事件ID已存在");
        }
        return toAjax(eventLogService.updateEventLog(eventLog));
    }

    /**
     * 修改事件状态
     */
    @Log(title = "事件管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestParam Long id, @RequestParam String status)
    {
        return toAjax(eventLogService.changeStatus(id, status));
    }

    /**
     * 删除事件
     */
    @Log(title = "事件管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(eventLogService.deleteEventLogByIds(ids));
    }

    /**
     * 导出事件数据
     */
    @Log(title = "事件管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EventLog eventLog)
    {
        List<EventLog> list = eventLogService.selectEventLogList(eventLog);
        ExcelUtil<EventLog> util = new ExcelUtil<>(EventLog.class);
        util.exportExcel(response, list, "事件数据");
    }
}