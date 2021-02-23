package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class ExplodingModifier implements RollModifier {
  public static final int MAX_ITERATION_CAP = 100;
  private final int explodingThreshold;
  private final int maxIterations;

  @Override
  public ModifierType type() {
    return ModifierType.EXPLOSIVE;
  }

  @Override
  public void modify(Random rand, List<List<PoolResult>> allResults) {
    if (allResults.isEmpty()) {
      throw new IllegalArgumentException("Empty results");
    }

    int iterationCap = Math.min(maxIterations, MAX_ITERATION_CAP);
    for (int i = 0; i < iterationCap; i++) {
      final List<PoolResult> baseResults = allResults.get(allResults.size() - 1);
      final List<PoolResult> explodedResults =
          baseResults.stream()
                     .filter(pr -> !pr.isEmpty())
                     .map(baseResult -> new PoolResult(baseResult.getPool(),
                                                       Arrays.stream(baseResult.getValues())
                                                             .filter(value -> value >= explodingThreshold)
                                                             .map(value -> baseResult
                                                                 .getPool()
                                                                 .rollSingle(rand))
                                                             .toArray())
                     )
                     .collect(Collectors.toList());
      allResults.add(explodedResults);
      if (explodedResults.size() == 0 || explodedResults.stream().allMatch(PoolResult::isEmpty)) break;
    }
  }
}
