package com.plavonra.assertions;

import org.assertj.core.api.AbstractAssert;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ResponseAssert extends AbstractAssert<ResponseAssert, ResponseEntity<?>> {

  protected ResponseAssert(ResponseEntity<?> actual) {
    super(actual, ResponseAssert.class);
  }

  public static ResponseAssert assertThat(ResponseEntity<?> actual) {
    return new ResponseAssert(actual);
  }

  public ResponseAssert isSuccessfulResponse() {
    HttpStatusCode actualStatus = actual.getStatusCode();
    if (!actualStatus.is2xxSuccessful()) {
      failWithMessage("Expected successful HTTP status (2xx) but was %s", actualStatus);
    }
    return this;
  }

  public ResponseAssert hasStatusCode(HttpStatus expectedStatus) {
    HttpStatusCode actualStatus = actual.getStatusCode();
    if (!expectedStatus.equals(actualStatus)) {
      failWithMessage("Expected HTTP status '%s' but was '%s'", expectedStatus, actualStatus);
    }
    return this;
  }

  public ResponseAssert hasProblemJsonContentType() {
    MediaType contentType = actual.getHeaders().getContentType();

    if (contentType == null || !MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(contentType)) {

      failWithMessage(
          "Expected Content-Type '%s' but was '%s'",
          MediaType.APPLICATION_PROBLEM_JSON, contentType);
    }
    return this;
  }
}
