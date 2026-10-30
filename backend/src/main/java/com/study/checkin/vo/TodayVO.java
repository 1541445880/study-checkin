package com.study.checkin.vo;

import lombok.Data;

/**
 * 今日打卡状态（聚合：支持一天多次打卡）
 */
@Data
public class TodayVO {

    private Boolean todayChecked;   // 今日是否已有打卡

    private Integer todayMinutes;   // 今日累计学习时长(分钟)

    private Long todayCount;        // 今日打卡次数
}
