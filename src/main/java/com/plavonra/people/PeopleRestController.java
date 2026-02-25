package com.plavonra.people;

import com.plavonra.people.service.ActionSuggestionService;
import com.plavonra.people.service.PersonService;
import com.plavonra.services.people.api.model.*;
import com.plavonra.services.people.api.server.PeopleApi;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PeopleRestController implements PeopleApi {

  private final PersonService personService;
  private final ActionSuggestionService actionSuggestionService;

  @Override
  public Person createPerson(PersonRequest request) {
    return personService.createPerson(request);
  }

  @Override
  public PeoplePage listPeople(Integer page, Integer size) {
    int pageNum = page != null ? page : 0;
    int sizeNum = size != null ? size : 20;
    return personService.listPeople(pageNum, sizeNum);
  }

  @Override
  public Person getPerson(UUID id) {
    return personService.getPersonById(id);
  }

  @Override
  public Person replacePerson(UUID id, PersonRequest request) {
    return personService.updatePerson(id, request);
  }

  @Override
  public Person updatePersonNote(UUID id, PersonNoteRequest personNoteRequest) {
    return personService.updatePersonNote(id, personNoteRequest);
  }

  @Override
  public void deletePerson(UUID id) {
    personService.deletePerson(id);
  }

  @Override
  public ActionSuggestionResponse getPeopleActionSuggestion(String text) {
    return actionSuggestionService.suggest(text);
  }
}
