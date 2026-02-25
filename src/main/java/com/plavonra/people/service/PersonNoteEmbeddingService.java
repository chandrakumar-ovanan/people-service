package com.plavonra.people.service;

import com.plavonra.ai.embedding.VectorStoreService;
import com.plavonra.ai.model.VectorSource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonNoteEmbeddingService {

  private final VectorStoreService vectorStoreService;

  public static UUID documentIdForPerson(UUID personId) {
    return UUID.nameUUIDFromBytes(
        ("person:%s".formatted(personId)).getBytes(StandardCharsets.UTF_8));
  }

  public void deleteEmbedding(UUID personId) {
    UUID documentId = documentIdForPerson(personId);
    vectorStoreService.delete(List.of(documentId.toString()));
  }

  public void upsertEmbedding(UUID personId, String text, String model, String action) {
    UUID documentId = documentIdForPerson(personId);
    String actionValue = action != null ? action : "UPDATED";
    vectorStoreService.upsert(
        documentId,
        text,
        Map.of(
            "personId",
            personId.toString(),
            "model",
            model,
            "source",
            VectorSource.PERSON_NOTE,
            "action",
            actionValue));
  }
}
