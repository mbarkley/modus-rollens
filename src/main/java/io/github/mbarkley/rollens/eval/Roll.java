package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.eval.RollModifier.State;
import io.github.mbarkley.rollens.util.MessageUtil;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Roll implements Command {
  private final BaseRoll base;
  private final List<RollModifier> rollModifiers;
  private final ResultMapper resultMapper;

  @Override
  public CompletableFuture<String> execute(Message message) {
    final ThreadLocalRandom rand = ThreadLocalRandom.current();
    final int[] rolls = base.execute(rand);
    final String username = MessageUtil.getAuthorDisplayName(message);
    State state = new State(rolls, format("%s roll: `%s`", username, Arrays.toString(rolls)));
    for (RollModifier rollModifier : rollModifiers) {
      state = rollModifier.modify(rand, base, state);
    }
    final String value = resultMapper.mapResult(message, state.getRolls());

    return CompletableFuture.completedFuture(format("%s\nResult: %s", state.getLog(), value));
  }

}
