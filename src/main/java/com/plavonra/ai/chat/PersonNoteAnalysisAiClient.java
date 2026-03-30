package com.plavonra.ai.chat;

import com.plavonra.ai.model.PersonNoteAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonNoteAnalysisAiClient {

  private final ChatClient chatClient;
  private final PersonNoteAnalysisPromptService personNoteAnalysisService;

  public PersonNoteAnalysisResult analyze(String note) {
    String system = personNoteAnalysisService.getSystemPrompt();
    String user = personNoteAnalysisService.buildUserPrompt(note);

    return chatClient
        .prompt()
        .system(system)
        .user(user)
        .call()
        .entity(PersonNoteAnalysisResult.class);
  }
}
