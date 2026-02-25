package com.plavonra.events;

import com.plavonra.ai.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonNoteHandler {
  private final PersonNoteAiFacade aiFacade;

  public void handle(PersonNoteUpdatedEvent event) {

    UUID personId = event.personId();
    String note = event.note();

    MDC.put("personId", personId.toString());
    try {
      log.debug(
          "Handling PersonNoteUpdatedEvent personId={}, noteLength={}",
          personId,
          note != null ? note.length() : 0);

      aiFacade.process(personId, note);

    } finally {
      MDC.clear();
    }
  }
}
