package com.study.checkin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.checkin.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 豆包大模型（火山引擎方舟）流式对话服务。
 * 用 JDK 自带 HttpClient 流式调用豆包，把 SSE 结果逐块转发给前端。
 */
@Service
public class DoubaoService {

    private static final String SYSTEM_PROMPT =
            "你是一位温暖、专业的学习打卡助教。你可以：1) 陪用户聊学习、打卡、学习方法；" +
            "2) 针对学习拖延、坚持不下去给建议；3) 解答学习相关的问题。回答要简洁友好，多用鼓励，一般控制在 200 字以内。";

    @Value("${doubao.api-key}")
    private String apiKey;

    @Value("${doubao.model}")
    private String model;

    @Value("${doubao.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** 流式对话入口：返回 SseEmitter，边生成边推给前端 */
    public SseEmitter stream(ChatRequest req) {
        SseEmitter emitter = new SseEmitter(0L); // 0 = 不超时

        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("你的")) {
            safeError(emitter, "后端还没配置豆包 API Key，请在 application.yml 的 doubao.api-key 里填写");
            return emitter;
        }

        try {
            String json = buildRequestBody(req);
            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(httpReq, HttpResponse.BodyHandlers.ofInputStream())
                    .thenAccept(resp -> forward(resp, emitter))
                    .exceptionally(err -> {
                        safeError(emitter, "调用豆包失败：" + err.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            safeError(emitter, "请求构造失败：" + e.getMessage());
        }
        return emitter;
    }

    /** 读取豆包 SSE 流并逐块转发 */
    private void forward(HttpResponse<java.io.InputStream> resp, SseEmitter emitter) {
        int status = resp.statusCode();
        if (status != 200) {
            safeError(emitter, "豆包接口返回错误，HTTP " + status + "（请检查 api-key 与 model 是否填对）");
            return;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if ("[DONE]".equals(data)) {
                    break;
                }
                String text = extractContent(data);
                if (text != null && !text.isEmpty()) {
                    // 用 JSON 编码，防止内容里的换行破坏 SSE 行结构
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(text)));
                }
            }
            emitter.complete();
        } catch (Exception e) {
            safeError(emitter, "读取豆包响应中断：" + e.getMessage());
        }
    }

    /** 从豆包 SSE 的 data JSON 里提取 delta.content */
    private String extractContent(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).path("delta");
                JsonNode content = delta.path("content");
                if (content != null && content.isValueNode()) {
                    return content.asText();
                }
            }
        } catch (Exception ignored) {
            // 非 JSON 行，忽略
        }
        return null;
    }

    /** 组装请求体：system + 历史 + 最新消息 */
    private String buildRequestBody(ChatRequest req) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(msg("system", SYSTEM_PROMPT));
        if (req.getHistory() != null) {
            for (ChatRequest.ChatMessage m : req.getHistory()) {
                if (m.getRole() != null && m.getContent() != null) {
                    messages.add(msg(m.getRole(), m.getContent()));
                }
            }
        }
        messages.add(msg("user", req.getMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", true);
        body.put("temperature", 0.7);
        return objectMapper.writeValueAsString(body);
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    /** 发送错误事件并结束 */
    private void safeError(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().data("[ERROR]" + msg));
            emitter.complete();
        } catch (Exception ignored) {
        }
    }
}
