package io.github.mbarkley.rollens.eval;

import lombok.Value;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

@Value
public class DicePool {
  UniformDicePool[] uniformDicePool;

  public DicePool(UniformDicePool... uniformDicePool) {
    this.uniformDicePool = uniformDicePool;
  }

  public Stream<Roll> execute(Random rand) {
    return Arrays.stream(uniformDicePool)
                 .flatMap(p -> p.execute(rand)
                                .mapToObj(value -> new Roll(p.getNumberOfSides(), value))
                 );
  }
}
