package org.example.agent;

    import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    @RestController
    public class ChatController {

        // 自动读取配置文件里的密钥和地址
        @Value("${deepseek.api-key}")
        private String apiKey;

        @Value("${deepseek.base-url}")
        private String baseUrl;

        // Spring 自带的请求工具，不用管原理
        @Autowired
        private RestTemplate restTemplate;

        // 对话接口：浏览器访问就能和AI聊天
        @GetMapping("/chat")
        public String chat(String message) {

            // 1. 构造请求参数
            Map<String, Object> request = new HashMap<>();
            request.put("model", "deepseek-chat");
            request.put("messages", List.of(
                    Map.of("role", "user", "content", message)
            ));

            // 2. 请求头（带API密钥）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. 发送请求给DeepSeek
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, entity, Map.class);

            // 4. 解析AI返回的回答
            Map<String, Object> body = response.getBody();
            if (body != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    return "AI 回答：" + messageObj.get("content");
                }
            }
            return "调用失败，检查API Key";
        }
    }

