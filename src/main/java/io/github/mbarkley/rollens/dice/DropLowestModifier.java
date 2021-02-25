package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class DropLowestModifier implements RollModifier {
  private final int drop;

  @Override
  public ModifierType type() {
    return ModifierType.DROP_LOW;
  }

  @Override
  public void modify(Random rand, List<List<PoolResult>> allResults) {
    if (allResults.isEmpty()) {
      throw new IllegalArgumentException("empty results");
    }
    final int numDice = allResults.stream()
                                  .flatMap(Collection::stream)
                                  .mapToInt(pr -> pr.getValues().length)
                                  .sum();

    new KeepHighestModifier(numDice - drop).modify(rand, allResults);
  }
}
