package com.plavonra.people.repository;

import static com.plavonra.people.util.PersonConstants.*;

import com.plavonra.people.entity.PersonEntity;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, UUID> {

  @Override
  @Query("select p from PersonEntity p where p.action <> 'DELETED'")
  Page<PersonEntity> findAll(Pageable personPageable);

  @Query("select p from PersonEntity p where p.email=:email and p.action <> 'DELETED'")
  Optional<PersonEntity> findByEmail(@Param(EMAIL) @NonNull final String emailId);

  @Query("select p from PersonEntity p where p.id=:id and p.action <> 'DELETED'")
  Optional<PersonEntity> findByPersonId(@Param(ID) @NonNull final UUID personId);

  @Query(
      "select p from PersonEntity p left join fetch p.noteAnalysis where p.id=:id and p.action <> 'DELETED'")
  Optional<PersonEntity> findByPersonIdWithNoteAnalysis(@Param(ID) @NonNull final UUID personId);

  @Query(
      "update PersonEntity p set p.lastModifiedBy=:lastModifiedBy, p.lastModifiedDate=:lastModifiedDate, p.action='DELETED' where p.id=:id")
  @Modifying
  void softDeleteByPersonId(
      @Param(ID) @NonNull final UUID id,
      @Param(LAST_MODIFIED_DATE) @NonNull final Date updatedDate,
      @Param(LAST_MODIFIED_BY) @NonNull final String updatedBy);
}
