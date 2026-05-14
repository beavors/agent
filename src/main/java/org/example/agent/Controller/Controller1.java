package org.example.agent.Controller;
import org.example.agent.RAG.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class Controller1 {
    private static final List<Map<String, String>> messageHistory = Collections.synchronizedList(new ArrayList<>());
    
    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private skill_whether weatherService;
    
    @Autowired
    private Recognition intentRecognizer;
    
    @Autowired(required = false)
    private RagService ragService;

    @GetMapping("/chat")
    public String chat(@RequestParam String message, 
                      @RequestParam(required = false, defaultValue = "deepseek-chat") String model,
                      @RequestParam(required = false, defaultValue = "auto") String skill,
                      @RequestParam(required = false, defaultValue = "true") boolean useRag) {
        try {
            if ("auto".equals(skill)) {
                Recognition.IntentResult intent = intentRecognizer.recognizeIntent(message);
                
                System.out.println("意图识别结果: " + intent.getIntent() + 
                                 ", 置信度: " + intent.getConfidence() + 
                                 ", 参数: " + intent.getExtractedParams());
                
                if ("WEATHER".equals(intent.getIntent().name()) && intent.getConfidence() > 0.7) {
                    Map<String, Object> params = intent.getExtractedParams();
                    String city = params != null ? (String) params.get("city") : null;
                    
                    if (city != null && !city.isEmpty()) {
                        System.out.println("自动调用天气查询，城市: " + city);
                        return weatherService.handleWeatherQueryWithCity(city);
                    } else {
                        return weatherService.handleWeatherQuery(message);
                    }
                }
            } else if ("weather".equals(skill)) {
                return weatherService.handleWeatherQuery(message);
            }

            String ragContext = "";
            if (useRag && ragService != null) {
                ragContext = ragService.retrieveContext(message);
                if (ragContext != null && !ragContext.isEmpty()) {
                    System.out.println("RAG 检索到相关知识: " + ragContext.substring(0, Math.min(200, ragContext.length())));
                }
            }

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messageHistory.add(userMsg);

            String systemPrompt = buildSystemPrompt(ragContext, skill);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            
            messages.addAll(messageHistory);

            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    if (messageObj != null && messageObj.get("content") != null) {
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
            }
            return "未收到AI回复";
        } catch (Exception e) {
            e.printStackTrace();
            return "调用失败: " + e.getMessage();
        }
    }

    private String buildSystemPrompt(String ragContext, String skill) {
        StringBuilder prompt = new StringBuilder();
        
        if ("coding".equals(skill)) {
            prompt.append("你是一个专业的编程专家，擅长编写、调试和优化代码。\n");
            prompt.append("请提供清晰、高效、可维护的代码解决方案。\n\n");
        } else if ("writing".equals(skill)) {
            prompt.append("你是一个专业的写作助手，擅长文章创作、润色和编辑。\n");
            prompt.append("请提供流畅、优美、有逻辑的文字内容。\n\n");
        } else if ("translation".equals(skill)) {
            prompt.append("你是一个专业的翻译专家，精通多国语言翻译。\n");
            prompt.append("请提供准确、地道、符合语境的翻译结果。\n\n");
        } else if ("analysis".equals(skill)) {
            prompt.append("你是一个数据分析专家，擅长数据处理、分析和可视化。\n");
            prompt.append("请提供专业、准确、有洞察力的分析结果。\n\n");
        } else if ("weather".equals(skill)) {
            prompt.append("你是一个天气查询助手，帮助用户查询各地天气情况。\n\n");
        } else {
            prompt.append("你是一个智能助手。\n\n");
        }
        
        if (ragContext != null && !ragContext.isEmpty()) {
            prompt.append("以下是相关的背景知识，请基于这些信息回答问题：\n");
            prompt.append("=== 背景知识 ===\n");
            prompt.append(ragContext);
            prompt.append("=== 结束 ===\n\n");
            prompt.append("如果背景知识中包含相关信息，请优先使用这些信息来回答用户的问题。\n");
            prompt.append("如果背景知识与问题无关或不足以回答问题，你可以使用自己的知识来回答。\n\n");
        }
        
        prompt.append("请用中文简洁明了地回答用户的问题。");
        
        return prompt.toString();
    }

    @GetMapping("/clear")
    public String clear() {
        messageHistory.clear();
        return "对话历史已清空";
    }

}


