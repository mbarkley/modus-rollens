package io.github.mbarkley.rollens;

import io.github.mbarkley.rollens.eval.Command;
import io.github.mbarkley.rollens.parse.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jdbi.v3.core.Jdbi;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class Bot extends ListenerAdapter {
  private final Parser parser;
  private final Jdbi jdbi;
  private final ExecutorService executorService;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    Message message = event.getMessage();
    if (log.isDebugEnabled()) {
      if (message.isFromGuild()) {
        log.debug("Parsing message from guild/channel=[{}/{}]",
                  message.getGuild().getName(),
                  message.getChannel().getName());
      } else {
        log.debug("Parsing DM from user=[{}]", message.getAuthor().getId());
      }
    }
    parser.parse(message.getContentRaw())
          .ifPresent(command -> {
            try {
              if (message.isFromGuild()) {
                log.info("Executing guild/channel/command=[{}/{}/{}]",
                         message.getGuild().getId(),
                         message.getChannel().getId(), command);
              } else {
                log.info("Executing user/command=[{}/{}]", message.getAuthor().getId(), command);
              }
              command.execute(new Command.ExecutionContext(executorService, jdbi, parser, new Random(), message))
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