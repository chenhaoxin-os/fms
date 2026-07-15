package com.fms.system.domain;

import com.fms.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

/**
 * 发卡管理对象 card_issue
 *
 * @author ruoyi
 */
public class CardIssue extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 持有人姓名 */
    @NotBlank(message = "持有人姓名不能为空")
    private String holderName;

    /** 人员编号 */
    @NotBlank(message = "人员编号不能为空")
    private String personCode;

    /** 卡类别（主卡/副卡） */
    @NotBlank(message = "卡类别不能为空")
    private String cardType;

    /** 标识牌十进制编码 */
    private String decimalCode;

    /** EPC编码 */
    private String epcCode;

    /** 发卡时间 */
    private Date issueTime;

    /** 卡状态（0待发卡 1已发卡 2已注销） */
    private String cardStatus;

    /** 用户id */
    private Long userId;

    private String userNumber;
    // getter/setter 省略（请使用IDE生成或Lombok）
    // 建议使用Lombok @Data，但若依默认无Lombok，需手动生成
    // 以下仅展示关键字段，实际需补充所有get/set

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public String getPersonCode() { return personCode; }
    public void setPersonCode(String personCode) { this.personCode = personCode; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public String getDecimalCode() { return decimalCode; }
    public void setDecimalCode(String decimalCode) { this.decimalCode = decimalCode; }
    public String getEpcCode() { return epcCode; }
    public void setEpcCode(String epcCode) { this.epcCode = epcCode; }
    public Date getIssueTime() { return issueTime; }
    public void setIssueTime(Date issueTime) { this.issueTime = issueTime; }
    public String getCardStatus() { return cardStatus; }
    public void setCardStatus(String cardStatus) { this.cardStatus = cardStatus; }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    @Override
    public String toString() {
        return "CardIssue{" +
                "id=" + id +
                ", holderName='" + holderName + '\'' +
                ", personCode='" + personCode + '\'' +
                ", cardType='" + cardType + '\'' +
                ", decimalCode='" + decimalCode + '\'' +
                ", epcCode='" + epcCode + '\'' +
                ", issueTime=" + issueTime +
                ", cardStatus='" + cardStatus + '\'' +
                ", userId=" + userId +
                ", userNumber='" + userNumber + '\'' +
                '}';
    }
}
