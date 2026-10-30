package com.study.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 每日一句名言实体，对应 daily_quote 表
 */
@Data
@TableName("daily_quote")
public class Quote {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private String author;

    private LocalDateTime createdAt;
}
