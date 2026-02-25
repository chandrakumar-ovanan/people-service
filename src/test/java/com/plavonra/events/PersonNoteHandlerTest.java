package com.plavonra.events;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.plavonra.ai.PersonNoteAiFacade;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonNoteHandlerTest {

  @Mock private PersonNoteAiFacade aiFacade;

  @InjectMocks private PersonNoteHandler handler;

  @Test
  void handle_delegatesToAiFacade() {
    UUID personId = UUID.randomUUID();
    String note = "some note";
    PersonNoteUpdatedEvent event = new PersonNoteUpdatedEvent(personId, note);

    handler.handle(event);

    verify(aiFacade).process(eq(personId), eq(note));
  }
}
