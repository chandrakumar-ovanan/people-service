package com.plavonra.ai.util;

import java.util.Map;

public final class TemplateInterpolator {

  private TemplateInterpolator() {}

  public static String interpolateDoubleCurly(String template, Map<String, String> values) {
    if (template == null || template.isBlank() || values == null || values.isEmpty()) {
      return template;
    }

    String result = template;

    for (var e : values.entrySet()) {
      String key = e.getKey();
      if (key == null || key.isBlank()) {
        continue;
      }

      String val = e.getValue() == null ? "" : e.getValue();
      result = result.replace("{{" + key + "}}", val);
    }

    return result;
  }
}
