package com.study.checkin.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CheckInRequest {

    @NotNull(message = "学习时长不能为空")
    @Min(value = 1, message = "学习时长至少 1 分钟")
    private Integer studyMinutes;

    private String content;

    private String mood;

    /**
     * 打卡日期，格式 yyyy-MM-dd。
     * 不传默认打今天；传过去的日期即补签（不允许补未来）。
     */
    private String checkDate;

    /** 打卡图片URL列表（不限数量） */
    private java.util.List<String> images;
}
