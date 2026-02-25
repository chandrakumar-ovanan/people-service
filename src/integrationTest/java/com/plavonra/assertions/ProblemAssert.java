package com.plavonra.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class ProblemAssert extends AbstractAssert<ProblemAssert, ProblemDetail> {

  private static final String DETAIL_MESSAGE = "Detail message";
  private static final String TITLE_MESSAGE = "Title message";
  private static final String HTTP_STATUS = "HTTP status";

  protected ProblemAssert(ProblemDetail actual) {
    super(actual, ProblemAssert.class);
  }

  public static ProblemAssert assertThat(ProblemDetail actual) {
    return new ProblemAssert(actual);
  }

  public ProblemAssert hasDetail(String expectedDetail) {
    return assertFieldEquality(actual.getDetail(), expectedDetail, DETAIL_MESSAGE);
  }

  public ProblemAssert hasStatus(HttpStatus expectedStatus) {
    return assertFieldEquality(actual.getStatus(), expectedStatus.value(), HTTP_STATUS);
  }

  public ProblemAssert problemAssert(
      HttpStatus expectedStatus, String expectedTitle, String expectedDetail) {
    SoftAssertions.assertSoftly(
        softly -> {
          assertFieldEquality(softly, actual.getStatus(), expectedStatus.value(), HTTP_STATUS);
          assertFieldEquality(softly, actual.getDetail(), expectedDetail, DETAIL_MESSAGE);
          assertFieldEquality(softly, actual.getTitle(), expectedTitle, TITLE_MESSAGE);
        });
    return this;
  }

  private <T> ProblemAssert assertFieldEquality(T actualField, T expectedField, String fieldName) {
    if (!actualField.equals(expectedField)) {
      failWithMessage("Expected %s '%s' but was '%s'", fieldName, expectedField, actualField);
    }
    return this;
  }

  private <T> void assertFieldEquality(
      SoftAssertions softly, T actualField, T expectedField, String fieldName) {
    softly.assertThat(actualField).as("Expected " + fieldName).isEqualTo(expectedField);
  }
}
