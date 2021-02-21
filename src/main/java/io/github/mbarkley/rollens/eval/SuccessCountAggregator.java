package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

import java.util.stream.IntStream;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@With
public class SuccessCountAggregator implements ResultAggregator {
  private final int successThreshold;
  private final int failureThreshold;

  @Override
  public int combineResult(IntStream rawRolls) {

    return rawRolls.map(value -> {
      if (value >= successThreshold) {
        return 1;
      } else if (value <= failureThreshold) {
        return -1;
      } else {
        return 0;
      }
    }).sum();
  }
}
