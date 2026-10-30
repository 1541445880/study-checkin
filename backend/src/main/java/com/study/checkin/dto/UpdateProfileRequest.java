package com.study.checkin.dto;

import lombok.Data;

/**
 * 修改个人资料请求：昵称 + 出生日期 + 个性签名 + 省市区
 */
@Data
public class UpdateProfileRequest {

    private String nickname;

    /** 出生日期，格式 yyyy-MM-dd，可空（表示清除） */
    private String birthday;

    /** 个性签名，可空 */
    private String signature;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 性别：男/女 */
    private String gender;

    /** 每日学习目标(分钟) */
    private Integer dailyGoal;
}
