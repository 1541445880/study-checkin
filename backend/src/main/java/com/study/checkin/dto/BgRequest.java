package com.study.checkin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新个人主页背景图请求
 */
@Data
public class BgRequest {

    @NotBlank(message = "背景图地址不能为空")
    private String url;
}
