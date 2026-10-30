package com.study.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.checkin.entity.Quote;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuoteMapper extends BaseMapper<Quote> {
}
