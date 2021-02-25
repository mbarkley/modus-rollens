package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;

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
    final int numberOfDice = allResults.stream()
                                       .flatMap(Collection::stream)
                                       .mapToInt(pr -> pr.getValues().length)
                                       .sum();
    new KeepHighestModifier(numberOfDice - drop).modify(rand, allResults);
  }
}
