package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.eval.Command;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SlashCommandParser {
  public static class InvalidSlashCommand extends Exception {
    public InvalidSlashCommand(String message) {
      super(message);
    }
  }

  public record SlashCommand(List<String> path, List<Option<?>> options) {}

  public record Option<T>(String name, T value) {
    public String valueAsString() {
      return switch (value) {
        case String s -> s;
        default -> String.valueOf(value);
      };
    }
  }

  private final TextParser textParser;

  public Command<?> parse(SlashCommand input) throws InvalidSlashCommand {
    final String textInput = """
        /%s %s""".formatted(
        String.join(" ", input.path()),
        input.options().stream().map(Option::valueAsString).collect(Collectors.joining(" "))
    );

    return textParser.parse(textInput)
                     .orElseThrow(() -> new InvalidSlashCommand("Could not recognize command. See `/mr help` for valid examples."));
  }
}
