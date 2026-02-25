package com.plavonra;

import com.plavonra.people.service.PersonService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = PeopleServiceApplication.class)
@ActiveProfiles("integration")
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {
  private static final String CONTEXT_PATH = "/people-service";
  private static final String API_V1 = "/api/v1";
  private static final String PEOPLE_PATH = "/people";

  @LocalServerPort protected int port;

  @Autowired protected TestRestTemplate restTemplate;

  @MockitoSpyBean protected PersonService personService;

  protected String baseUrl() {
    return "http://localhost:" + port + CONTEXT_PATH;
  }

  protected String peopleUrl() {
    return UriComponentsBuilder.fromUriString(baseUrl())
        .path(API_V1)
        .path(PEOPLE_PATH)
        .toUriString();
  }

  protected String personUrl(UUID id) {
    return UriComponentsBuilder.fromUriString(baseUrl())
        .path(API_V1)
        .path(PEOPLE_PATH)
        .pathSegment(id.toString())
        .toUriString();
  }
}
