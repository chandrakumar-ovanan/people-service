package com.plavonra.events;

import java.util.UUID;

public record PersonNoteUpdatedEvent(UUID personId, String note) {}
