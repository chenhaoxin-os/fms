package com.fms.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.fms.common.annotation.Log;
import com.fms.common.core.controller.BaseController;
import com.fms.common.core.domain.AjaxResult;
import com.fms.common.core.page.TableDataInfo;
import com.fms.common.enums.BusinessType;
import com.fms.system.domain.CardIssue;
import com.fms.system.service.ICardIssueService;

/**
 * 发卡管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/cardIssue")
public class CardIssueController extends BaseController
{
    @Autowired
    private ICardIssueService cardIssueService;

    /**
     * 查询发卡管理列表
     */
    @GetMapping("/list")
    public TableDataInfo list(CardIssue cardIssue)
    {
        startPage();
        List<CardIssue> list = cardIssueService.selectCardIssueList(cardIssue);
        return getDataTable(list);
    }

    /**
     * 获取发卡管理详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(cardIssueService.selectCardIssueById(id));
    }

    /**
     * 新增发卡管理（一般用于导入人员数据）
     */
    @Log(title = "发卡管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CardIssue cardIssue)
    {
        if (!cardIssueService.checkPersonCodeUnique(cardIssue))
        {
            return error("新增失败，人员编号【" + cardIssue.getPersonCode() + "】已存在");
        }
        cardIssue.setCreateBy(getUsername());
        return toAjax(cardIssueService.insertCardIssue(cardIssue));
    }

    /**
     * 修改发卡管理
     */
    @Log(title = "发卡管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody CardIssue cardIssue)
    {
        cardIssue.setUpdateBy(getUsername());
        return toAjax(cardIssueService.updateCardIssue(cardIssue));
    }

    /**
     * 删除发卡管理
     */
    @Log(title = "发卡管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cardIssueService.deleteCardIssueByIds(ids));
    }

    /**
     * 发卡操作（写卡+绑定）
     * @param id 记录ID
     * @param decimalCode 十进制编码（读卡器获取）
     * @param epcCode EPC编码
     * @return 结果
     */
    @Log(title = "发卡管理", businessType = BusinessType.OTHER)
    @PostMapping("/issue")
    public AjaxResult issue(@RequestParam Long id, @RequestParam String decimalCode, @RequestParam String epcCode, @RequestParam String cardType)
    {
        // 这里可以增加调用读卡器硬件的逻辑，实际项目中需替换为真实读卡
        boolean result = cardIssueService.issueCard(id, decimalCode, epcCode,cardType);
        return result ? success("发卡成功") : error("发卡失败");
    }

    /**
     * 注销卡片
     */
    @Log(title = "发卡管理", businessType = BusinessType.OTHER)
    @PostMapping("/cancel")
    public AjaxResult cancel(@RequestParam Long id)
    {
        boolean result = cardIssueService.cancelCard(id);
        return result ? success("注销成功") : error("注销失败");
    }
}
