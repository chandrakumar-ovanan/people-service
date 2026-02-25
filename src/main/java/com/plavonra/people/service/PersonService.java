package com.plavonra.people.service;

import static com.plavonra.common.audit.Action.CREATED;
import static com.plavonra.common.audit.Action.UPDATED;
import static com.plavonra.people.util.PersonErrorMessages.*;
import static com.plavonra.people.validation.PageRequestValidator.validateRequest;
import static com.plavonra.people.validation.PersonValidator.validatePerson;
import static org.springframework.util.StringUtils.hasText;

import com.plavonra.error.ConflictException;
import com.plavonra.error.FieldValidationException;
import com.plavonra.error.ResourceNotFoundException;
import com.plavonra.events.PersonNoteUpdatedEvent;
import com.plavonra.people.entity.PersonEntity;
import com.plavonra.people.entity.PersonNoteAnalysisEntity;
import com.plavonra.people.mapper.PersonMapper;
import com.plavonra.people.repository.PersonNoteAnalysisRepository;
import com.plavonra.people.repository.PersonRepository;
import com.plavonra.services.people.api.model.PeoplePage;
import com.plavonra.services.people.api.model.Person;
import com.plavonra.services.people.api.model.PersonNoteRequest;
import com.plavonra.services.people.api.model.PersonRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides access to 'com.chandrakumar.ms.api.person.service' data
 *
 * @author chandrakumar ovanan
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PersonService {

  private static final String UNKNOWN_AUDITOR = "unknown";

  private final ApplicationEventPublisher eventPublisher;
  private final PersonRepository personRepository;
  private final PersonNoteAnalysisRepository personNoteAnalysisRepository;
  private final PersonNoteEmbeddingService personNoteEmbeddingService;
  private final AuditorAware<String> auditorAware;
  private final PersonMapper personMapper;

  @Transactional(readOnly = true)
  public PeoplePage listPeople(int page, int size) {
    log.debug("Retrieving people page={}, size={}", page, size);

    validateRequest(page, size)
        .ifPresent(
            error -> {
              throw new FieldValidationException(error);
            });

    Page<PersonEntity> pageResult = personRepository.findAll(PageRequest.of(page, size));
    List<PersonEntity> content = pageResult.getContent();

    Map<UUID, PersonNoteAnalysisEntity> analysisByPersonId =
        content.isEmpty()
            ? Map.of()
            : personNoteAnalysisRepository
                .findByPerson_IdIn(content.stream().map(PersonEntity::getId).toList())
                .stream()
                .collect(Collectors.toMap(a -> a.getPerson().getId(), a -> a, (a, b) -> a));

    List<Person> persons =
        content.stream()
            .map(entity -> personMapper.toPerson(entity, analysisByPersonId.get(entity.getId())))
            .toList();

    PeoplePage peoplePage =
        personMapper.getPeoplePage(
            pageResult.getNumber(),
            pageResult.getSize(),
            persons,
            (int) pageResult.getTotalElements());

    if (persons.isEmpty()) {
      log.info("No people found");
    }

    return peoplePage;
  }

  @Transactional(readOnly = true)
  public Person getPersonById(UUID id) {
    log.debug("Retrieving person id={}", id);

    PersonEntity entity =
        personRepository
            .findByPersonIdWithNoteAnalysis(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ERROR_PERSON_NOT_FOUND, "Person with id %s was not found".formatted(id)));
    Person person =
        entity.getNoteAnalysis() != null
            ? personMapper.toPerson(entity, entity.getNoteAnalysis())
            : personMapper.toPerson(entity);

    log.info("Person retrieved id={}", person.getId());
    return person;
  }

  @Transactional
  public Person createPerson(PersonRequest personRequest) {
    log.debug("Creating person");

    validatePerson(personRequest)
        .ifPresent(
            error -> {
              throw new FieldValidationException(error);
            });

    personRepository
        .findByEmail(personRequest.getEmail())
        .ifPresent(
            p -> {
              log.warn("Create rejected: person already exists email={}", personRequest.getEmail());
              throw new ConflictException(ERROR_EMAIL_ALREADY_EXISTS);
            });

    PersonEntity entity = personMapper.toPersonEntity(personRequest);
    entity.setAction(CREATED);

    PersonEntity saved = personRepository.save(entity);
    publishNoteUpdatedEvent(saved);

    log.info("Person created id={}", saved.getId());
    return personMapper.toPerson(saved);
  }

  @Transactional
  public Person updatePerson(UUID personId, PersonRequest personRequest) {
    log.debug("Updating person id={}", personId);

    validatePerson(personRequest)
        .ifPresent(
            error -> {
              throw new FieldValidationException(error);
            });
    PersonEntity existingEntity = findPerson(personId);
    personMapper.toPersonEntity(personRequest, existingEntity);
    existingEntity.setAction(UPDATED);

    PersonEntity updated = personRepository.save(existingEntity);
    publishNoteUpdatedEvent(updated);

    log.info("Person updated id={}", updated.getId());
    return personMapper.toPerson(updated);
  }

  @Transactional
  public void deletePerson(UUID id) {
    log.debug("Deleting person id={}", id);

    PersonEntity entity = findPerson(id);

    personNoteAnalysisRepository.deleteByPerson_Id(id);
    personNoteEmbeddingService.deleteEmbedding(id);

    String auditor = auditorAware.getCurrentAuditor().orElse(UNKNOWN_AUDITOR);

    if (UNKNOWN_AUDITOR.equals(auditor)) {
      log.warn("Auditor not resolved, using fallback");
    }

    personRepository.softDeleteByPersonId(entity.getId(), new Date(), auditor);

    log.info("Person deleted id={} by {}", entity.getId(), auditor);
  }

  private PersonEntity findPerson(UUID id) {
    return personRepository
        .findByPersonId(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    ERROR_PERSON_NOT_FOUND, "Person with id %s was not found".formatted(id)));
  }

  @Transactional
  public Person updatePersonNote(UUID id, PersonNoteRequest request) {
    log.debug("Updating person id={} for notes", id);

    PersonEntity entity = findPerson(id);

    entity.setNote(request != null ? request.getNote() : null);
    entity.setAction(UPDATED);

    PersonEntity updated = personRepository.save(entity);
    publishNoteUpdatedEvent(updated);

    log.info("Person updated id={} for notes", updated.getId());
    return personMapper.toPerson(updated);
  }

  private void publishNoteUpdatedEvent(PersonEntity entity) {
    if (!hasText(entity.getNote())) {
      return;
    }

    eventPublisher.publishEvent(new PersonNoteUpdatedEvent(entity.getId(), entity.getNote()));
  }
}
