package com.plavonra.people.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import com.plavonra.ai.embedding.VectorStoreService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonNoteEmbeddingServiceTest {

  @Mock private VectorStoreService vectorStoreService;

  @InjectMocks private PersonNoteEmbeddingService personNoteEmbeddingService;

  @Test
  void upsertEmbedding_callsVectorStoreWithExpectedMetadata() {
    UUID personId = UUID.randomUUID();
    String text = "note content";
    String model = "test-model";
    String action = "CREATED";

    personNoteEmbeddingService.upsertEmbedding(personId, text, model, action);

    verify(vectorStoreService)
        .upsert(
            any(UUID.class),
            eq(text),
            org.mockito.ArgumentMatchers.argThat(
                m ->
                    personId.toString().equals(m.get("personId"))
                        && "test-model".equals(m.get("model"))
                        && "PERSON_NOTE".equals(m.get("source").toString())
                        && "CREATED".equals(m.get("action"))));
  }

  @Test
  void upsertEmbedding_whenActionNull_usesUPDATEDInMetadata() {
    UUID personId = UUID.randomUUID();
    personNoteEmbeddingService.upsertEmbedding(personId, "text", "model", null);

    verify(vectorStoreService)
        .upsert(
            any(UUID.class),
            eq("text"),
            org.mockito.ArgumentMatchers.argThat(m -> "UPDATED".equals(m.get("action"))));
  }

  @Test
  void deleteEmbedding_callsVectorStoreDeleteWithDocumentId() {
    UUID personId = UUID.randomUUID();
    personNoteEmbeddingService.deleteEmbedding(personId);

    verify(vectorStoreService)
        .delete(
            org.mockito.ArgumentMatchers.argThat(
                ids ->
                    ids.size() == 1
                        && ids.get(0)
                            .equals(
                                PersonNoteEmbeddingService.documentIdForPerson(personId)
                                    .toString())));
  }
}
