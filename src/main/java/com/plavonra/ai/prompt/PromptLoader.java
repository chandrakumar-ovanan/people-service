package com.plavonra.ai.prompt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromptLoader {

  private final ResourceLoader resourceLoader;

  public String load(String path) {
    try (InputStream is = resourceLoader.getResource("classpath:" + path).getInputStream()) {

      return new String(is.readAllBytes(), StandardCharsets.UTF_8);

    } catch (Exception e) {
      throw new IllegalStateException("Failed to load prompt: " + path, e);
    }
  }

  public String render(String path, Map<String, String> variables) {
    String template = load(path);

    for (var entry : variables.entrySet()) {
      template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }

    return template;
  }
}
