package com.study.checkin.controller;

import com.study.checkin.dto.ChatRequest;
import com.study.checkin.service.DoubaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private DoubaoService doubaoService;

    /** 流式对话：POST /api/chat/stream，返回 SSE 流 */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody ChatRequest req) {
        return doubaoService.stream(req);
    }
}
