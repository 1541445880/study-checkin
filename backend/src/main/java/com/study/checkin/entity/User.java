package com.study.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体，对应 user 表
 */
@Data
@TableName("`user`")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private LocalDate birthday;

    private String bg;

    private String signature;

    private String province;

    private String city;

    private String district;

    private String gender;

    private Integer dailyGoal;

    private LocalDateTime createdAt;
}
