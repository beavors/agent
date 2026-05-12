package org.example.agent.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller1 {
    private static List<Map<String, String>> messageHistory = new ArrayList<>();
    
    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private skill_whether weatherService;

    @GetMapping("/chat")
    public String chat(@RequestParam String message, 
                      @RequestParam(required = false, defaultValue = "deepseek-chat") String model,
                      @RequestParam(required = false, defaultValue = "general") String skill) {
        try {
            if ("weather".equals(skill)) {
                return weatherService.handleWeatherQuery(message);
            }

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messageHistory.add(userMsg);

            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("messages", messageHistory);

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
                    String aiAnswer = messageObj.get("content").toString();
                    Map<String, String> aiMsg = new HashMap<>();
                    aiMsg.put("role", "assistant");
                    aiMsg.put("content", aiAnswer);
                    messageHistory.add(aiMsg);
                    
                    StringBuilder allHistory = new StringBuilder();
                    allHistory.append("=====================\n");
                    allHistory.append("【完整对话上下文】\n");
                    allHistory.append("=====================\n");
                    for (Map<String, String> msg : messageHistory) {
                        allHistory.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
                    }
                    System.out.println(allHistory.toString());
                    
                    return aiAnswer;
                }
            }
            return "未收到AI回复";
        } catch (Exception e) {
            e.printStackTrace();
            return "调用失败: " + e.getMessage();
        }
    }

    @GetMapping("/clear")
    public String clear() {
        messageHistory.clear();
        return "对话历史已清空";
    }

}


