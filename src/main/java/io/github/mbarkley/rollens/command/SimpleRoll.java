package io.github.mbarkley.rollens.command;

import io.github.mbarkley.rollens.format.Formatter;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.String.format;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class SimpleRoll implements Command {
    private final int numberOfDice;
    private final int numberOfSides;

    @Override
    public CompletableFuture<String> execute(Message message, Formatter formatter) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int[] rawRolls = IntStream.generate(() -> rand.nextInt(1, numberOfSides + 1))
                                  .limit(numberOfDice)
                                  .toArray();
        int sum = Arrays.stream(rawRolls).sum();
        String responseText = formatter.formatResponse(message, rawRolls, sum);

        return CompletableFuture.completedFuture(responseText);
    }
}