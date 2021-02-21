package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.stream.IntStream;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class SumAggregator implements ResultAggregator {
  @Override
  public int combineResult(IntStream rawRolls) {
    return rawRolls.sum();
  }
}
