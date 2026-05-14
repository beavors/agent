package org.example.agent.Controller;

import org.example.agent.RAG.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rag")
@CrossOrigin(origins = "*")
public class RagController {

    @Autowired
    private RagService ragService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            ragService.uploadAndProcessFile(file);

            response.put("success", true);
            response.put("message", "文件上传并处理成功");
            response.put("documentCount", ragService.getDocumentCount());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "文件处理失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/upload-text")
    public ResponseEntity<Map<String, Object>> uploadText(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("文本内容不能为空");
            }

            ragService.loadTextToVectorStore(text);

            response.put("success", true);
            response.put("message", "文本上传成功");
            response.put("documentCount", ragService.getDocumentCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "文本处理失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearVectorStore() {
        Map<String, Object> response = new HashMap<>();

        try {
            ragService.clearVectorStore();

            response.put("success", true);
            response.put("message", "向量库已清空");
            response.put("documentCount", 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清空失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            int count = ragService.getDocumentCount();

            response.put("success", true);
            response.put("documentCount", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}