package com.plavonra.people.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plavonra.ai.embedding.VectorStoreService;
import com.plavonra.people.mapper.ActionSuggestionMapper;
import com.plavonra.services.people.api.model.ActionInfo;
import com.plavonra.services.people.api.model.ActionSuggestionResponse;
import com.plavonra.services.people.api.model.BasedOn;
import com.plavonra.services.people.api.model.ConfidenceInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

@ExtendWith(MockitoExtension.class)
class ActionSuggestionServiceTest {

  @Mock private VectorStoreService vectorStoreService;
  @Mock private ActionSuggestionMapper actionSuggestionMapper;

  @InjectMocks private ActionSuggestionService actionSuggestionService;

  @Nested
  class Suggest {

    @Test
    void emptySearchResult_returnsNoneSuggestion() {
      when(vectorStoreService.search(anyString())).thenReturn(Collections.emptyList());
      ActionSuggestionResponse stubResponse = new ActionSuggestionResponse();
      stubResponse.setInput("some text");
      ActionInfo action = new ActionInfo();
      action.setCode("REVIEW");
      stubResponse.setAction(action);
      ConfidenceInfo confidence = new ConfidenceInfo();
      confidence.setLevel(ConfidenceInfo.LevelEnum.LOW);
      confidence.setScore(0.0);
      confidence.setLabel("Low confidence");
      stubResponse.setConfidence(confidence);
      BasedOn basedOn = new BasedOn();
      basedOn.setCount(0);
      basedOn.setSimilarCases(List.of());
      stubResponse.setBasedOn(basedOn);
      when(actionSuggestionMapper.toResponse(eq("some text"), eq("REVIEW"), eq(0.0), anyList()))
          .thenReturn(stubResponse);

      ActionSuggestionResponse result = actionSuggestionService.suggest("some text");

      assertThat(result).isNotNull();
      assertThat(result.getInput()).isEqualTo("some text");
      assertThat(result.getAction().getCode()).isEqualTo("REVIEW");
      assertThat(result.getConfidence().getLevel()).isEqualTo(ConfidenceInfo.LevelEnum.LOW);
    }

    @Test
    void searchResult_returnsMappedResponse() {
      Document doc =
          new Document("id", "text", Map.of("source", "PERSON_NOTE", "action", "REFUND"));
      when(vectorStoreService.search("query")).thenReturn(List.of(doc));
      ActionInfo action = new ActionInfo();
      action.setCode("REFUND");
      ActionSuggestionResponse expected = new ActionSuggestionResponse();
      expected.setAction(action);
      when(actionSuggestionMapper.toResponse(anyString(), anyString(), anyDouble(), anyList()))
          .thenReturn(expected);

      ActionSuggestionResponse result = actionSuggestionService.suggest("query");

      assertThat(result).isSameAs(expected);
      verify(actionSuggestionMapper).toResponse(eq("query"), eq("REFUND"), anyDouble(), anyList());
    }
  }
}
