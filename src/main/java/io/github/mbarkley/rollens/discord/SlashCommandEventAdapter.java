package io.github.mbarkley.rollens.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

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
    return """
        /%s %s""".formatted(
        event.getCommandPath().replace('/', ' '),
        event.getOptions().stream().map(OptionMapping::getAsString).collect(Collectors.joining(" "))
    );
  }

  @Override
  public void reply(String response) {
    event.reply(response).queue();
  }

  @Override
  public void markIgnored() {
    event.reply("Could not recognize %s command. See `/mr help` for valid examples."
                    .formatted(event.getSubcommandName())).queue();
  }
}
