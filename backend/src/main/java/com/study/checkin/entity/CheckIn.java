package com.study.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 打卡记录实体，对应 check_in 表
 */
@Data
@TableName("check_in")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate checkDate;

    private Integer studyMinutes;

    private String content;

    private String mood;

    /** 打卡图片URL列表（数据库存 JSON 数组字符串） */
    private String images;

    /** 图片URL列表（返回给前端的数组，不落库） */
    @TableField(exist = false)
    private List<String> imageList;

    private LocalDateTime createdAt;
}
