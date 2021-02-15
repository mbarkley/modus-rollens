package io.github.mbarkley.rollens.eval;

import java.util.Random;

public interface RollModifier {
    int[] modify(Random rand, BaseRoll baseRoll, int[] rawRolls);
}
