package com.plavonra.ai.chat;

import com.plavonra.ai.model.PersonNoteAnalysisResult;
import com.plavonra.ai.prompt.PromptLoader;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonNoteAnalysisAiClient {

  private final ChatClient chatClient;
  private final PromptLoader promptLoader;

  public PersonNoteAnalysisResult analyze(String note) {
    String prompt =
        promptLoader.render("prompts/person-note-analysis.prompt", Map.of("note", note));

    PersonNoteAnalysisResult response =
        chatClient.prompt(prompt).call().entity(PersonNoteAnalysisResult.class);
    ;

    return response;
  }
}
