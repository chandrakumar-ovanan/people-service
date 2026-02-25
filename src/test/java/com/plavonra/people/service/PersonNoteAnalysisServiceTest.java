package com.plavonra.people.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plavonra.ai.model.PersonNoteAnalysisResult;
import com.plavonra.people.entity.PersonNoteAnalysisEntity;
import com.plavonra.people.repository.PersonNoteAnalysisRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonNoteAnalysisServiceTest {

  @Mock private PersonNoteAnalysisRepository repository;
  @Mock private EntityManager entityManager;

  @InjectMocks private PersonNoteAnalysisService personNoteAnalysisService;

  @Test
  void upsertAiAnalysis_existingEntity_updatesAndSaves() {
    UUID personId = UUID.randomUUID();
    PersonNoteAnalysisResult result = new PersonNoteAnalysisResult("topic", "positive");
    PersonNoteAnalysisEntity existing = new PersonNoteAnalysisEntity();
    when(repository.findByPerson_Id(personId)).thenReturn(Optional.of(existing));

    personNoteAnalysisService.upsertAiAnalysis(personId, result, "model");

    verify(repository).save(existing);
    assertThat(existing.getTopic()).isEqualTo("topic");
    assertThat(existing.getSentiment()).isEqualTo("positive");
    assertThat(existing.getModelName()).isEqualTo("model");
  }

  @Test
  void upsertAiAnalysis_newEntity_createsAndSaves() {
    UUID personId = UUID.randomUUID();
    PersonNoteAnalysisResult result = new PersonNoteAnalysisResult("topic", "neutral");
    when(repository.findByPerson_Id(personId)).thenReturn(Optional.empty());
    when(entityManager.getReference(any(), eq(personId))).thenReturn(null);

    personNoteAnalysisService.upsertAiAnalysis(personId, result, "model");

    verify(repository).save(any(PersonNoteAnalysisEntity.class));
  }
}
