package com.plavonra.people.validation;

import java.util.Optional;

public final class PageRequestValidator {

  private PageRequestValidator() {}

  public static final String THE_SIZE_MUST_NOT_BE_ZERO_OR_NEGATIVE = "Size.must.not.zero.negative";
  public static final String THE_PAGE_MUST_NOT_BE_NEGATIVE = "Page.must.not.negative";

  public static Optional<String> validateRequest(Integer page, Integer size) {

    if (page == null && size == null) {
      return Optional.empty();
    }

    if (size != null && size <= 0) {
      return Optional.of(THE_SIZE_MUST_NOT_BE_ZERO_OR_NEGATIVE);
    }

    if (page != null && page < 0) {
      return Optional.of(THE_PAGE_MUST_NOT_BE_NEGATIVE);
    }

    return Optional.empty();
  }
}
