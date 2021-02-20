package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.dice.PoolResult;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class ExplodingModifier implements RollModifier {
  public static final int MAX_ITERATION_CAP = 100;
  private final int explodingThreshold;
  private final int maxIterations;

  @Override
  public void modify(Random rand, List<PoolResult[]> allResults) {
    if (allResults.isEmpty()) {
      throw new IllegalArgumentException("Empty results");
    }

    int iterationCap = Math.min(maxIterations, MAX_ITERATION_CAP);
    for (int i = 0; i < iterationCap; i++) {
      final PoolResult[] baseResults = allResults.get(allResults.size() - 1);
      final PoolResult[] explodedResults =
          Arrays.stream(baseResults)
                .filter(pr -> !pr.isEmpty())
                .map(baseResult -> new PoolResult(baseResult.getPool(),
                                                  Arrays.stream(baseResult.getValues())
                                                        .filter(value -> value >= explodingThreshold)
                                                        .map(value -> baseResult
                                                            .getPool()
                                                            .rollSingle(rand))
                                                        .toArray())
                )
                .toArray(PoolResult[]::new);
      allResults.add(explodedResults);
      if (explodedResults.length == 0 || Arrays.stream(explodedResults).allMatch(PoolResult::isEmpty)) break;
    }
  }
}
