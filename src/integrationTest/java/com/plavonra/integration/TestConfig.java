package com.plavonra.integration;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Bean;

public class TestConfig {
  @Bean
  public TestRestTemplate testRestTemplate(RestTemplateBuilder builder) {
    return new TestRestTemplate(builder);
  }
}
