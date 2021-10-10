package io.github.mbarkley.rollens.discord;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

public interface CommandEvent {
  @NotNull String getId();

  @NotNull MessageChannel getChannel();

  @NotNull User getUser();

  Member getMember();

  void reply(@NotNull Message message, boolean intermediate);

  default void reply(@NotNull String response) {
    final MessageBuilder builder = new MessageBuilder();
    builder.setContent(response);
    reply(builder.build(), false);
  }

  default boolean isFromGuild() {
    return ChannelType.TEXT.equals(getChannel().getType());
  }

  default Guild getGuild() {
    if (isFromGuild()) {
      return ((TextChannel) getChannel()).getGuild();
    } else {
      throw new IllegalStateException("Event not from a text channel");
    }
  }
}
