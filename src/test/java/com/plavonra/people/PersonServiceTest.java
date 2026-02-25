package com.plavonra.people;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plavonra.error.ConflictException;
import com.plavonra.error.FieldValidationException;
import com.plavonra.error.ResourceNotFoundException;
import com.plavonra.events.PersonNoteUpdatedEvent;
import com.plavonra.people.entity.PersonEntity;
import com.plavonra.people.entity.PersonNoteAnalysisEntity;
import com.plavonra.people.mapper.PersonMapper;
import com.plavonra.people.repository.PersonNoteAnalysisRepository;
import com.plavonra.people.repository.PersonRepository;
import com.plavonra.people.service.PersonNoteEmbeddingService;
import com.plavonra.people.service.PersonService;
import com.plavonra.services.people.api.model.PeoplePage;
import com.plavonra.services.people.api.model.Person;
import com.plavonra.services.people.api.model.PersonNoteRequest;
import com.plavonra.services.people.api.model.PersonRequest;
import java.util.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private PersonRepository personRepository;
  @Mock private PersonNoteAnalysisRepository personNoteAnalysisRepository;
  @Mock private PersonNoteEmbeddingService personNoteEmbeddingService;
  @Mock private AuditorAware<String> auditorAware;
  @Mock private PersonMapper personMapper;

  @InjectMocks private PersonService personService;
  private final UUID id = UUID.randomUUID();

  @Nested
  class GetAllPersons {

    @Test
    void getAllPersons_returnPersonListResponse() {
      PersonEntity entity = personEntity(id);
      Person person = personDto(id);
      PeoplePage response = new PeoplePage();

      when(personRepository.findAll(any(PageRequest.class)))
          .thenReturn(new PageImpl<>(List.of(entity)));
      when(personNoteAnalysisRepository.findByPerson_IdIn(List.of(id))).thenReturn(List.of());
      when(personMapper.toPerson(entity, null)).thenReturn(person);
      when(personMapper.getPeoplePage(anyInt(), anyInt(), any(), anyInt())).thenReturn(response);

      PeoplePage result = personService.listPeople(0, 10);

      assertThat(result).isSameAs(response);
    }

    @Test
    void listPeople_whenNoRecords_returnsEmptyPage() {
      // given
      int page = 0;
      int size = 10;
      int totalElements = 0;

      PeoplePage expectedPeoplePage = new PeoplePage();
      expectedPeoplePage.setPage(page);
      expectedPeoplePage.setSize(size);
      expectedPeoplePage.setItems(Collections.emptyList());
      expectedPeoplePage.setTotalElements(totalElements);

      PageRequest pageRequest = PageRequest.of(page, size);

      when(personRepository.findAll(pageRequest)).thenReturn(Page.empty());

      when(personMapper.getPeoplePage(anyInt(), anyInt(), any(), anyInt()))
          .thenReturn(expectedPeoplePage);

      // when
      PeoplePage result = personService.listPeople(page, size);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getItems()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(totalElements);
      assertThat(result.getPage()).isEqualTo(page);
      assertThat(result.getSize()).isEqualTo(size);
    }

    @Test
    void getAllPersons_throwFieldValidationException() {
      assertThatThrownBy(() -> personService.listPeople(-1, 10))
          .isInstanceOf(FieldValidationException.class);
    }
  }

  @Nested
  class GetPersonById {

    @Test
    void getPersonById_returnPerson() {
      PersonEntity entity = personEntity(id);
      Person person = personDto(id);

      when(personRepository.findByPersonIdWithNoteAnalysis(id)).thenReturn(Optional.of(entity));
      when(personMapper.toPerson(entity)).thenReturn(person);

      Person result = personService.getPersonById(id);

      assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getPersonById_whenNoteAnalysisExists_returnsPersonWithTopicAndSentiment() {
      PersonEntity entity = personEntity(id);
      PersonNoteAnalysisEntity analysis = new PersonNoteAnalysisEntity();
      analysis.setTopic("refund");
      analysis.setSentiment("positive");
      analysis.setPerson(entity);
      entity.setNoteAnalysis(analysis);
      Person person = personDto(id);
      person.setNoteTopic("refund");
      person.setNoteSentiment("positive");

      when(personRepository.findByPersonIdWithNoteAnalysis(id)).thenReturn(Optional.of(entity));
      when(personMapper.toPerson(entity, analysis)).thenReturn(person);

      Person result = personService.getPersonById(id);

      assertThat(result.getId()).isEqualTo(id);
      assertThat(result.getNoteTopic()).isEqualTo("refund");
      assertThat(result.getNoteSentiment()).isEqualTo("positive");
    }

    @Test
    void getPersonById_throwResourceNotFoundException() {
      when(personRepository.findByPersonIdWithNoteAnalysis(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> personService.getPersonById(id))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  @Nested
  class CreatePerson {

    @Test
    void createPerson_returnPerson() {
      PersonRequest request = validPersonRequest();
      PersonEntity entity = new PersonEntity();
      PersonEntity saved = personEntity(id);
      Person person = personDto(id);

      when(personRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
      when(personMapper.toPersonEntity(request)).thenReturn(entity);
      when(personRepository.save(entity)).thenReturn(saved);
      when(personMapper.toPerson(saved)).thenReturn(person);

      Person result = personService.createPerson(request);

      assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void createPerson_throwResourceAlreadyFoundException() {
      PersonRequest request = validPersonRequest();

      when(personRepository.findByEmail(request.getEmail()))
          .thenReturn(Optional.of(new PersonEntity()));

      assertThatThrownBy(() -> personService.createPerson(request))
          .isInstanceOf(ConflictException.class);
    }
  }

  @Nested
  class UpdatePerson {

    @Test
    void updatePerson_returnUpdatedPerson() {
      // given
      UUID id = UUID.randomUUID();
      PersonRequest request = validPersonRequest();

      PersonEntity existingEntity = personEntity(id);
      PersonEntity savedEntity = personEntity(id);
      Person personDto = personDto(id);

      stubPersonFound(id, existingEntity);

      when(personRepository.save(existingEntity)).thenReturn(savedEntity);
      when(personMapper.toPerson(savedEntity)).thenReturn(personDto);

      // when
      Person result = personService.updatePerson(id, request);

      // then
      assertThat(result.getId()).isEqualTo(id);

      verify(personMapper).toPersonEntity(request, existingEntity);
      verify(personRepository).save(existingEntity);
      verify(personMapper).toPerson(savedEntity);
    }

    @Test
    void updatePerson_throwResourceNotFoundException() {
      PersonRequest request = validPersonRequest();

      when(personRepository.findByPersonId(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> personService.updatePerson(id, request))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  @Nested
  class UpdatePersonNote {

    @Test
    void updatePersonNote_returnUpdatedPerson() {
      PersonNoteRequest request = new PersonNoteRequest();
      request.setNote("Some note");
      PersonEntity existingEntity = personEntity(id);
      Person personDto = personDto(id);

      stubPersonFound(id, existingEntity);
      when(personRepository.save(existingEntity)).thenReturn(existingEntity);
      when(personMapper.toPerson(existingEntity)).thenReturn(personDto);

      Person result = personService.updatePersonNote(id, request);

      assertThat(result.getId()).isEqualTo(id);
      verify(personRepository).save(existingEntity);
      verify(personMapper).toPerson(existingEntity);
      verify(eventPublisher).publishEvent(any(PersonNoteUpdatedEvent.class));
    }

    @Test
    void updatePersonNote_withEmptyNote_doesNotPublishEvent() {
      PersonNoteRequest request = new PersonNoteRequest();
      request.setNote("");
      PersonEntity existingEntity = personEntity(id);
      PersonEntity savedEntity = personEntity(id);
      stubPersonFound(id, existingEntity);
      when(personRepository.save(existingEntity)).thenReturn(savedEntity);
      when(personMapper.toPerson(savedEntity)).thenReturn(personDto(id));

      personService.updatePersonNote(id, request);

      verify(personRepository).save(existingEntity);
      verify(eventPublisher, never()).publishEvent(any(PersonNoteUpdatedEvent.class));
    }

    @Test
    void updatePersonNote_throwResourceNotFoundException() {
      PersonNoteRequest request = new PersonNoteRequest();
      request.setNote("Note");
      when(personRepository.findByPersonId(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> personService.updatePersonNote(id, request))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  @Nested
  class DeletePerson {

    @Test
    void deletePerson_success() {
      PersonEntity entity = personEntity(id);

      stubPersonFound(id, entity);
      when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("tester"));

      personService.deletePerson(id);

      verify(personNoteAnalysisRepository).deleteByPerson_Id(id);
      verify(personNoteEmbeddingService).deleteEmbedding(id);
      verify(personRepository).softDeleteByPersonId(eq(id), any(Date.class), eq("tester"));
    }

    @Test
    void deletePerson_auditorMissingUseFallback() {
      PersonEntity entity = personEntity(id);

      stubPersonFound(id, entity);
      when(auditorAware.getCurrentAuditor()).thenReturn(Optional.empty());

      personService.deletePerson(id);

      verify(personNoteAnalysisRepository).deleteByPerson_Id(id);
      verify(personNoteEmbeddingService).deleteEmbedding(id);
      verify(personRepository).softDeleteByPersonId(eq(id), any(Date.class), eq("unknown"));
    }

    @Test
    void deletePerson_throwResourceNotFoundException() {
      when(personRepository.findByPersonId(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> personService.deletePerson(id))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // -------- helpers --------

  private PersonRequest validPersonRequest() {
    PersonRequest request = new PersonRequest();
    request.setEmail("test@test.com");
    request.setAge(18);
    return request;
  }

  private PersonEntity personEntity(UUID id) {
    PersonEntity entity = new PersonEntity();
    entity.setId(id);
    return entity;
  }

  private Person personDto(UUID id) {
    Person person = new Person();
    person.setId(id);
    return person;
  }

  private void stubPersonFound(UUID id, PersonEntity entity) {
    when(personRepository.findByPersonId(id)).thenReturn(Optional.of(entity));
  }
}
