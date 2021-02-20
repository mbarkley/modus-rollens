package io.github.mbarkley.rollens.eval;

import lombok.Value;

import java.util.Random;
import java.util.stream.IntStream;

@Value
public class UniformDicePool {
  int numberOfDice;
  int numberOfSides;

  public IntStream execute(Random rand) {
    return IntStream.generate(() -> rand.nextInt(numberOfSides) + 1)
                    .limit(numberOfDice);
  }
}
