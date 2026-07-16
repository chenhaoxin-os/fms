package com.fms.system.domain;

import lombok.Data;

@Data
public class TagState {
    private String epc;
    private long lastSeen;                 // 最后出现时间戳
    private long seriesStart;              // 当前连续序列的开始时间
    private boolean inCar;                 // 是否在车
    private long carEnterTime;             // 进入在车的时间戳
}
