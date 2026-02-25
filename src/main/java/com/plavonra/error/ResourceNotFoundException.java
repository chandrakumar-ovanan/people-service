package com.plavonra.error;

import com.plavonra.error.model.ApiException;

public class ResourceNotFoundException extends ApiException {
  public ResourceNotFoundException(String title, String message) {
    super(title, message);
  }
}
