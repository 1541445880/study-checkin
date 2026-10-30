package com.study.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.checkin.entity.Quote;
import com.study.checkin.mapper.QuoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    @Autowired
    private QuoteMapper quoteMapper;

    /** 随机取一条名言 */
    public Quote random() {
        return quoteMapper.selectOne(new QueryWrapper<Quote>()
                .last("ORDER BY RAND() LIMIT 1"));
    }
}
