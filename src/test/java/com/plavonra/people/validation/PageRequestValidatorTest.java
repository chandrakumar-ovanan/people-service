package com.plavonra.people.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PageRequestValidatorTest {

  @Nested
  class ValidateRequest {

    @Test
    void nullPageAndNullSize_returnEmpty() {
      // Given
      Integer page = null;
      Integer size = null;

      // When
      Optional<String> result = PageRequestValidator.validateRequest(page, size);

      // Then
      assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("validPageSizeArguments")
    void validPageAndSize_returnEmpty(Integer page, Integer size) {
      // When
      Optional<String> result = PageRequestValidator.validateRequest(page, size);

      // Then
      assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void invalidSizeValidateRequest_returnError(int size) {
      // Given
      Integer page = 0;

      // When
      Optional<String> result = PageRequestValidator.validateRequest(page, size);

      // Then
      assertThat(result).contains(PageRequestValidator.THE_SIZE_MUST_NOT_BE_ZERO_OR_NEGATIVE);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, -100})
    void invalidPageValidateRequest_returnError(int page) {
      // Given
      Integer size = 10;

      // When
      Optional<String> result = PageRequestValidator.validateRequest(page, size);

      // Then
      assertThat(result).contains(PageRequestValidator.THE_PAGE_MUST_NOT_BE_NEGATIVE);
    }

    @ParameterizedTest
    @MethodSource("invalidPageAndSizeArguments")
    void invalidPageAndInvalidSize_returnSizeErrorFirst(Integer page, Integer size) {
      // When
      Optional<String> result = PageRequestValidator.validateRequest(page, size);

      // Then
      assertThat(result).contains(PageRequestValidator.THE_SIZE_MUST_NOT_BE_ZERO_OR_NEGATIVE);
    }

    static Stream<Arguments> validPageSizeArguments() {
      return Stream.of(
          Arguments.of(0, 1), Arguments.of(1, 10), Arguments.of(null, 10), Arguments.of(0, null));
    }

    static Stream<Arguments> invalidPageAndSizeArguments() {
      return Stream.of(Arguments.of(-1, 0), Arguments.of(-10, -1));
    }
  }
}
