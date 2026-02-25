package com.plavonra.error;

import com.plavonra.error.model.ApiException;

public class ConflictException extends ApiException {

  public ConflictException(String message) {
    super("Conflict", message);
  }
}
