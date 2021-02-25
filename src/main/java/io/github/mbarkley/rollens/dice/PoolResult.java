package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static java.lang.String.format;

@RequiredArgsConstructor
@EqualsAndHashCode
public class PoolResult {
  @Getter
  private final UniformDicePool pool;
  private final int[] dropped;
  @Getter
  private final int[] values;

  public PoolResult(UniformDicePool pool, int... values) {
    this.pool = pool;
    this.values = values;
    this.dropped = new int[0];
  }

  public void writeString(String strikethroughMarker, StringBuilder sb) {
    sb.append("[");
    final String delimiter = ", ";
    for (var value : values) {
      sb.append(value)
        .append(delimiter);
    }
    if (dropped.length > 0) {
      sb.append(strikethroughMarker);
      for (var value : dropped) {
        sb.append(value)
          .append(delimiter);
      }
      sb.replace(sb.length() - delimiter.length(), sb.length(), strikethroughMarker)
        .append(']');
    } else if (values.length > 0) {
      sb.replace(sb.length() - delimiter.length(), sb.length(), "]");
    } else {
      sb.append("]");
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    writeString("~~", sb);

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
