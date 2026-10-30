package com.study.checkin.vo;

import lombok.Data;

import java.util.List;

/**
 * 统计总览返回
 */
@Data
public class StatsVO {

    private Integer streak;          // 连续打卡天数

    private Long totalDays;          // 累计打卡天数

    private Long totalMinutes;       // 累计学习时长(分钟)

    private Integer todayMinutes;    // 今日学习时长(0 表示未打卡)

    private Boolean todayChecked;    // 今日是否已打卡

    private Integer dailyGoal;       // 每日学习目标(分钟)

    private List<HeatmapVO> heatmap; // 近一年热力图数据

    private List<MoodStatVO> moodStats; // 心情分布（饼图数据，按次数降序）
}
