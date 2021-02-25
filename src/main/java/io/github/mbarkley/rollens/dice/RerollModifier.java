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
  private final int threshold;

  @Override
  public ModifierType type() {
    return ModifierType.REROLL;
  }

  @Override
  public void modify(Random rand, List<List<PoolResult>> allResults) {
    if (allResults.isEmpty()) {
      throw new IllegalArgumentException("Empty results");
    }

    List<PoolResult> newRolls = new ArrayList<>();
    for (List<PoolResult> poolResults : allResults) {
      for (int j = 0; j < poolResults.size(); j++) {
        PoolResult poolResult = poolResults.get(j);
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
        poolResults.set(j, new PoolResult(poolResult.getPool(), dropped, values));
      }
    }

    if (!newRolls.isEmpty()) allResults.add(newRolls);
  }
}
