package com.plavonra.util;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonUtil {
  private static final String EMAIL_REGEX =
      "^[a-zA-Z0-9_+&*-]++(?:\\.[a-zA-Z0-9_+&*-]++)*+@(?:[a-zA-Z0-9-]++\\.)++[a-zA-Z]{2,7}$";

  /**
   * This method validates the input email address with EMAIL_REGEX pattern
   *
   * @param email incoming request
   * @return boolean
   */
  public static boolean validateEmail(final String email) {
    if (email == null || email.isBlank()) {
      return false;
    }
    return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
  }

  public static boolean notValidateEmail(final String email) {
    return !validateEmail(email);
  }
}
