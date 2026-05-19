package com.fms.system.service;


import java.util.List;
import com.fms.system.domain.CardIssue;

/**
 * 发卡管理Service接口
 *
 * @author ruoyi
 */
public interface ICardIssueService
{
    /**
     * 查询发卡管理列表（分页用）
     */
    public List<CardIssue> selectCardIssueList(CardIssue cardIssue);

    /**
     * 根据ID查询发卡管理
     */
    public CardIssue selectCardIssueById(Long id);

    /**
     * 新增发卡管理（通常由其他系统同步，这里提供）
     */
    public int insertCardIssue(CardIssue cardIssue);

    /**
     * 修改发卡管理
     */
    public int updateCardIssue(CardIssue cardIssue);

    /**
     * 批量删除发卡管理
     */
    public int deleteCardIssueByIds(Long[] ids);

    /**
     * 发卡操作（写卡+绑定）
     * @param id 记录ID
     * @param decimalCode 十进制编码（读卡器读取）
     * @param epcCode EPC编码（读卡器读取）
     * @return 结果
     */
    public boolean issueCard(Long id, String decimalCode, String epcCode,String cardType);

    /**
     * 注销卡片
     * @param id 记录ID
     * @return 结果
     */
    public boolean cancelCard(Long id);

    /**
     * 校验人员编号是否唯一
     */
    public boolean checkPersonCodeUnique(CardIssue cardIssue);
}
