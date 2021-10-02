package io.github.mbarkley.rollens.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
class SlashCommandEventAdapter implements CommandEvent {
  private final SlashCommandEvent event;

  @Override
  public @NotNull String getId() {
    return event.getCommandId();
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
    return event.getCommandString();
  }

  @Override
  public void reply(String response) {
    event.reply(response).queue();
  }
}
