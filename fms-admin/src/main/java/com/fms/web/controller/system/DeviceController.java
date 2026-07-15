package com.fms.web.controller.system;



import java.util.List;

import com.fms.system.domain.ReaderEvent;
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
import com.fms.system.domain.Device;
import com.fms.system.domain.vo.DeviceStatisticsVO;
import com.fms.system.service.IDeviceService;

/**
 * 装置设备管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/device")
public class DeviceController extends BaseController
{
    @Autowired
    private IDeviceService deviceService;

    /**
     * 查询装置列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(Device device)
    {
        startPage();
        List<Device> list = deviceService.selectDeviceListForPage(device);
        return getDataTable(list);
    }

    /**
     * 获取装置统计信息
     */
    @GetMapping("/statistics")
    public AjaxResult statistics()
    {
        DeviceStatisticsVO vo = deviceService.getStatistics();
        return success(vo);
    }


    /**
     * 查询装置详情
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(deviceService.selectDeviceById(id));
    }

    /**
     * 新增装置
     */
    @Log(title = "装置管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Device device)
    {
        if (!deviceService.checkDeviceIdUnique(device))
        {
            return error("新增装置【" + device.getName() + "】失败，装置编号已存在");
        }
        return toAjax(deviceService.insertDevice(device));
    }

    /**
     * 修改装置
     */
    @Log(title = "装置管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Device device)
    {
        if (!deviceService.checkDeviceIdUnique(device))
        {
            return error("修改装置【" + device.getName() + "】失败，装置编号已存在");
        }
        return toAjax(deviceService.updateDevice(device));
    }

    /**
     * 修改装置状态
     */
    @Log(title = "装置管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestParam Long id, @RequestParam String status)
    {
        return toAjax(deviceService.changeStatus(id, status));
    }

    /**
     * 删除装置
     */
    @Log(title = "装置管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(deviceService.deleteDeviceByIds(ids));
    }

    /**
     * 导出装置数据
     */
    @Log(title = "装置管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Device device)
    {
        List<Device> list = deviceService.selectDeviceList(device);
        ExcelUtil<Device> util = new ExcelUtil<>(Device.class);
        util.exportExcel(response, list, "装置数据");
    }

    /**
     * 导出装置数据
     */
    @PostMapping("/pushEvent")
    public void pushEvent(@RequestBody ReaderEvent readerEvent)
    {
        deviceService.pushEvent(readerEvent);
    }


}
