package com.plavonra.people;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.plavonra.BaseIntegrationTest;
import com.plavonra.assertions.ProblemAssert;
import com.plavonra.assertions.ResponseAssert;
import com.plavonra.error.ResourceNotFoundException;
import com.plavonra.people.service.PersonService;
import com.plavonra.services.people.api.model.PeoplePage;
import com.plavonra.services.people.api.model.Person;
import com.plavonra.services.people.api.model.PersonName;
import com.plavonra.services.people.api.model.PersonRequest;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PeopleRestControllerIntegrationTest extends BaseIntegrationTest {

  @MockitoBean private PersonService personService;

  private final UUID id = UUID.randomUUID();
  private final UUID missingId = UUID.randomUUID();

  @Nested
  class CreatePerson {

    @Test
    void validRequest_returnsCreatedPerson() {
      Person response = person(id, "John", "Doe");
      when(personService.createPerson(any())).thenReturn(response);

      ResponseEntity<Person> result =
          restTemplate.postForEntity(peopleUrl(), validPersonRequest(), Person.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getId()).isEqualTo(id);
    }
  }

  @Nested
  class GetPersonById {

    @Test
    void existingId_returnsPerson() {
      when(personService.getPersonById(id)).thenReturn(person(id, "Alice", "Smith"));

      ResponseEntity<Person> result = restTemplate.getForEntity(personUrl(id), Person.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(result.getBody().getId()).isEqualTo(id);
    }

    @Test
    void missingId_returnsNotFound() {
      when(personService.getPersonById(missingId)).thenThrow(personNotFound(missingId));

      ResponseEntity<ProblemDetail> response =
          restTemplate.getForEntity(personUrl(missingId), ProblemDetail.class);

      assertNotFound(response, "Person with id " + missingId + " was not found");
    }
  }

  @Nested
  class UpdatePerson {

    @Test
    void validRequest_updatesPerson() {
      when(personService.updatePerson(eq(id), any())).thenReturn(person(id, "Bob", "New"));

      ResponseEntity<Person> result =
          restTemplate.exchange(
              personUrl(id), HttpMethod.PUT, new HttpEntity<>(validPersonRequest()), Person.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(result.getBody().getId()).isEqualTo(id);
    }

    @Test
    void missingId_returnsNotFound() {
      when(personService.updatePerson(eq(missingId), any())).thenThrow(personNotFound(missingId));

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
      doNothing().when(personService).deletePerson(id);

      ResponseEntity<Void> response =
          restTemplate.exchange(personUrl(id), HttpMethod.DELETE, null, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void missingId_returnsNotFound() {
      doThrow(personNotFound(missingId)).when(personService).deletePerson(missingId);

      ResponseEntity<ProblemDetail> response =
          restTemplate.exchange(personUrl(missingId), HttpMethod.DELETE, null, ProblemDetail.class);

      assertNotFound(response, "Person with id " + missingId + " was not found");
    }
  }

  @Nested
  class ListPeople {

    @Test
    void validRequest_returnsOk() {
      when(personService.listPeople(0, 10)).thenReturn(new PeoplePage());

      ResponseEntity<Void> response =
          restTemplate.getForEntity(peopleUrl() + "?page=0&size=10", Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
  }

  private static Person person(UUID id, String firstName, String lastName) {
    Person p = new Person();
    p.setId(id);
    p.setNote("testing");

    PersonName name = new PersonName();
    name.setFirstName(firstName);
    name.setLastName(lastName);
    p.setName(name);

    return p;
  }

  private static PersonRequest validPersonRequest() {
    PersonName name = new PersonName();
    name.setFirstName("John");
    name.setLastName("Doe");

    PersonRequest p = new PersonRequest();
    p.setAge(18);
    p.setFavoriteColor("red");
    p.setName(name);
    p.setEmail("test@example.com");
    p.note("testing");

    return p;
  }

  private static ResourceNotFoundException personNotFound(UUID id) {
    return new ResourceNotFoundException(
        "Person not found", "Person with id " + id + " was not found");
  }

  private void assertNotFound(ResponseEntity<ProblemDetail> response, String detail) {
    ResponseAssert.assertThat(response)
        .hasStatusCode(HttpStatus.NOT_FOUND)
        .hasProblemJsonContentType();

    ProblemAssert.assertThat(response.getBody())
        .problemAssert(HttpStatus.NOT_FOUND, "Person not found", detail);
  }
}
