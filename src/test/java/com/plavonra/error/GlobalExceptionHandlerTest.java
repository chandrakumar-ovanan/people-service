package com.plavonra.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @InjectMocks private GlobalExceptionHandler handler;

  @Nested
  class HandleApiException {

    @Test
    void resourceNotFoundException_returnsNotFoundProblemDetail() {
      ResourceNotFoundException ex =
          new ResourceNotFoundException("Person not found", "Person with id x was not found");

      ProblemDetail result = handler.handleApiException(ex);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
      assertThat(result.getTitle()).isEqualTo("Person not found");
      assertThat(result.getDetail()).isEqualTo("Person with id x was not found");
    }

    @Test
    void fieldValidationException_returnsBadRequestProblemDetail() {
      FieldValidationException ex = new FieldValidationException("Email address format is invalid");

      ProblemDetail result = handler.handleApiException(ex);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
      assertThat(result.getTitle()).isEqualTo("Validation error");
      assertThat(result.getDetail()).isEqualTo("Email address format is invalid");
    }

    @Test
    void conflictException_returnsConflictProblemDetail() {
      ConflictException ex = new ConflictException("Email address already exists");

      ProblemDetail result = handler.handleApiException(ex);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
      assertThat(result.getTitle()).isEqualTo("Conflict");
      assertThat(result.getDetail()).isEqualTo("Email address already exists");
    }
  }

  @Nested
  class HandleBadRequest {

    @Test
    void illegalArgumentException_returnsBadRequestProblemDetail() {
      IllegalArgumentException ex = new IllegalArgumentException("Invalid UUID format");

      ProblemDetail result = handler.handleBadRequest(ex);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
      assertThat(result.getTitle()).isEqualTo("Invalid request");
      assertThat(result.getDetail()).isEqualTo("Invalid UUID format");
    }
  }

  @Nested
  class HandleGeneric {

    @Test
    void exception_returnsInternalServerErrorProblemDetail() {
      Exception ex = new RuntimeException("Unexpected");

      ProblemDetail result = handler.handleGeneric(ex);

      assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
      assertThat(result.getTitle()).isEqualTo("Internal server error");
      assertThat(result.getDetail()).isEqualTo("Unexpected error occurred");
    }
  }
}
