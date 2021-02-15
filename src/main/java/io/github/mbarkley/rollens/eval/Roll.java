package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.format.Formatter;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Roll implements Command {
  private final BaseRoll base;
  private final List<RollModifier> rollModifiers;
  private final ResultMapper resultMapper;

  @Override
  public CompletableFuture<String> execute(Message message, Formatter formatter) {
    final ThreadLocalRandom rand = ThreadLocalRandom.current();
    int[] rawRolls = base.execute(rand);
    for (RollModifier rollModifier : rollModifiers) {
      rawRolls = rollModifier.modify(rand, base, rawRolls);
    }

    return CompletableFuture.completedFuture(resultMapper.mapResult(message, formatter, rawRolls));
  }
}
