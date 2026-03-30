package com.plavonra.error;

import com.plavonra.error.model.ApiException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  ProblemDetail handleApiException(ApiException ex) {
    HttpStatus status = getHttpStatus(ex);

    ProblemDetail pd = ProblemDetail.forStatus(status);
    String title = ex.getTitle();
    pd.setTitle(title != null ? title : "Error");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail handleBadRequest(IllegalArgumentException ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Invalid request");
    pd.setDetail(ex.getMessage() != null ? ex.getMessage() : "Invalid request");
    return pd;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String detail =
        ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining("; "));
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Validation error");
    pd.setDetail(detail.isEmpty() ? "Validation failed" : detail);
    return pd;
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleGeneric(Exception ex) {
    log.error("Unhandled request failure", ex);
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal server error");
    pd.setDetail("Unexpected error occurred");
    return pd;
  }

  private static HttpStatus getHttpStatus(ApiException ex) {
    return switch (ex) {
      case ResourceNotFoundException e -> HttpStatus.NOT_FOUND;
      case FieldValidationException e -> HttpStatus.BAD_REQUEST;
      case ConflictException e -> HttpStatus.CONFLICT;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
