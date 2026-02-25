package com.plavonra.ai;

import com.plavonra.ai.chat.PersonNoteAnalysisAiClient;
import com.plavonra.ai.model.AiExecutionSummary;
import com.plavonra.ai.model.PersonNoteAnalysisResult;
import com.plavonra.people.service.PersonNoteAnalysisService;
import com.plavonra.people.service.PersonNoteEmbeddingService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonNoteAiFacade {

  private final PersonNoteAnalysisAiClient analysisClient;
  private final PersonNoteAnalysisService analysisService;

  private final PersonNoteEmbeddingService embeddingService;

  private final OllamaChatProperties ollamaChatProperties;

  public AiExecutionSummary process(UUID personId, String note) {
    boolean analysisDone = upsertAiAnalysis(personId, note);
    boolean embeddingDone = upsertEmbedding(personId, note);
    log.debug(
        "PersonNoteUpdatedEvent SUMMARY analysisDone={}, embeddingDone={}",
        analysisDone,
        embeddingDone);
    return new AiExecutionSummary(analysisDone, embeddingDone);
  }

  private boolean upsertAiAnalysis(UUID personId, String note) {
    try {
      log.debug("Starting person note analysis for person id {}", personId);

      PersonNoteAnalysisResult result = analysisClient.analyze(note);

      log.info("Person note analysis completed successfully for person id {}", personId);
      if (result == null) return false;
      analysisService.upsertAiAnalysis(personId, result, ollamaChatProperties.getModel());
      log.info("Person note analysis saved successfully for person id {}", personId);
      return true;
    } catch (Exception ex) {
      log.error("Failed to save person note analysis for person id {}", personId, ex);
      return false;
    }
  }

  private boolean upsertEmbedding(UUID personId, String note) {
    try {
      log.debug("Starting person note embed for person id {}", personId);
      embeddingService.upsertEmbedding(personId, note, ollamaChatProperties.getModel(), null);
      log.info("Person note embed saved successfully for person id {}", personId);
      return true;
    } catch (Exception ex) {
      log.error("Failed to save person note embedding for person id {}", personId, ex);
      return false;
    }
  }
}
