package com.plavonra.people.service;

import com.plavonra.ai.model.PersonNoteAnalysisResult;
import com.plavonra.people.entity.PersonEntity;
import com.plavonra.people.entity.PersonNoteAnalysisEntity;
import com.plavonra.people.repository.PersonNoteAnalysisRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonNoteAnalysisService {
  private final PersonNoteAnalysisRepository repository;
  private final EntityManager entityManager;

  @Transactional
  public void upsertAiAnalysis(UUID personId, PersonNoteAnalysisResult aiResult, String model) {

    PersonNoteAnalysisEntity entity =
        repository
            .findByPerson_Id(personId)
            .orElseGet(
                () -> {
                  PersonNoteAnalysisEntity e = new PersonNoteAnalysisEntity();
                  e.setPerson(entityManager.getReference(PersonEntity.class, personId));
                  return e;
                });

    entity.setTopic(aiResult.topic());
    entity.setSentiment(aiResult.sentiment());
    entity.setModelName(model);
    entity.setAnalyzedAt(Instant.now());

    repository.save(entity);
  }
}
