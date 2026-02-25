package com.plavonra.error;

import com.plavonra.error.model.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
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

  @ExceptionHandler(Exception.class)
  ProblemDetail handleGeneric(Exception ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal server error");
    pd.setDetail("Unexpected error occurred");
    return pd;
  }

  private static HttpStatus getHttpStatus(ApiException ex) {
    return switch (ex) {
      case ResourceNotFoundException _ -> HttpStatus.NOT_FOUND;
      case FieldValidationException _ -> HttpStatus.BAD_REQUEST;
      case ConflictException _ -> HttpStatus.CONFLICT;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
