package io.github.mbarkley.rollens.util;

import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

public class EnvUtil {
  private EnvUtil() {}

  public static String requireEnvString(String name) {
    return Objects.requireNonNull(System.getenv(name), format("Value must be provided for environment variable [%s]", name));
  }

  public static String optionalEnvString(String name, String fallback) {
    return Optional.ofNullable(System.getenv(name)).orElse(fallback);
  }
}
