package com.fms.system.service.impl;

import java.util.Date;
import java.util.List;
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
        if (!checkPersonCodeUnique(cardIssue))
        {
            throw new ServiceException("人员编号【" + cardIssue.getPersonCode() + "】已存在");
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
        if (card == null)
        {
            throw new ServiceException("记录不存在");
        }
        // 只有待发卡或已注销状态可以发卡
        if (!"0".equals(card.getCardStatus()) && !"2".equals(card.getCardStatus()))
        {
            throw new ServiceException("当前状态不允许发卡，仅支持待发卡或已注销状态");
        }
        CardIssue update = new CardIssue();
        update.setId(id);
        update.setDecimalCode(decimalCode);
        update.setEpcCode(epcCode);
        update.setIssueTime(new Date());
        update.setCardType(cardType);
        update.setCardStatus("1"); // 已发卡
        int rows = cardIssueMapper.updateCardIssue(update);
        return rows > 0;
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
}
