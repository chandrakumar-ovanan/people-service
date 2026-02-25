package com.plavonra;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>("pgvector/pgvector:pg16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
    //        .withInitScript("db/init_pgvector.sql")
    ;
  }
}
