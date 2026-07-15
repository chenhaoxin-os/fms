package com.fms.system.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fms.common.core.domain.entity.SysUser;
import com.fms.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fms.common.constant.UserConstants;
import com.fms.common.exception.ServiceException;
import com.fms.common.utils.StringUtils;
import com.fms.system.domain.CardIssue;
import com.fms.system.mapper.CardIssueMapper;
import com.fms.system.service.ICardIssueService;

/**
 * 发卡管理Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class CardIssueServiceImpl implements ICardIssueService
{
    @Autowired
    private CardIssueMapper cardIssueMapper;

    @Autowired
    private SysUserMapper userMapper;
    @Override
    public List<CardIssue> selectCardIssueList(CardIssue cardIssue)
    {
        return cardIssueMapper.selectCardIssueList(cardIssue);
    }

    @Override
    public CardIssue selectCardIssueById(Long id)
    {
        return cardIssueMapper.selectCardIssueById(id);
    }

    @Override
    public int insertCardIssue(CardIssue cardIssue)
    {
        // 移除原唯一校验，改为检查该人员卡片总数是否已达上限（含所有状态）
        int count = cardIssueMapper.countByPersonCode(cardIssue.getPersonCode());
        if (count >= 3) {
            throw new ServiceException("该人员卡片记录已达3条，无法再新增");
        }
        return cardIssueMapper.insertCardIssue(cardIssue);
    }

    @Override
    public int updateCardIssue(CardIssue cardIssue)
    {
        return cardIssueMapper.updateCardIssue(cardIssue);
    }

    @Override
    public int deleteCardIssueByIds(Long[] ids)
    {
        return cardIssueMapper.deleteCardIssueByIds(ids);
    }

    @Override
    @Transactional
    public boolean issueCard(Long id, String decimalCode, String epcCode,String cardType)
    {
        CardIssue card = cardIssueMapper.selectCardIssueById(id);
        if (card == null) {
            throw new ServiceException("记录不存在");
        }
        // 只有待发卡或已注销状态可以发卡
        if (!"0".equals(card.getCardStatus()) && !"2".equals(card.getCardStatus())) {
            throw new ServiceException("当前状态不允许发卡，仅支持待发卡或已注销状态");
        }

        // 检查该人员已发卡数量是否达到3张
        int issuedCount = cardIssueMapper.countIssuedByPersonCode(card.getPersonCode());
        if (issuedCount >= 3) {
            throw new ServiceException("该人员已绑定3张卡，无法再发卡");
        }

        CardIssue update = new CardIssue();
        update.setId(id);
        update.setDecimalCode(decimalCode);
        update.setEpcCode(epcCode);
        update.setIssueTime(new Date());
        update.setCardType(cardType);
        update.setCardStatus("1");
        int rows = cardIssueMapper.updateCardIssue(update);
        return rows > 0;
    }
    /**
     * 校验卡类型数量限制（主卡≤1，副卡≤2）
     * @param personCode 人员编码
     * @param cardType   卡类型
     * @param excludeId  需排除的记录ID（插入时传null）
     */
    private void validateCardTypeLimit(String personCode, String cardType, Long excludeId) {
        if (StringUtils.isBlank(personCode) || StringUtils.isBlank(cardType)) {
            throw new ServiceException("人员编码和卡类型不能为空");
        }
        // 仅校验主卡和副卡，其他类型（如有）暂不限制
        if (!"主卡".equals(cardType) && !"副卡".equals(cardType)) {
            return; // 其他类型不限制
        }
        int count = cardIssueMapper.countByPersonCodeAndCardType(personCode, cardType, excludeId);
        if ("主卡".equals(cardType) && count >= 1) {
            throw new ServiceException("该人员已存在主卡，不能再新增主卡");
        }
        if ("副卡".equals(cardType) && count >= 2) {
            throw new ServiceException("该人员已存在两张副卡，不能再新增副卡");
        }
    }
    @Override
    @Transactional
    public boolean cancelCard(Long id)
    {
        CardIssue card = cardIssueMapper.selectCardIssueById(id);
        if (card == null)
        {
            throw new ServiceException("记录不存在");
        }
        // 只有已发卡状态可以注销
        if (!"1".equals(card.getCardStatus()))
        {
            throw new ServiceException("当前状态不允许注销，仅支持已发卡状态");
        }
        CardIssue update = new CardIssue();
        update.setId(id);
        update.setCardStatus("2"); // 已注销
        // 不清除编码，保留记录
        int rows = cardIssueMapper.updateCardIssue(update);
        return rows > 0;
    }

    @Override
    public boolean checkPersonCodeUnique(CardIssue cardIssue)
    {
        Long id = cardIssue.getId() == null ? -1L : cardIssue.getId();
        CardIssue info = cardIssueMapper.checkPersonCodeUnique(cardIssue.getPersonCode());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public Map<String, String> generateCode(String personCode, String cardType) {
        SysUser user = userMapper.selectUserByUserName(personCode);
        if (user == null) {
            throw new ServiceException("人员不存在");
        }
        // 校验必要字段
        if (StringUtils.isAnyBlank(user.getPersonCategory(), user.getSex(), user.getBloodType())) {
            throw new ServiceException("人员信息不完整，无法生成编码");
        }
        String suffix = buildSuffix(user,cardType);
        String decimalCode =suffix;
        String epcCode = decimalCode + "FFFFFFFFF";

        Map<String, String> result = new HashMap<>();
        result.put("decimalCode", decimalCode);
        result.put("epcCode", epcCode);
        return result;
    }

    @Override
    public String getNameByEpcCode(String epcCode) {
        return cardIssueMapper.getNameByEpcCode(epcCode);
    }

    private String  buildSuffix(SysUser user,String cardType) {
        switch (cardType){
            case "主卡":
                cardType = "1";
                break;
            case "副卡1":
                cardType = "2";
                break;
            case "副卡2":
                cardType = "3";
                break;
            default:
                throw new ServiceException("未知的卡类型");
        }
        return user.getPersonCategory()+user.getYearOfEnlistment()+user.getSerialNo() + cardType + user.getSex() + user.getBloodType() + user.getHistoryOfDrugAllergy();
    }

}
