package com.plavonra.ai.config;

import com.plavonra.ai.embedding.SearchProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder builder) {
    return builder.build();
  }

  @Bean
  SearchProperties searchProperties(
      @Value("${plavonra.ai.embedding.top-k:5}") int topK,
      @Value("${plavonra.ai.embedding.similarity-threshold:0.7}") double similarityThreshold) {
    return new SearchProperties(topK, similarityThreshold);
  }
}
