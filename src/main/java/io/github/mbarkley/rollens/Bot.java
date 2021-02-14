package io.github.mbarkley.rollens;

import io.github.mbarkley.rollens.parse.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class Bot extends ListenerAdapter {
    private static final String COMMAND_PREFIX = "!mr ";
    private final Parser parser;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        parser.parse(msg)
        .ifPresent(command -> {
            try {
                command.execute()
                       .thenAccept(responseText -> event.getChannel().sendMessage(responseText).queue());
            } catch (Exception e) {
                log.warn(format("Error while executing command [%s]", msg.getContentRaw()), e);
            }
        });
    }
}