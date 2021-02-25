package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Help implements Command {
  public static Help INSTANCE = new Help();
  public static String HELP_MESSAGE =
      """
          __Roll Dice__
          `!mr 5d10`: Roll five ten-sided dice
          `!mr 2d6 + 3d4`: Roll two six-sided dice and three four-sided dice (can also use `-`, `*`, and `/`).
          `!mr 2d6 + 1`: Roll two six-sided dice and add one (also supports `-`, `*`, `/`); must come after other flags.
          `!mr 4 (2d10 + d6)`: Repeat roll of two ten-sided dice and one six-sided die four times. Repeated roll can include other modifiers.
          
          __Counting Results__
          `!mr 5d10 t6`: Roll five ten-sided dice and count successes (six and above).
          `!mr 5d10 t6 f2`: As above, but subtract failures (2 or below) from successes.
          
          __Re-Roll Modifiers__
          `!mr 5d10 e10`: Roll five ten-sided dice but roll one additional die for each ten (sometimes called "exploding" dice).
          `!mr 5d10 ie10`: As above, but explode on tens indefinitely (capped at one-hundred times to prevent abuse).
          `!mr 5d10 r3`: Roll five ten-sided dice, take any results less than or equal to three, and re-roll them once.
          
          __Keeping and Dropping Dice__
          `!mr 5d10 k3`: Roll five ten-sided dice, keep the three highest results.
          `!mr 5d10 d3`: Roll five ten-sided dice, drop the lowest three results.
          
          __Save Custom Rolls__
          `!mr save (werewolf num diff) = {num}d10 t{diff} f1`: Save a roll called `werewolf`.
          `!mr save (werewolf num) = {num}d10 t6 f1`: Save a roll called `werewolf` that has a different number of inputs from the other saved roll.
          
          __Use a Saved Roll__
          `!mr werewolf 5 6`: Use the saved roll, `werewolf`, with `num=5` and `diff=6`.
          
          __Show Saved Rolls__
          `!mr list`: Lists all saved rolls.
          
          __Delete Saved Roll__
          `!mr delete werewolf 2`: Delete the saved roll called `werewolf` that has two inputs.
          
          __Show This Help Message__
          `!mr help`""";

  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    return CompletableFuture.completedFuture(HELP_MESSAGE);
  }
}
