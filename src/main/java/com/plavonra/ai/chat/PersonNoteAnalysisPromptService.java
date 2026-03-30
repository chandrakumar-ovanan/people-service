package com.plavonra.ai.chat;

import com.plavonra.ai.util.TemplateInterpolator;
import com.plavonra.spring.ai.langfuse.resolver.LangfusePromptTemplateResolver;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonNoteAnalysisPromptService {

  private static final String SYSTEM_PROMPT_NAME = "person-note-analysis-system";
  private static final String USER_PROMPT_NAME = "person-note-analysis-user";

  private final LangfusePromptTemplateResolver promptTemplateResolver;

  public String getSystemPrompt() {
    return promptTemplateResolver.resolveTemplate(SYSTEM_PROMPT_NAME);
  }

  public String buildUserPrompt(String note) {
    String textPrompt = promptTemplateResolver.resolveTemplate(USER_PROMPT_NAME);
    log.debug(
        "User prompt template from Langfuse, length {} (before {{...}} substitution)",
        textPrompt.length());

    String userPrompt =
        TemplateInterpolator.interpolateDoubleCurly(textPrompt, Map.of("note", note));

    log.debug("User message length {} after substitution", userPrompt.length());
    return userPrompt;
  }
}
