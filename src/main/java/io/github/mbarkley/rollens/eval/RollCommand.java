package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.eval.RollModifier.State;
import io.github.mbarkley.rollens.util.MessageUtil;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.join;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class RollCommand implements Command {
  private final DicePool base;
  private final List<RollModifier> rollModifiers;
  private final ResultMapper resultMapper;

  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    final Random rand = context.getRand();
    final Roll[] rolls = base.execute(rand)
                             .toArray(Roll[]::new);
    final Message message = context.getMessage();
    final String username = MessageUtil.getAuthorDisplayName(message);
    final String arrayValues = Arrays.stream(rolls).map(Roll::getValue).map(Object::toString)
                                 .collect(Collectors.joining(", "));
    State state = new State(rolls, format("%s roll: `[%s]`", username, arrayValues));
    for (RollModifier rollModifier : rollModifiers) {
      state = rollModifier.modify(rand, base, state);
    }
    final String value = resultMapper.mapResult(message, state.getRollValues());

    return CompletableFuture.completedFuture(format("%s\nResult: %s", state.getLog(), value));
  }

}
