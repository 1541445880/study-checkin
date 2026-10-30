package com.study.checkin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新头像请求
 */
@Data
public class AvatarRequest {

    @NotBlank(message = "头像地址不能为空")
    private String avatarUrl;
}
