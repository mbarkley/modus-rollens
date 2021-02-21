package io.github.mbarkley.rollens.dice;

import lombok.Value;

import java.util.Arrays;

import static java.lang.String.format;

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

  public static PoolResult combine(PoolResult left, PoolResult right) {
    if (left.getPool().getNumberOfSides() == right.getPool().getNumberOfSides()) {
      final int totalNumDice = left.getPool().getNumberOfDice() + right.getPool().getNumberOfDice();
      final int[] allValues = Arrays.copyOf(left.getValues(), left.getValues().length + right.getValues().length);
      System.arraycopy(right.getValues(), 0, allValues, left.getValues().length, right.getValues().length);
      return new PoolResult(new UniformDicePool(totalNumDice, left.getPool().getNumberOfSides()), allValues);
    } else {
      throw new IllegalArgumentException(format("Dice sides don't match for arguments [%s] and [%s]", left.getPool(), right.getPool()));
    }
  }
}
