package org.example.agent.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class Recognition {
    
    @Value("${deepseek.api-key}")
    private String apiKey;
    
    @Value("${deepseek.base-url}")
    private String baseUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public enum IntentType {
        WEATHER,
        GENERAL,
        CODING,
        WRITING,
        TRANSLATION,
        ANALYSIS,
        UNKNOWN
    }
    
    public static class IntentResult {
        private IntentType intent;
        private Map<String, Object> extractedParams;
        private double confidence;
        
        public IntentResult(IntentType intent, Map<String, Object> params, double confidence) {
            this.intent = intent;
            this.extractedParams = params;
            this.confidence = confidence;
        }
        
        public IntentType getIntent() { return intent; }
        public Map<String, Object> getExtractedParams() { return extractedParams; }
        public double getConfidence() { return confidence; }
    }
    
    public IntentResult recognizeIntent(String message) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(message);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);
            
            Map<String, Object> request = new HashMap<>();
            request.put("model", "deepseek-chat");
            request.put("messages", messages);
            request.put("temperature", 0.3);
            request.put("max_tokens", 200);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, entity, Map.class);
            
            Map<String, Object> body = response.getBody();
            if (body != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    String aiResponse = messageObj.get("content").toString().trim();
                    
                    return parseIntentResponse(aiResponse);
                }
            }
            
            return new IntentResult(IntentType.GENERAL, new HashMap<>(), 0.0);
            
        } catch (Exception e) {
            System.err.println("意图识别失败: " + e.getMessage());
            e.printStackTrace();
            return new IntentResult(IntentType.GENERAL, new HashMap<>(), 0.0);
        }
    }
    
    private String buildSystemPrompt() {
        return "你是一个意图识别助手。请分析用户的输入，判断其意图类型，并提取关键参数。\n" +
               "\n" +
               "支持的意图类型：\n" +
               "1. weather - 天气查询（如：北京天气、今天上海怎么样、查一下广州的天气预报）\n" +
               "2. general - 普通对话、问答、聊天\n" +
               "3. coding - 编程、代码相关问题\n" +
               "4. writing - 写作、创作、润色\n" +
               "5. translation - 翻译\n" +
               "6. analysis - 数据分析、统计\n" +
               "\n" +
               "对于天气查询，需要提取：\n" +
               "- city: 城市名称（支持中文、拼音、英文）\n" +
               "- time: 时间（今天/明天/后天，可选）\n" +
               "\n" +
               "请以JSON格式返回，包含以下字段：\n" +
               "{\n" +
               "  \"intent\": \"意图类型\",\n" +
               "  \"confidence\": 置信度(0-1之间的小数),\n" +
               "  \"params\": {\n" +
               "    \"city\": \"城市名（如果是天气查询）\",\n" +
               "    \"time\": \"时间（如果有）\"\n" +
               "  }\n" +
               "}\n" +
               "\n" +
               "示例1：\n" +
               "用户：北京今天天气怎么样？\n" +
               "返回：{\"intent\":\"weather\",\"confidence\":0.95,\"params\":{\"city\":\"北京\",\"time\":\"今天\"}}\n" +
               "\n" +
               "示例2：\n" +
               "用户：帮我写一段Python代码\n" +
               "返回：{\"intent\":\"coding\",\"confidence\":0.9,\"params\":{}}\n" +
               "\n" +
               "示例3：\n" +
               "用户：你好，你是谁？\n" +
               "返回：{\"intent\":\"general\",\"confidence\":0.98,\"params\":{}}\n" +
               "\n" +
               "示例4：\n" +
               "用户：分析一下这个数据\n" +
               "返回：{\"intent\":\"analysis\",\"confidence\":0.9,\"params\":{}}\n" +
               "\n" +
               "注意：\n" +
               "1. 只返回JSON，不要有其他文字\n" +
               "2. 置信度要根据关键词和语义判断\n" +
               "3. 城市名保持用户输入的原始形式";
    }
    
    private String buildUserPrompt(String message) {
        return "请分析以下用户输入的意图：\n\n用户：" + message;
    }
    
    private IntentResult parseIntentResponse(String jsonResponse) {
        try {
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                return new IntentResult(IntentType.GENERAL, new HashMap<>(), 0.0);
            }
            
            Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
            
            String intentStr = (String) response.get("intent");
            if (intentStr == null || intentStr.trim().isEmpty()) {
                return new IntentResult(IntentType.GENERAL, new HashMap<>(), 0.0);
            }
            
            Double confidence = (Double) response.get("confidence");
            Map<String, Object> params = (Map<String, Object>) response.get("params");
            
            if (params == null) {
                params = new HashMap<>();
            }
            
            IntentType intent;
            try {
                intent = IntentType.valueOf(intentStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("未知的意图类型: " + intentStr);
                return new IntentResult(IntentType.GENERAL, params, 0.0);
            }
            
            if (confidence == null) {
                confidence = 0.5;
            }
            
            return new IntentResult(intent, params, confidence);
            
        } catch (Exception e) {
            System.err.println("解析意图响应失败: " + e.getMessage());
            return new IntentResult(IntentType.GENERAL, new HashMap<>(), 0.0);
        }
    }
}
