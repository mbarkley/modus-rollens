package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.math.Operator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.stream.IntStream;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class OperationAggregator implements ResultAggregator {

  private final ResultAggregator left;
  private final Operator operator;
  private final int right;

  @Override
  public int combineResult(IntStream rawRolls) {
    final int left = this.left.combineResult(rawRolls);
    return operator.apply(left, right);
  }
}
