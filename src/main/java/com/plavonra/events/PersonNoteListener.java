package com.plavonra.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PersonNoteListener {
  private final PersonNoteHandler handler;

  @Async
  @EventListener
  public void on(PersonNoteUpdatedEvent event) {
    handler.handle(event);
  }
}
