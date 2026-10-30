package com.study.checkin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 心情分布统计项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodStatVO {

    private String mood;      // 心情标签，如 "专注"

    private Integer count;    // 出现次数
}
