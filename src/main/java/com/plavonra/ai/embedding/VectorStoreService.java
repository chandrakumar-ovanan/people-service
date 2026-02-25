package com.plavonra.ai.embedding;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VectorStoreService {

  private final VectorStore vectorStore;
  private final SearchProperties props;

  public void upsert(UUID documentId, String text, Map<String, Object> metadata) {
    Document document = new Document(documentId.toString(), text, metadata);
    vectorStore.add(List.of(document));
  }

  public void delete(List<String> documentIds) {
    if (documentIds == null || documentIds.isEmpty()) {
      return;
    }
    vectorStore.delete(documentIds);
  }

  public List<Document> search(String query) {
    if (query == null || query.isBlank()) {
      return List.of();
    }
    String normalizedQuery = query.toLowerCase().trim();

    SearchRequest searchRequest =
        SearchRequest.builder()
            .query(normalizedQuery)
            .topK(props.topK())
            .similarityThreshold(props.similarityThreshold())
            .build();

    return vectorStore.similaritySearch(searchRequest);
  }
}
