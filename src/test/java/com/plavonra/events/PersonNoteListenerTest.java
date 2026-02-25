package com.plavonra.events;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonNoteListenerTest {

  @Mock private PersonNoteHandler handler;

  @InjectMocks private PersonNoteListener listener;

  @Test
  void on_delegatesToHandler() {
    PersonNoteUpdatedEvent event = new PersonNoteUpdatedEvent(UUID.randomUUID(), "note text");

    listener.on(event);

    verify(handler).handle(event);
  }
}
