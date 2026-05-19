package com.fms.system.mapper;

import java.util.List;
import com.fms.system.domain.CardIssue;

/**
 * 发卡管理Mapper接口
 *
 * @author ruoyi
 */
public interface CardIssueMapper
{
    /**
     * 查询发卡管理列表
     *
     * @param cardIssue 查询条件
     * @return 发卡管理集合
     */
    public List<CardIssue> selectCardIssueList(CardIssue cardIssue);

    /**
     * 根据ID查询发卡管理
     *
     * @param id 主键ID
     * @return 发卡管理
     */
    public CardIssue selectCardIssueById(Long id);

    /**
     * 新增发卡管理
     *
     * @param cardIssue 发卡管理
     * @return 结果
     */
    public int insertCardIssue(CardIssue cardIssue);

    /**
     * 修改发卡管理
     *
     * @param cardIssue 发卡管理
     * @return 结果
     */
    public int updateCardIssue(CardIssue cardIssue);

    /**
     * 删除发卡管理
     *
     * @param id 主键ID
     * @return 结果
     */
    public int deleteCardIssueById(Long id);

    /**
     * 批量删除发卡管理
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteCardIssueByIds(Long[] ids);

    /**
     * 根据人员编号查询记录
     *
     * @param personCode 人员编号
     * @return 结果
     */
    public CardIssue checkPersonCodeUnique(String personCode);

    /**
     * 根据用户ID批量删除发卡管理记录
     * @param userIds
     */
    void deleteUserByIds(Long[] userIds);
}