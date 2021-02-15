package io.github.mbarkley.rollens;

import io.github.mbarkley.rollens.parse.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jdbi.v3.core.Jdbi;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class Bot extends ListenerAdapter {
  private final Parser parser;
  private final Jdbi jdbi;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    Message message = event.getMessage();
    if (log.isDebugEnabled()) {
      log.debug("Parsing message from guild/channel=[{}/{}]",
                message.getGuild().getName(),
                message.getChannel().getName());
    }
    parser.parse(message)
          .ifPresent(command -> {
            try {
              log.info("Executing guild/channel/command=[{}/{}/{}]",
                       message.getGuild().getId(),
                       message.getChannel().getId(), command);
              log.debug("Executing command: {}", command);
              command.execute(ThreadLocalRandom.current(), message)
                     .whenComplete((responseText, ex) -> {
                       if (ex != null) {
                         log.warn("Encountered error for message.id={}: {}", message.getId(), ex.getMessage());
                         log.debug("Exception stacktrace", ex);
                       } else {
                         log.debug("Sending response text for message.id={}", message.getId());
                         event.getChannel().sendMessage(responseText).queue();
                       }
                     });
            } catch (Exception e) {
              log.warn(format("Error while executing command message.id=%s", message.getId()), e);
            }
          });
  }
}