package io.github.mbarkley.rollens.db;

import lombok.*;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public abstract class BaseSavedRoll<T extends BaseSavedRoll<T>> implements Comparable<T> {
  long guildId;
  String rollName;
  byte arity;
  @Getter(onMethod = @__(@ColumnName("parameters")))
  String encodedParameters;
  String expression;

  protected BaseSavedRoll(long guildId, String rollName, List<String> parameters, String expression) {
    this(guildId, rollName, validateParameterLength(parameters), encodeParameters(parameters), expression);
  }

  private static byte validateParameterLength(List<String> parameters) {
    final byte byteVal = (byte) parameters.size();
    if (byteVal != parameters.size()) {
      throw new IllegalArgumentException(format("Given %d parameters, but length must fit into byte", parameters.size()));
    }
    return byteVal;
  }

  @NotNull
  private static String encodeParameters(List<String> encodedParameters) {
    return String.join(":", encodedParameters);
  }

  public List<String> getDecodedParameters() {
    if (encodedParameters.isEmpty()) {
      return List.of();
    } else {
      return Arrays.asList(encodedParameters.split(":"));
    }
  }

  public String toAssignmentString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('(').append(rollName);
    for (var param : getDecodedParameters()) {
      sb.append(' ')
        .append(param);
    }
    sb.append(") = ")
      .append(expression);

    return sb.toString();
  }

  public String toLHS() {
    final StringBuilder sb = new StringBuilder();
    sb.append(rollName);
    for (var param : getDecodedParameters()) {
      sb.append(' ')
        .append(param);
    }

    return sb.toString();
  }

  @Override
  public int compareTo(@NotNull T o) {
    final int first = rollName.compareTo(o.getRollName());
    if (first != 0) {
      return first;
    } else {
      return Integer.compare(arity, o.getArity());
    }
  }
}
