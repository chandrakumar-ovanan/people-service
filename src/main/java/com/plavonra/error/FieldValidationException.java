package com.plavonra.error;

import com.plavonra.error.model.ApiException;

public class FieldValidationException extends ApiException {
  public FieldValidationException(String message) {
    super("Validation error", message);
  }
}
