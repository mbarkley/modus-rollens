package io.github.mbarkley.rollens.eval;

import lombok.Value;

import java.util.Random;
import java.util.stream.IntStream;

@Value
public class BaseRoll {
  int numberOfDice;
  int numberOfSides;

  public int[] execute(Random rand) {
    return IntStream.generate(() -> rand.nextInt(numberOfSides) + 1)
                    .limit(numberOfDice)
                    .toArray();
  }
}
