package org.example.agent.RAG;

import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
public class RagConfig {

    @Bean
    public Function<List<Document>, List<Document>> tokenTextSplitter() {
        return documents -> {
            // 简单的文本分割逻辑，按段落分割
            return documents.stream()
                .flatMap(doc -> {
                    String text = doc.getContent();
                    String[] paragraphs = text.split("\n\n+");
                    return java.util.Arrays.stream(paragraphs)
                        .filter(p -> !p.trim().isEmpty())
                        .map(p -> new Document(p, doc.getMetadata()));
                })
                .toList();
        };
    }
}
