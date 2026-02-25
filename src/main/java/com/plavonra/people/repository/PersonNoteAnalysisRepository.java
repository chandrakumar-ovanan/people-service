package com.plavonra.people.repository;

import com.plavonra.people.entity.PersonNoteAnalysisEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonNoteAnalysisRepository
    extends JpaRepository<PersonNoteAnalysisEntity, UUID> {

  Optional<PersonNoteAnalysisEntity> findByPerson_Id(UUID personId);

  @Query(
      "select a from PersonNoteAnalysisEntity a join fetch a.person where a.person.id in :personIds")
  List<PersonNoteAnalysisEntity> findByPerson_IdIn(@Param("personIds") List<UUID> personIds);

  @Modifying
  @Query("delete from PersonNoteAnalysisEntity a where a.person.id = :personId")
  void deleteByPerson_Id(@Param("personId") UUID personId);
}
