package io.github.mbarkley.rollens.dice;

import lombok.Value;

import java.util.Arrays;
import java.util.Random;

@Value
public class DicePool {
  UniformDicePool[] uniformDicePool;

  public DicePool(UniformDicePool... uniformDicePool) {
    this.uniformDicePool = uniformDicePool;
  }

  public PoolResult[] execute(Random rand) {
    return Arrays.stream(uniformDicePool)
                 .map(p -> p.execute(rand))
                 .toArray(PoolResult[]::new);
  }
}
