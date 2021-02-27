package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class RerollModifier implements RollModifier {
  private static final int MAX_ITERATION_CAP = 100;
  private final int threshold;
  private final int maxIterations;

  @Override
  public ModifierType type() {
    return ModifierType.REROLL;
  }

  @Override
  public void modify(Random rand, List<List<PoolResult>> allResults) {
    if (allResults.isEmpty()) {
      throw new IllegalArgumentException("Empty results");
    }

    final int bound = Math.min(maxIterations, MAX_ITERATION_CAP);
    final int startingSize = allResults.size();
    for (int i = 0; i < bound; i++) {
      List<PoolResult> newRolls = new ArrayList<>();
      for (int j = Integer.signum(i) * (startingSize - 1) + i; j < allResults.size(); j++) {
        List<PoolResult> poolResults = allResults.get(j);
        for (int k = 0; k < poolResults.size(); k++) {
          PoolResult poolResult = poolResults.get(k);
          final int[] dropped = IntStream.concat(Arrays.stream(poolResult.getValues())
                                                       .filter(n -> n <= threshold),
                                                 Arrays.stream(poolResult.getDropped()))
                                         .toArray();
          final int[] values = Arrays.stream(poolResult.getValues())
                                     .filter(n -> n > threshold)
                                     .toArray();
          final int[] rerolls = IntStream.generate(() -> poolResult.getPool().rollSingle(rand))
                                         .limit(dropped.length)
                                         .toArray();
          if (rerolls.length > 0) newRolls.add(new PoolResult(poolResult.getPool(), new int[0], rerolls));
          poolResults.set(k, new PoolResult(poolResult.getPool(), dropped, values));
        }
      }

      if (!newRolls.isEmpty()) allResults.add(newRolls);
    }
  }
}
