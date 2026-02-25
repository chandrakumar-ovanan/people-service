package com.plavonra.people.validation;

import static com.plavonra.people.util.PersonErrorMessages.ERROR_AGE_MUST_BE_AT_LEAST_18;
import static com.plavonra.people.util.PersonErrorMessages.ERROR_EMAIL_INVALID_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

import com.plavonra.services.people.api.model.PersonRequest;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PersonValidatorTest {

  @Nested
  class ValidatePerson {

    @Test
    void validatePerson_returnEmpty() {
      // Given
      PersonRequest request = validPersonRequest();

      // When
      Optional<String> result = PersonValidator.validatePerson(request);

      // Then
      assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "test@", "@test.com", "test.com"})
    void validatePerson_throwErrorWhenEmailInvalid(String email) {
      // Given
      PersonRequest request = validPersonRequest();
      request.setEmail(email);

      // When
      Optional<String> result = PersonValidator.validatePerson(request);

      // Then
      assertThat(result).contains(ERROR_EMAIL_INVALID_FORMAT);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 17})
    void validatePerson_throwErrorWhenAgeBelowMinimum(int age) {
      // Given
      PersonRequest request = validPersonRequest();
      request.setAge(age);

      // When
      Optional<String> result = PersonValidator.validatePerson(request);

      // Then
      assertThat(result).contains(ERROR_AGE_MUST_BE_AT_LEAST_18);
    }

    @Test
    void validatePerson_returnEmptyWhenAgeIsExactly18() {
      // Given
      PersonRequest request = validPersonRequest();
      request.setAge(18);

      // When
      Optional<String> result = PersonValidator.validatePerson(request);

      // Then
      assertThat(result).isEmpty();
    }
  }

  private PersonRequest validPersonRequest() {
    PersonRequest request = new PersonRequest();
    request.setEmail("test@test.com");
    request.setAge(18);
    return request;
  }
}
