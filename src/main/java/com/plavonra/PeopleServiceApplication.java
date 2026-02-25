package com.plavonra;

import com.plavonra.ai.embedding.SearchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SearchProperties.class)
public class PeopleServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PeopleServiceApplication.class, args);
  }
}
