package com.plavonra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plavonra.ai.model.PersonNoteAnalysisResult;
import com.plavonra.people.service.PersonNoteAnalysisService;
import com.plavonra.people.service.PersonNoteEmbeddingService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatProperties;

@ExtendWith(MockitoExtension.class)
class PersonNoteAiFacadeTest {

  @Mock private com.plavonra.ai.chat.PersonNoteAnalysisAiClient analysisClient;
  @Mock private PersonNoteAnalysisService analysisService;
  @Mock private PersonNoteEmbeddingService embeddingService;
  @Mock private OllamaChatProperties ollamaChatProperties;

  @InjectMocks private PersonNoteAiFacade personNoteAiFacade;

  @Test
  void process_whenAnalysisSucceeds_returnsSummary() {
    UUID personId = UUID.randomUUID();
    String note = "note";
    when(ollamaChatProperties.getModel()).thenReturn("model");
    when(analysisClient.analyze(note)).thenReturn(new PersonNoteAnalysisResult("t", "s"));

    var result = personNoteAiFacade.process(personId, note);

    assertThat(result.analysisDone()).isTrue();
    assertThat(result.embeddingDone()).isTrue();
    verify(analysisService).upsertAiAnalysis(eq(personId), any(), eq("model"));
    verify(embeddingService).upsertEmbedding(eq(personId), eq(note), eq("model"), eq(null));
  }

  @Test
  void process_whenAnalysisReturnsNull_returnsAnalysisNotDone() {
    UUID personId = UUID.randomUUID();
    when(ollamaChatProperties.getModel()).thenReturn("m");
    when(analysisClient.analyze(anyString())).thenReturn(null);

    var result = personNoteAiFacade.process(personId, "note");

    assertThat(result.analysisDone()).isFalse();
    assertThat(result.embeddingDone()).isTrue();
  }

  @Test
  void process_whenAnalysisThrows_returnsAnalysisNotDone() {
    UUID personId = UUID.randomUUID();
    when(ollamaChatProperties.getModel()).thenReturn("m");
    when(analysisClient.analyze(anyString())).thenThrow(new RuntimeException("fail"));

    var result = personNoteAiFacade.process(personId, "note");

    assertThat(result.analysisDone()).isFalse();
    assertThat(result.embeddingDone()).isTrue();
  }
}
