package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.eval.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.mbarkley.rollens.parse.SlashCommandParser.IntegerOptionIdentifier.ARITY;
import static io.github.mbarkley.rollens.parse.SlashCommandParser.StringOptionIdentifier.*;

@RequiredArgsConstructor
public class SlashCommandParser {

  public List<CommandData> getSlashCommands() {
    final List<SlashCommandSpec> commandSpecs = List.of(
        new SlashCommandSpec(
            "mr",
            "help",
            "Display instructions for Modus Rollens bot",
            List.of()
        ),
        new SlashCommandSpec(
            "mr",
            "list",
            "Display saved rolls",
            List.of()
        ),
        new SlashCommandSpec(
            "mr",
            "save",
            "Save a roll by name",
            List.of(
                new OptionSpec<>(
                    SAVE_ASSIGNMENT,
                    true
                )
            )
        ),
        new SlashCommandSpec(
            "mr",
            "delete",
            "Delete a saved roll by name",
            List.of(
                new OptionSpec<>(
                    ROLL_NAME,
                    true
                ),
                new OptionSpec<>(
                    ARITY,
                    true
                )
            )
        ),
        new SlashCommandSpec(
            "mr",
            "roll",
            "Roll some dice",
            List.of(
                new OptionSpec<>(
                    DICE_POOL,
                    true
                )
            )
        ),
        new SlashCommandSpec(
            "mr",
            "select",
            "Interactively select a saved roll",
            List.of()
        ),
        new SlashCommandSpec(
            "mr",
            "annotate",
            "Annotate a saved roll",
            List.of(
                new OptionSpec<>(
                    ANNOTATION,
                    true
                ),
                new OptionSpec<>(
                    ROLL_NAME,
                    true
                ),
                new OptionSpec<>(
                    ARITY,
                    false
                ),
                new OptionSpec<>(
                    PARAMETER,
                    false
                )
            )
        )
    );
    final SlashCommandSpec rootSpec = new SlashCommandSpec(
        null,
        "mr",
        "Root command for Modus Rollens",
        List.of()
    );
    final CommandData rootCmd = new CommandData(rootSpec.command(), rootSpec.description());
    // If there's ever more than one level of command nesting, need to do some kind of mapping and topological sort
    rootCmd.addSubcommands(
        commandSpecs.stream()
                    .map(spec -> {
                      final SubcommandData subcommandData = new SubcommandData(spec.command(), spec.description());
                      spec.options().forEach(optionSpec -> subcommandData.addOption(
                          optionSpec.type(),
                          optionSpec.identifier().getName(),
                          optionSpec.identifier().getDescription(),
                          optionSpec.required()
                      ));

                      return subcommandData;
                    })
                    .toList());

    return List.of(rootCmd);
  }

  public static class InvalidSlashCommand extends Exception {
    public InvalidSlashCommand(String message) {
      super(message);
    }
  }

  public record SlashCommand(List<String> path, List<Option<?>> options) {
  }

  public sealed interface Option<T> {
    String name();

    T value();

    default String valueAsString() {
      return value().toString();
    }
  }

  private record SlashCommandSpec(String parent, String command, String description, List<OptionSpec<?>> options) {
  }

  private record OptionSpec<T extends OptionIdentifier>(T identifier, boolean required) {
    OptionType type() {
      return switch ((OptionIdentifier) identifier) {
        case StringOptionIdentifier ignored -> OptionType.STRING;
        case IntegerOptionIdentifier ignored -> OptionType.INTEGER;
      };
    }
  }

  private sealed interface OptionIdentifier {
    String getName();

    String getDescription();
  }

  @RequiredArgsConstructor
  @Getter
  public enum StringOptionIdentifier implements OptionIdentifier {
    DICE_POOL("dice-pool", "An expression of a dice pool (e.g. `2d6 + 1`)"),
    ROLL_NAME("roll-name", "The name of a saved roll"),
    SAVE_ASSIGNMENT("save-assignment", "An assignment of a dice pool by name (e.g. `roll = 2d6 + 1` or `roll dex = d20 + {dex}`)"),
    ANNOTATION("annotation", "The text description for a saved roll or parameter"),
    PARAMETER("parameter-name", "The name of a parameter in a saved roll");
    private final String name;
    private final String description;

    public static StringOptionIdentifier match(String name) throws InvalidSlashCommand {
      return Arrays.stream(values())
          .filter(identifier -> identifier.name.equals(name))
          .findFirst()
          .orElseThrow(() -> new InvalidSlashCommand("Unknown option [%s]".formatted(name)));
    }
  }

  @RequiredArgsConstructor
  @Getter
  public enum IntegerOptionIdentifier implements OptionIdentifier {
    ARITY("arity", "The number of parameters in the saved roll");
    private final String name;
    private final String description;

    public static IntegerOptionIdentifier match(String name) throws InvalidSlashCommand {
      return Arrays.stream(values())
                   .filter(identifier -> identifier.name.equals(name))
                   .findFirst()
                   .orElseThrow(() -> new InvalidSlashCommand("Unknown option [%s]".formatted(name)));
    }
  }

  public record StringOption(StringOptionIdentifier optionName, String value) implements Option<String> {
    @Override
    public String name() {
      return optionName.getName();
    }

    @Override
    public String valueAsString() {
      return value;
    }
  }

  public record IntegerOption(IntegerOptionIdentifier optionName, Long value) implements Option<Long> {
    @Override
    public String name() {
      throw new UnsupportedOperationException();
    }
  }

  private final TextParser textParser;

  public Command<?> parse(SlashCommand input) throws InvalidSlashCommand {
    final String textInput;
    if (input.path().size() > 1 && input.path().get(1).equals("annotate")) {
      textInput = "/mr annotate %s ! %s".formatted(
          input.options().stream().filter(option -> !option.name().equals(ANNOTATION.getName()))
               .map(Option::valueAsString).collect(Collectors.joining(" ")),
          input.options().stream().filter(option -> option.name().equals(ANNOTATION.getName())).findFirst()
               .orElseThrow(() -> new InvalidSlashCommand("Could not recognize command. See `/mr help` for valid examples."))
      );
    } else {
      textInput = """
        /%s %s""".formatted(
          String.join(" ", input.path()),
          input.options().stream().map(Option::valueAsString).collect(Collectors.joining(" "))
      );
    }

    return textParser.parse(textInput)
                     .orElseThrow(() -> new InvalidSlashCommand("Could not recognize command. See `/mr help` for valid examples."));
  }
}
