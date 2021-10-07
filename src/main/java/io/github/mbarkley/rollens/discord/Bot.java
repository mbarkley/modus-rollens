package io.github.mbarkley.rollens.discord;

import io.github.mbarkley.rollens.eval.Command;
import io.github.mbarkley.rollens.eval.Command.ArgumentSelectOutput;
import io.github.mbarkley.rollens.eval.Command.CommandSelectOutput;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
import io.github.mbarkley.rollens.parse.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class Bot extends ListenerAdapter {
  public static final String COMMAND_SELECT_MENU_ID = "command-select-menu";
  public static final String ARGUMENT_SELECTOR_ID = "argument-selector";
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
    delete.addOption(
        OptionType.STRING,
        "save-name",
        "The name of a saved roll",
        true
    );
    delete.addOption(
        OptionType.INTEGER,
        "arity",
        "The number of parameters in the saved roll",
        true
    );
    final SubcommandData rollCmd = new SubcommandData("roll", "Roll some dice");
    rollCmd.addOption(
        OptionType.STRING,
        "dice-pool",
        "An expression of a dice pool (e.g. `2d6 + 1`)",
        true
    );
    final SubcommandData selectCmd = new SubcommandData("select", "Interactively select a saved roll");

    rootCmd.addSubcommands(
        helpCmd,
        list,
        save,
        delete,
        rollCmd,
        selectCmd
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

  @Override
  public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
    onCommandEvent(new CommandSelectMenuCommandEventAdapter(event));
  }

  @Override
  public void onButtonClick(@NotNull ButtonClickEvent event) {
    onCommandEvent(new ButtonCommandEventAdapter(event));
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
                      .whenComplete((Command.CommandOutput output, Throwable ex) -> {
                        if (ex != null) {
                          log.warn("Encountered error for message.id={}: {}", event.getId(), ex.getMessage());
                          log.debug("Exception stacktrace", ex);
                        } else switch (output) {
                          case StringOutput responseText -> {
                            log.debug("Sending response text for message.id={}", event.getId());
                            event.reply(responseText.value());
                          }
                          case CommandSelectOutput commandSelectOutput -> {
                            log.debug("Sending command select for message.id={}", event.getId());
                            final MessageBuilder builder = new MessageBuilder();
                            builder.setContent(commandSelectOutput.prompt());
                            List<SelectOption> options = commandSelectOutput.options()
                                                                            .stream()
                                                                            .map(option -> SelectOption.of(option.label(), option.selectExpression()))
                                                                            .toList();
                            builder.setActionRows(
                                ActionRow.of(
                                    new SelectionMenuImpl(
                                        COMMAND_SELECT_MENU_ID,
                                        "",
                                        0,
                                        1,
                                        false,
                                        options
                                    )
                                )
                            );

                            event.reply(builder.build(), true);
                          }
                          case ArgumentSelectOutput argumentSelectOutput -> {
                            log.debug("Sending response arg select for message.id={}", event.getId());
                            final MessageBuilder builder = new MessageBuilder();
                            builder.setContent(argumentSelectOutput.prompt());
                            final List<ButtonImpl> buttons =
                                IntStream.iterate(0, n -> n + 1)
                                         .limit(25)
                                         .mapToObj(String::valueOf)
                                         .map(n -> new ButtonImpl(
                                             argumentSelectOutput.selectExpression() + " " + n,
                                             n,
                                             ButtonStyle.SECONDARY,
                                             false,
                                             null))
                                         .toList();

                            builder.setActionRows(
                                ActionRow.of(buttons.subList(0, 5)),
                                ActionRow.of(buttons.subList(5, 10)),
                                ActionRow.of(buttons.subList(10, 15)),
                                ActionRow.of(buttons.subList(15, 20)),
                                ActionRow.of(buttons.subList(20, 25))
                            );

                            event.reply(builder.build(), true);
                          }
                        }
                      });
                } catch (Exception e) {
                  log.warn(format("Error while executing command message.id=%s", event.getId()), e);
                }
              },
              event::markIgnored);
  }
}