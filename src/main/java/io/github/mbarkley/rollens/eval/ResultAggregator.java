package io.github.mbarkley.rollens.eval;

import java.util.stream.IntStream;

public interface ResultAggregator {
  int combineResult(IntStream rawRolls);
}
