package io.github.mbarkley.rollens.discord;

import io.github.mbarkley.rollens.eval.Command;
import io.github.mbarkley.rollens.parse.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class Bot extends ListenerAdapter {
  private final Parser parser;
  private final Jdbi jdbi;
  private final ExecutorService executorService;

  public List<CommandData> getSlashCommands() {
    final CommandData rootCmd = new CommandData("mr", "Root command for Modus Rollens");
    final SubcommandData helpCmd = new SubcommandData("help", "Display instructions for Modus Rollens bot");
    final SubcommandData list = new SubcommandData("list", "Display saved rolls");
    final SubcommandData save = new SubcommandData("save", "Save a roll by name");
    save.addOption(
        OptionType.STRING,
        "save-assignment",
        "An assignment of a dice pool by name (e.g. `roll = 2d6 + 1` or `roll dex = d20 + {dex}`)",
        true
    );
    final SubcommandData delete = new SubcommandData("delete", "Delete a saved roll by name");
    final SubcommandData rollCmd = new SubcommandData("roll", "Roll some dice");
    rollCmd.addOption(
        OptionType.STRING,
        "dice-pool",
        "An expression of a dice pool (e.g. `2d6 + 1`)", true
    );

    rootCmd.addSubcommands(
        helpCmd,
        list,
        save,
        delete,
        rollCmd
    );

    return List.of(rootCmd);
  }

  @Override
  public void onSlashCommand(@NotNull SlashCommandEvent event) {
    onCommandEvent(new SlashCommandEventAdapter(event));
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    onCommandEvent(new MessageCommandEventAdapter(event));
  }

  private void onCommandEvent(CommandEvent event) {
    if (log.isDebugEnabled()) {
      if (event.isFromGuild()) {
        log.debug("Parsing command from guild/channel=[{}/{}]",
                  event.getGuild().getName(),
                  event.getChannel().getName());
      } else {
        log.debug("Parsing DM from user=[{}]", event.getUser().getId());
      }
    }
    final String input = event.getCommand();
    parser.parse(input)
          .ifPresentOrElse(
              command -> {
                try {
                  if (event.isFromGuild()) {
                    log.info("Executing guild/channel/command=[{}/{}/{}]",
                             event.getGuild().getId(),
                             event.getChannel().getId(), command);
                  } else {
                    log.info("Executing user/command=[{}/{}]", event.getUser().getId(), command);
                  }
                  command
                      .execute(new Command.ExecutionContext(executorService, jdbi, parser, ThreadLocalRandom::current, event))
                      .whenComplete((responseText, ex) -> {
                        if (ex != null) {
                          log.warn("Encountered error for message.id={}: {}", event.getId(), ex.getMessage());
                          log.debug("Exception stacktrace", ex);
                        } else {
                          log.debug("Sending response text for message.id={}", event.getId());
                          event.reply(responseText);
                        }
                      });
                } catch (Exception e) {
                  log.warn(format("Error while executing command message.id=%s", event.getId()), e);
                }
              },
              event::markIgnored);
  }
}