package com.study.checkin.vo;

import lombok.Data;

/**
 * 用户资料返回
 */
@Data
public class UserProfileVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    /** 出生日期 yyyy-MM-dd，未设置则为 null */
    private String birthday;

    /** 个人主页背景图 URL，未设置则为 null */
    private String bg;

    /** 个性签名，未设置则为 null */
    private String signature;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 性别：男/女/保密 */
    private String gender;

    /** 每日学习目标(分钟) */
    private Integer dailyGoal;
}
