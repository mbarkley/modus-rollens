package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class ExplodingModifier implements RollModifier {
    public static final int MAX_ITERATION_CAP = 100;
    private final int explodingThreshold;
    private final int maxIterations;
    @Override
    public int[] modify(Random rand, BaseRoll baseRoll, int[] rawRolls) {
        return Arrays.stream(rawRolls)
                     .flatMap(roll -> IntStream
                             .iterate(roll, r -> r >= explodingThreshold, r -> rand
                                     .nextInt(baseRoll.getNumberOfSides()) + 1)
                             .limit(Math.min(maxIterations, MAX_ITERATION_CAP)))
                     .toArray();
    }
}
