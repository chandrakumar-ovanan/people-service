package com.plavonra.ai.embedding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

@ExtendWith(MockitoExtension.class)
class VectorStoreServiceTest {

  @Mock private VectorStore vectorStore;

  private VectorStoreService vectorStoreService;

  @BeforeEach
  void setUp() {
    vectorStoreService = new VectorStoreService(vectorStore, new SearchProperties(5, 0.7));
  }

  @Test
  void upsert_addsDocumentToStore() {
    UUID documentId = UUID.randomUUID();
    String text = "note text";
    var metadata = java.util.Map.<String, Object>of("key", "value");

    vectorStoreService.upsert(documentId, text, metadata);

    ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
    verify(vectorStore).add(captor.capture());
    List<Document> docs = captor.getValue();
    assertThat(docs).hasSize(1);
    assertThat(docs.getFirst().getId()).isEqualTo(documentId.toString());
    assertThat(docs.getFirst().getText()).isEqualTo(text);
    assertThat(docs.getFirst().getMetadata()).isEqualTo(metadata);
  }

  @Test
  void search_buildsSearchRequestAndReturnsResults() {
    Document doc = new Document("id", "content", java.util.Map.of());
    when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

    List<Document> result = vectorStoreService.search("  QUERY  ");

    assertThat(result).containsExactly(doc);
    ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
    verify(vectorStore).similaritySearch(captor.capture());
    assertThat(captor.getValue().getQuery()).isEqualTo("query");
  }
}
