package io.github.mbarkley.rollens.discord;

import io.github.mbarkley.rollens.eval.Command;
import io.github.mbarkley.rollens.eval.Command.ArgumentSelectOutput;
import io.github.mbarkley.rollens.eval.Command.CommandSelectOutput;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
import io.github.mbarkley.rollens.parse.SlashCommandParser;
import io.github.mbarkley.rollens.parse.SlashCommandParser.IntegerOptionIdentifier;
import io.github.mbarkley.rollens.parse.SlashCommandParser.StringOptionIdentifier;
import io.github.mbarkley.rollens.parse.TextParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class Bot extends ListenerAdapter {
  public static final String COMMAND_SELECT_MENU_ID = "command-select-menu";
  private final TextParser textParser;
  public final SlashCommandParser slashCommandParser;
  private final Jdbi jdbi;
  private final ExecutorService executorService;

  @Override
  public void onSlashCommand(@NotNull SlashCommandEvent event) {
    final List<String> path = Arrays.asList(event.getCommandPath().split("/"));
    try {
      final List<SlashCommandParser.Option<?>> options = convertOptions(event.getOptions());
      processCommand(
          new SlashCommandEventAdapter(event),
          slashCommandParser.parse(new SlashCommandParser.SlashCommand(path, options)));
    } catch (SlashCommandParser.InvalidSlashCommand e) {
      event.reply(e.getMessage()).queue();
    }
  }

  @NotNull
  private List<SlashCommandParser.Option<?>> convertOptions(List<OptionMapping> optionMappings) throws SlashCommandParser.InvalidSlashCommand {
    final List<SlashCommandParser.Option<?>> options = new ArrayList<>();
    for (var optionMapping : optionMappings) {
      options.add(switch (optionMapping.getType()) {
        case STRING -> convertStringOption(optionMapping);
        case INTEGER -> convertIntegerOption(optionMapping);
        default -> throw new IllegalStateException("Unexpected value: " + optionMapping.getType());
      });
    }
    return options;
  }

  @NotNull
  private SlashCommandParser.Option<?> convertIntegerOption(OptionMapping optionMapping) throws SlashCommandParser.InvalidSlashCommand {
    final IntegerOptionIdentifier name = IntegerOptionIdentifier.match(optionMapping.getName());
    return new SlashCommandParser.IntegerOption(name, optionMapping.getAsLong());
  }

  @NotNull
  private SlashCommandParser.Option<?> convertStringOption(OptionMapping optionMapping) throws SlashCommandParser.InvalidSlashCommand {
    final StringOptionIdentifier name = StringOptionIdentifier.match(optionMapping.getName());
    return new SlashCommandParser.StringOption(name, optionMapping.getAsString());
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    final String input = event.getMessage().getContentRaw();
    textParser.parse(input)
              .ifPresent(
              command -> processCommand(new MessageCommandEventAdapter(event), command));
  }

  @Override
  public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
    if (event.getValues().size() == 1) {
      final String commandString = event.getValues().get(0);
      textParser.parse(commandString)
                .ifPresentOrElse(
                command -> processCommand(new CommandSelectMenuCommandEventAdapter(event), command),
                () -> event.reply("Could not process selection [%s]."
                                      .formatted(String.join(", ", event.getValues()))).queue());
    } else {
      log.warn("Select menu returned with too many values selected");
    }
  }

  @Override
  public void onButtonClick(@NotNull ButtonClickEvent event) {
    final String input = event.getComponentId();
    textParser.parse(input)
              .ifPresentOrElse(
              command -> processCommand(new ButtonCommandEventAdapter(event), command),
              () -> event.reply("Could not process selection [%s]."
                                    .formatted(String.join(", ", event.getComponentId()))).queue());
  }

  private void processCommand(CommandEvent event, Command<?> command) {
    try {
      if (event.isFromGuild()) {
        log.info("Executing guild/channel/command=[{}/{}/{}]",
                 event.getGuild().getId(),
                 event.getChannel().getId(), command);
      } else {
        log.info("Executing user/command=[{}/{}]", event.getUser().getId(), command);
      }
      command
          .execute(new Command.ExecutionContext(executorService, jdbi, textParser, ThreadLocalRandom::current, event))
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
  }
}