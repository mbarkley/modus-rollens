package io.github.mbarkley.rollens.dice;

import lombok.Value;

import java.util.Random;
import java.util.stream.IntStream;

@Value
public class UniformDicePool {
  int numberOfDice;
  int numberOfSides;

  public PoolResult execute(Random rand) {
    final int[] values = IntStream.generate(() -> rollSingle(rand))
                                  .limit(numberOfDice)
                                  .toArray();
    return new PoolResult(this, new int[0], values);
  }

  public int rollSingle(Random rand) {
    return rand.nextInt(numberOfSides) + 1;
  }
}
