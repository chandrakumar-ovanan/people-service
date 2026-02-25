package com.plavonra.people.mapper;

import com.plavonra.people.entity.PersonEntity;
import com.plavonra.people.entity.PersonNoteAnalysisEntity;
import com.plavonra.services.people.api.model.*;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PersonMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "action", ignore = true)
  @Mapping(target = "noteAnalysis", ignore = true)
  PersonEntity toPersonEntity(PersonRequest dto);

  @Mapping(target = "noteTopic", ignore = true)
  @Mapping(target = "noteSentiment", ignore = true)
  Person toPerson(PersonEntity entity);

  default Person toPerson(PersonEntity entity, PersonNoteAnalysisEntity analysis) {
    Person person = toPerson(entity);
    if (analysis != null) {
      person.setNoteTopic(analysis.getTopic());
      person.setNoteSentiment(analysis.getSentiment());
    }
    return person;
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "action", ignore = true)
  @Mapping(target = "noteAnalysis", ignore = true)
  void toPersonEntity(PersonRequest request, @MappingTarget PersonEntity existingEntity);

  default PeoplePage getPeoplePage(int page, int size, List<Person> persons, int totalElements) {
    PeoplePage peoplePage = new PeoplePage();
    peoplePage.setPage(page);
    peoplePage.setSize(size);
    peoplePage.setItems(persons);
    peoplePage.setTotalElements(totalElements);
    return peoplePage;
  }
}
