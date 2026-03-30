package com.plavonra.people;

import static com.plavonra.people.util.PersonErrorMessages.ERROR_EMAIL_ALREADY_EXISTS;
import static com.plavonra.people.util.PersonErrorMessages.ERROR_PERSON_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import com.plavonra.BaseIntegrationTest;
import com.plavonra.assertions.ProblemAssert;
import com.plavonra.assertions.ResponseAssert;
import com.plavonra.services.people.api.model.PeoplePage;
import com.plavonra.services.people.api.model.Person;
import com.plavonra.services.people.api.model.PersonName;
import com.plavonra.services.people.api.model.PersonRequest;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class PeopleRestControllerIntegrationTest extends BaseIntegrationTest {

  @Nested
  class CreatePerson {

    @Test
    void validRequest_returnsCreatedPerson() {
      PersonRequest request = validPersonRequest();

      ResponseEntity<Person> result =
          restTemplate.postForEntity(peopleUrl(), request, Person.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getEmail()).isEqualTo(request.getEmail());
      assertThat(result.getBody().getId()).isNotNull();
    }

    @Test
    void duplicateEmail_returnsConflict() {
      PersonRequest request = validPersonRequest();
      restTemplate.postForEntity(peopleUrl(), request, Person.class);

      ResponseEntity<ProblemDetail> result =
          restTemplate.postForEntity(peopleUrl(), request, ProblemDetail.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      ProblemAssert.assertThat(result.getBody())
          .problemAssert(HttpStatus.CONFLICT, "Conflict", ERROR_EMAIL_ALREADY_EXISTS);
    }
  }

  @Nested
  class GetPersonById {

    @Test
    void existingId_returnsPerson() {
      Person created = createPersonViaApi();

      ResponseEntity<Person> result =
          restTemplate.getForEntity(personUrl(created.getId()), Person.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getId()).isEqualTo(created.getId());
    }

    @Test
    void missingId_returnsNotFound() {
      UUID missingId = UUID.randomUUID();

      ResponseEntity<ProblemDetail> response =
          restTemplate.getForEntity(personUrl(missingId), ProblemDetail.class);

      assertNotFound(response, "Person with id " + missingId + " was not found");
    }
  }

  @Nested
  class UpdatePerson {

    @Test
    void validRequest_updatesPerson() {
      Person created = createPersonViaApi();
      PersonRequest update = validPersonRequest();
      update.getName().setFirstName("Bob");
      update.getName().setLastName("Updated");

      ResponseEntity<Person> result =
          restTemplate.exchange(
              personUrl(created.getId()), HttpMethod.PUT, new HttpEntity<>(update), Person.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getName().getFirstName()).isEqualTo("Bob");
    }

    @Test
    void missingId_returnsNotFound() {
      UUID missingId = UUID.randomUUID();

      ResponseEntity<ProblemDetail> response =
          restTemplate.exchange(
              personUrl(missingId),
              HttpMethod.PUT,
              new HttpEntity<>(validPersonRequest()),
              ProblemDetail.class);

      assertNotFound(response, "Person with id " + missingId + " was not found");
    }
  }

  @Nested
  class DeletePerson {

    @Test
    void existingId_returnsNoContent() {
      Person created = createPersonViaApi();

      ResponseEntity<Void> response =
          restTemplate.exchange(personUrl(created.getId()), HttpMethod.DELETE, null, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void missingId_returnsNotFound() {
      UUID missingId = UUID.randomUUID();

      ResponseEntity<ProblemDetail> response =
          restTemplate.exchange(personUrl(missingId), HttpMethod.DELETE, null, ProblemDetail.class);

      assertNotFound(response, "Person with id " + missingId + " was not found");
    }
  }

  @Nested
  class ListPeople {

    @Test
    void validRequest_returnsOkWithBody() {
      createPersonViaApi();

      ResponseEntity<PeoplePage> response =
          restTemplate.getForEntity(peopleUrl() + "?page=0&size=10", PeoplePage.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getItems()).isNotEmpty();
    }
  }

  private Person createPersonViaApi() {
    ResponseEntity<Person> created =
        restTemplate.postForEntity(peopleUrl(), validPersonRequest(), Person.class);
    assertThat(created.getBody()).isNotNull();
    return created.getBody();
  }

  private static PersonRequest validPersonRequest() {
    PersonName name = new PersonName();
    name.setFirstName("John");
    name.setLastName("Doe");

    PersonRequest p = new PersonRequest();
    p.setAge(18);
    p.setFavoriteColor("red");
    p.setName(name);
    p.setEmail("it-" + UUID.randomUUID() + "@example.com");

    return p;
  }

  private void assertNotFound(ResponseEntity<ProblemDetail> response, String detail) {
    ResponseAssert.assertThat(response)
        .hasStatusCode(HttpStatus.NOT_FOUND)
        .hasProblemJsonContentType();

    ProblemAssert.assertThat(response.getBody())
        .problemAssert(HttpStatus.NOT_FOUND, ERROR_PERSON_NOT_FOUND, detail);
  }
}
