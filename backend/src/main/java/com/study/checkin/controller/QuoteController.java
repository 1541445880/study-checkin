package com.study.checkin.controller;

import com.study.checkin.common.Result;
import com.study.checkin.entity.Quote;
import com.study.checkin.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    /** 随机返回一句名言（首页"每日一句"） */
    @GetMapping("/random")
    public Result<Quote> random() {
        return Result.ok(quoteService.random());
    }
}
