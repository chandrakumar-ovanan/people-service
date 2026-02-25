package com.plavonra.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommonUtilTest {

  @Nested
  class ValidateEmail {

    @Test
    void validEmail_returnTrue() {
      assertThat(CommonUtil.validateEmail("test@example.com")).isTrue();
      assertThat(CommonUtil.validateEmail("user.name+tag@domain.co.uk")).isTrue();
      assertThat(CommonUtil.validateEmail("a@b.co")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "test@", "@test.com", "test.com", "", "null"})
    void invalidEmail_returnFalse(String email) {
      assertThat(CommonUtil.validateEmail(email)).isFalse();
    }
  }

  @Nested
  class NotValidateEmail {

    @Test
    void validEmail_returnFalse() {
      assertThat(CommonUtil.notValidateEmail("test@example.com")).isFalse();
    }

    @Test
    void invalidEmail_returnTrue() {
      assertThat(CommonUtil.notValidateEmail("invalid")).isTrue();
    }
  }
}
