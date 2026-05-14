package org.example.agent.RAG;

import org.apache.commons.io.FileUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class RagService {

    @Autowired(required = false)
    private VectorStore vectorStore;

    @Autowired(required = false)
    private Function<List<Document>, List<Document>> tokenTextSplitter;

    private final Map<String, Integer> documentChunks = new ConcurrentHashMap<>();

    public void loadDocToVectorStore(File txtFile) throws Exception {
        if (vectorStore == null) {
            throw new IllegalStateException("向量库未配置");
        }
        
        String content = FileUtils.readFileToString(txtFile, StandardCharsets.UTF_8);
        Document doc = new Document(content);
        List<Document> splitDocs = splitDocument(doc);
        vectorStore.add(splitDocs);
        
        String docId = UUID.randomUUID().toString();
        documentChunks.put(docId, splitDocs.size());
    }

    public void loadTextToVectorStore(String text) throws Exception {
        if (vectorStore == null) {
            throw new IllegalStateException("向量库未配置");
        }
        
        Document doc = new Document(text);
        List<Document> splitDocs = splitDocument(doc);
        vectorStore.add(splitDocs);
        
        String docId = UUID.randomUUID().toString();
        documentChunks.put(docId, splitDocs.size());
    }

    public void uploadAndProcessFile(MultipartFile file) throws Exception {
        if (vectorStore == null) {
            throw new IllegalStateException("向量库未配置");
        }
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("只支持 .txt 文本文件");
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        
        String docId = UUID.randomUUID().toString();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", originalFilename);
        metadata.put("uploadTime", String.valueOf(System.currentTimeMillis()));
        metadata.put("docId", docId);
        
        Document doc = new Document(content, metadata);
        
        List<Document> splitDocs = splitDocument(doc);
        vectorStore.add(splitDocs);
        
        documentChunks.put(docId, splitDocs.size());
        
        System.out.println("文件上传成功: " + originalFilename + ", 切片数: " + splitDocs.size());
    }

    private List<Document> splitDocument(Document doc) {
        if (tokenTextSplitter != null) {
            return tokenTextSplitter.apply(List.of(doc));
        } else {
            return List.of(doc);
        }
    }

    public String retrieveContext(String question) {
        if (vectorStore == null) {
            return "";
        }
        
        SearchRequest request = SearchRequest.query(question).withTopK(3);

        List<Document> docs = vectorStore.similaritySearch(request);
        
        if (docs == null || docs.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        for (Document d : docs) {
            context.append(d.getContent()).append("\n\n");
        }
        return context.toString();
    }

    public void clearVectorStore() {
        documentChunks.clear();
        System.out.println("向量库已清空");
    }

    public int getDocumentCount() {
        return documentChunks.size();
    }
}
