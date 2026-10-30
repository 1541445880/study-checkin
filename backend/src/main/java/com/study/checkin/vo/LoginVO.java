package com.study.checkin.vo;

import lombok.Data;

/**
 * 登录成功返回
 */
@Data
public class LoginVO {

    private String token;

    private Long userId;

    private String nickname;

    private String avatar;

    private String birthday;

    private String bg;

    private String signature;
}
