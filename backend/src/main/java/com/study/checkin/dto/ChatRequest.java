package com.study.checkin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 聊天请求：用户最新消息 + 历史对话（多轮上下文）
 */
@Data
public class ChatRequest {

    @NotBlank(message = "消息不能为空")
    private String message;

    /**
     * 历史对话，每项 {role: "user"/"assistant", content: "..."}
     * 可不传（首次对话）
     */
    private List<ChatMessage> history;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
