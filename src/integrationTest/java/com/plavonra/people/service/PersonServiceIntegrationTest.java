package com.plavonra.people.service;

import static com.plavonra.people.util.PersonErrorMessages.ERROR_EMAIL_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plavonra.BaseIntegrationTest;
import com.plavonra.error.ConflictException;
import com.plavonra.error.FieldValidationException;
import com.plavonra.error.ResourceNotFoundException;
import com.plavonra.people.repository.PersonRepository;
import com.plavonra.services.people.api.model.PeoplePage;
import com.plavonra.services.people.api.model.Person;
import com.plavonra.services.people.api.model.PersonName;
import com.plavonra.services.people.api.model.PersonRequest;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class PersonServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired private PersonService personService;
  @Autowired private PersonRepository personRepository;

  @Nested
  class CreatePerson {

    @Test
    void createPerson_persistsAndReturnsDto() {
      PersonRequest request = validRequest();

      Person created = personService.createPerson(request);

      assertThat(created.getId()).isNotNull();
      assertThat(created.getEmail()).isEqualTo(request.getEmail());
      assertThat(personRepository.findByEmail(request.getEmail())).isPresent();
    }

    @Test
    void createPerson_duplicateEmail_throwsConflict() {
      PersonRequest request = validRequest();
      personService.createPerson(request);

      assertThatThrownBy(() -> personService.createPerson(request))
          .isInstanceOf(ConflictException.class)
          .hasMessage(ERROR_EMAIL_ALREADY_EXISTS);
    }

    @Test
    void createPerson_invalidAge_throwsValidation() {
      PersonRequest request = validRequest();
      request.setAge(17);

      assertThatThrownBy(() -> personService.createPerson(request))
          .isInstanceOf(FieldValidationException.class);
    }
  }

  @Nested
  class GetPersonById {

    @Test
    void getPersonById_returnsPersistedPerson() {
      Person created = personService.createPerson(validRequest());

      Person found = personService.getPersonById(created.getId());

      assertThat(found.getId()).isEqualTo(created.getId());
      assertThat(found.getEmail()).isEqualTo(created.getEmail());
    }

    @Test
    void getPersonById_unknownId_throwsNotFound() {
      UUID id = UUID.randomUUID();

      assertThatThrownBy(() -> personService.getPersonById(id))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  @Nested
  class ListPeople {

    @Test
    void listPeople_returnsCreatedRows() {
      personService.createPerson(validRequest());
      personService.createPerson(validRequest());

      PeoplePage page = personService.listPeople(0, 20);

      assertThat(page.getItems()).hasSizeGreaterThanOrEqualTo(2);
      assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void listPeople_invalidPage_throwsValidation() {
      assertThatThrownBy(() -> personService.listPeople(-1, 10))
          .isInstanceOf(FieldValidationException.class);
    }
  }

  @Nested
  class UpdatePerson {

    @Test
    void updatePerson_appliesChanges() {
      Person created = personService.createPerson(validRequest());
      PersonRequest update = validRequest();
      update.setFavoriteColor("blue");
      update.getName().setFirstName("Jane");

      Person updated = personService.updatePerson(created.getId(), update);

      assertThat(updated.getFavoriteColor()).isEqualTo("blue");
      assertThat(updated.getName().getFirstName()).isEqualTo("Jane");
    }
  }

  @Nested
  class DeletePerson {

    @Test
    void deletePerson_excludesFromQueries() {
      Person created = personService.createPerson(validRequest());

      personService.deletePerson(created.getId());

      assertThatThrownBy(() -> personService.getPersonById(created.getId()))
          .isInstanceOf(ResourceNotFoundException.class);
      assertThat(personRepository.findByPersonId(created.getId())).isEmpty();
    }
  }

  private static PersonRequest validRequest() {
    PersonName name = new PersonName();
    name.setFirstName("Integration");
    name.setLastName("User");

    PersonRequest p = new PersonRequest();
    p.setAge(21);
    p.setFavoriteColor("green");
    p.setName(name);
    p.setEmail("svc-" + UUID.randomUUID() + "@example.com");
    return p;
  }
}
