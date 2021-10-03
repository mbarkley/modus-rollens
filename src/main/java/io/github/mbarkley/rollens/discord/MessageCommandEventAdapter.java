package io.github.mbarkley.rollens.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
class MessageCommandEventAdapter implements CommandEvent {
  private final MessageReceivedEvent event;

  @Override
  public @NotNull String getId() {
    return event.getMessageId();
  }

  @Override
  public @NotNull MessageChannel getChannel() {
    return event.getTextChannel();
  }

  @Override
  public @NotNull User getUser() {
    return event.getAuthor();
  }

  @Override
  public Member getMember() {
    return event.getMember();
  }

  @Override
  public @NotNull String getCommand() {
    return event.getMessage().getContentRaw();
  }

  @Override
  public void reply(String response) {
    getChannel().sendMessage(response).queue();
  }

  @Override
  public void markIgnored() {
    // Messages in channels are usually not directed at us, so we do nothing
  }
}
