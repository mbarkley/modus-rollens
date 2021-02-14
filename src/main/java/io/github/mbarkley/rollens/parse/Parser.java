package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.command.Command;
import io.github.mbarkley.rollens.command.SimpleRoll;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Parser {
    private static final Pattern SIMPLE_ROLL = Pattern.compile("^!mr\\s+((\\d+)d(\\d+))\\s*$");

    public Optional<Command> parse(Message message) {
        Matcher matcher = SIMPLE_ROLL.matcher(message.getContentRaw());
        if (matcher.matches()) {
            try {
                int numberOfDice = Integer.parseInt(matcher.group(2));
                int numberOfSides = Integer.parseInt(matcher.group(3));
                return Optional.of(new SimpleRoll(message, numberOfDice, numberOfSides));
            } catch (NumberFormatException nfe) {
                log.debug("Failed to parse numbers in command: {}", message.getContentRaw());
            }
        }

        return Optional.empty();
    }
}
