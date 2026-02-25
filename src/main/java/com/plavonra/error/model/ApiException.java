package com.plavonra.error.model;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

  private String title;

  protected ApiException(String title, String message) {
    super(message);
    this.title = title;
  }

  protected ApiException(String message) {
    super(message);
    this.title = "Error";
  }
}
