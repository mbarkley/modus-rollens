package io.github.mbarkley.rollens;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Slf4j
public class Bot extends ListenerAdapter {
    private static final String COMMAND_PREFIX = "!mr ";
    private static final Pattern SIMPLE_ROLL = Pattern.compile("^!mr\\s+((\\d+)d(\\d+))\\s*");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        if (msg.getContentRaw().startsWith(COMMAND_PREFIX)) {
            Matcher matcher = SIMPLE_ROLL.matcher(msg.getContentRaw());
            if (matcher.matches()) {
                try {
                    int numberOfDice = Integer.parseInt(matcher.group(2));
                    int numberOfSides = Integer.parseInt(matcher.group(3));
                    ThreadLocalRandom rand = ThreadLocalRandom.current();
                    int[] rawRolls = IntStream.generate(() -> rand.nextInt(1, numberOfSides + 1))
                                              .limit(numberOfDice)
                                              .toArray();
                    int sum = Arrays.stream(rawRolls).sum();
                    MessageChannel channel = event.getChannel();
                    channel.sendMessageFormat("%s Roll: `%s`\nResult: %d",
                                              msg.getAuthor().getName(),
                                              Arrays.toString(rawRolls),
                                              sum)
                           .queue();
                } catch (NumberFormatException nfe) {
                    log.debug("Failed to parse numbers in command: {}", msg.getContentRaw());
                }
            }
        }
    }
}