package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class SimpleRoll {
    private final int numberOfDice;
    private final int numberOfSides;

    public int[] execute() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        return IntStream.generate(() -> rand.nextInt(1, numberOfSides + 1))
                                  .limit(numberOfDice)
                                  .toArray();
    }
}
