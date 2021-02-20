package io.github.mbarkley.rollens.dice;

import lombok.Value;

@Value
public class PoolResult {
  UniformDicePool pool;
  int[] values;

  public void writeString(StringBuilder sb) {
    sb.append('[');
    final String delimiter = ", ";
    for (var value : values) {
      sb.append(value)
        .append(delimiter);
    }
    if (values.length > 0) {
      sb.replace(sb.length() - delimiter.length(), sb.length(), "]");
    } else {
      sb.append(']');
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    writeString(sb);

    return sb.toString();
  }

  public boolean isEmpty() {
    return values.length == 0;
  }
}
