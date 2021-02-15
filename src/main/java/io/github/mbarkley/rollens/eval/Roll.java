package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.format.Formatter;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Roll implements Command {
    private final SimpleRoll base;
    private final List<RollModifier> rollModifiers;
    private final ResultMapper resultMapper;

    @Override
    public CompletableFuture<String> execute(Message message, Formatter formatter) {
        int[] rawRolls = base.execute();
        for (RollModifier rollModifier : rollModifiers) {
            rawRolls = rollModifier.modify(rawRolls);
        }

        return CompletableFuture.completedFuture(resultMapper.mapResult(message, formatter, rawRolls));
    }
}
