package com.plavonra.people.validation;

import static com.plavonra.people.util.PersonErrorMessages.*;
import static com.plavonra.util.CommonUtil.notValidateEmail;

import com.plavonra.services.people.api.model.PersonRequest;
import java.util.Optional;

public final class PersonValidator {
  private PersonValidator() {}

  private static final int DEFAULT_AGE_ELIGIBLE = 18;

  public static Optional<String> validatePerson(final PersonRequest personRequest) {
    if (personRequest == null) {
      return Optional.of(ERROR_EMAIL_INVALID_FORMAT);
    }
    if (notValidateEmail(personRequest.getEmail())) {
      return Optional.of(ERROR_EMAIL_INVALID_FORMAT);
    }
    if (personRequest.getAge() == null || personRequest.getAge() < DEFAULT_AGE_ELIGIBLE) {
      return Optional.of(ERROR_AGE_MUST_BE_AT_LEAST_18);
    }
    return Optional.empty();
  }
}
