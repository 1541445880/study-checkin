package com.study.checkin.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 热力图数据点：某天的学习时长
 */
@Data
public class HeatmapVO {

    private LocalDate date;

    private Integer minutes;
}
