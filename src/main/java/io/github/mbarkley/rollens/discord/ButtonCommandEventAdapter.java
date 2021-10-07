package io.github.mbarkley.rollens.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
class ButtonCommandEventAdapter implements CommandEvent {
  private final ButtonClickEvent event;

  @Override
  public @NotNull String getId() {
    return event.getId();
  }

  @Override
  public @NotNull MessageChannel getChannel() {
    return event.getChannel();
  }

  @Override
  public @NotNull User getUser() {
    return event.getUser();
  }

  @Override
  public Member getMember() {
    return event.getMember();
  }

  @Override
  public @NotNull String getCommand() {
    return event.getComponentId();
  }

  @Override
  public void reply(@NotNull Message message, boolean intermediate) {
    if (event.getMessage().isEphemeral() && !intermediate) {
      final MessageBuilder builder = new MessageBuilder();
      builder.setContent("Complete");
      event.editMessage(builder.build())
           .and(event.getMessage().getChannel().sendMessage(message)).queue();
    } else {
      event.editMessage(message).queue();
    }
  }

  @Override
  public void markIgnored() {
    event.reply("Could not process selection [%s]."
                    .formatted(String.join(", ", event.getComponentId()))).queue();
  }
}
