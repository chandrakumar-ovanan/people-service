package com.plavonra.people.service;

import com.plavonra.ai.embedding.VectorStoreService;
import com.plavonra.ai.model.VectorSource;
import com.plavonra.people.mapper.ActionSuggestionMapper;
import com.plavonra.services.people.api.model.ActionSuggestionResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionSuggestionService {
  private final VectorStoreService vectorStoreService;
  private final ActionSuggestionMapper actionSuggestionMapper;

  public ActionSuggestionResponse suggest(String text) {

    log.info("AI suggestion requested for text='{}'", text);

    List<Document> documents = vectorStoreService.search(text);
    log.debug("Vector search returned {} documents", documents.size());

    if (CollectionUtils.isEmpty(documents)) {
      log.warn("No similar cases found. Falling back to REVIEW");
      return actionSuggestionMapper.toResponse(text, "REVIEW", 0.0, List.of());
    }

    Map<String, Long> counts =
        documents.stream()
            .map(Document::getMetadata)
            .filter(m -> VectorSource.PERSON_NOTE.name().equals(m.get("source")))
            .map(m -> m.get("action"))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    log.debug("Action distribution: {}", counts);

    Map.Entry<String, Long> winner =
        counts.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow();

    String action = winner.getKey();
    long matches = winner.getValue();
    int total = documents.size();

    double confidenceScore = (double) matches / total;

    log.info(
        "AI decision: action='{}', matches={}/{}, confidence={}",
        action,
        matches,
        total,
        String.format("%.2f", confidenceScore));

    return actionSuggestionMapper.toResponse(text, action, confidenceScore, documents);
  }
}
