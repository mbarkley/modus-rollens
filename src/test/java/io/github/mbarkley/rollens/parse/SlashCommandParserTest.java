package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.eval.*;
import io.github.mbarkley.rollens.parse.SlashCommandParser.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SlashCommandParserTest {
  SlashCommandParser slashCommandParser = new SlashCommandParser(new TextParser());

  @ParameterizedTest(name = "call \"{0}\" as {1}")
  @MethodSource("calls")
  public void should_saved_rolls(SlashCommand input, Object result) throws SlashCommandParser.InvalidSlashCommand {
    final Command<?> parsed = slashCommandParser.parse(input);
    Assertions.assertEquals(result, parsed);
  }

  @Test
  public void should_parse_help() throws SlashCommandParser.InvalidSlashCommand {
    final Command<?> parsed = slashCommandParser.parse(new SlashCommand(List.of("mr", "help"), List.of()));
    Assertions.assertEquals(Help.INSTANCE, parsed);
  }

  @Test
  public void should_parse_list() throws SlashCommandParser.InvalidSlashCommand {
    final Command<?> parsed = slashCommandParser.parse(new SlashCommand(List.of("mr", "list"), List.of()));
    Assertions.assertEquals(ListSaved.INSTANCE, parsed);
  }

  @ParameterizedTest(name = "delete \"{0}\" as {1}")
  @MethodSource("deletes")
  public void should_parse_delete(SlashCommand input, Object result) throws SlashCommandParser.InvalidSlashCommand {
    final Command<?> parsed = slashCommandParser.parse(input);
    Assertions.assertEquals(result, parsed);
  }

  @ParameterizedTest(name = "save \"{0}\" as {1}")
  @MethodSource("saves")
  public void should_parse_save(SlashCommand input, Object result) throws SlashCommandParser.InvalidSlashCommand {
    final Command<?> parsed = slashCommandParser.parse(input);
    Assertions.assertEquals(result, parsed);
  }

  @Test
  public void should_parse_select() throws SlashCommandParser.InvalidSlashCommand {
    final Command<?> parsed = slashCommandParser.parse(new SlashCommand(List.of("mr", "select"), List.of()));
    Assertions.assertEquals(new SelectSaved(null, null), parsed);
  }

  @ParameterizedTest(name = "annotate \"{0}\" as {1}")
  @MethodSource("annotates")
  public void should_parse_annotate(SlashCommand input, Object result) throws SlashCommandParser.InvalidSlashCommand {
    final Command<?> parsed = slashCommandParser.parse(input);
    Assertions.assertEquals(result, parsed);
  }

  @ParameterizedTest(name = "bad expression \"{0}\"")
  @MethodSource("badExpressions")
  public void should_not_parse_bad_expressions(SlashCommand input) {
    try {
      slashCommandParser.parse(input);
      Assertions.fail("Invalid command did not throw exception");
    } catch (SlashCommandParser.InvalidSlashCommand ex) {
      // success
    }
  }

  private record CommandTestCase(SlashCommand input, Command<?> output) {
    static CommandTestCase of(SlashCommand input, Command<?> output) {
      return new CommandTestCase(input, output);
    }
  }

  private static Stream<Arguments> calls() {
    return Stream.of(
        CommandTestCase.of(new SlashCommand(List.of("mr", "roll"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.DICE_POOL, "foo")
                                            )),
                           new Invoke("foo", new int[0])),
        CommandTestCase.of(new SlashCommand(List.of("mr", "roll"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.DICE_POOL, "foo 1337 13")
                                            )),
                           new Invoke("foo", new int[]{1337, 13}))
    ).map(testCase -> arguments(testCase.input(), testCase.output()));
  }

  private static Stream<Arguments> deletes() {
    return Stream.of(
        CommandTestCase.of(new SlashCommand(List.of("mr", "delete"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.ROLL_NAME, "foo"),
                                                new IntegerOption(IntegerOptionIdentifier.ARITY, 3L)
                                            )),
                           new Delete("foo", 3))
    ).map(testCase -> arguments(testCase.input(), testCase.output()));
  }

  private static Stream<Arguments> saves() {
    return Stream.of(
        CommandTestCase.of(new SlashCommand(List.of("mr", "save"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.SAVE_ASSIGNMENT,
                                                                 "(foo a b c) = 2d6")
                                            )),
                           new Save("foo", List.of("a", "b", "c"), "2d6")),
        CommandTestCase.of(new SlashCommand(List.of("mr", "save"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.SAVE_ASSIGNMENT,
                                                                 "foo a b c = 2d6")
                                            )),
                           new Save("foo", List.of("a", "b", "c"), "2d6")),
        CommandTestCase.of(new SlashCommand(List.of("mr", "save"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.SAVE_ASSIGNMENT,
                                                                 "foo a b c = {a}d{b} t{c} f1")
                                            )),
                           new Save("foo", List.of("a", "b", "c"), "{a}d{b} t{c} f1")),
        CommandTestCase.of(new SlashCommand(List.of("mr", "save"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.SAVE_ASSIGNMENT,
                                                                 "(foo a b c) = {a}d{b} + {c}")
                                            )),
                           new Save("foo", List.of("a", "b", "c"), "{a}d{b} + {c}")),
        CommandTestCase.of(new SlashCommand(List.of("mr", "save"),
                                            List.of(
                                                new StringOption(StringOptionIdentifier.SAVE_ASSIGNMENT,
                                                                 "foo a b c = {a}d{b} + {c}")
                                            )),
                           new Save("foo", List.of("a", "b", "c"), "{a}d{b} + {c}"))
    ).map(testCase -> arguments(testCase.input(), testCase.output()));
  }

  private static Stream<Arguments> annotates() {
    return Stream.of(
        CommandTestCase.of(new SlashCommand(
                               List.of("mr", "annotate"),
                               List.of(
                                   new StringOption(StringOptionIdentifier.ANNOTATION, "Some text"),
                                   new StringOption(StringOptionIdentifier.ROLL_NAME, "foo")
                               )),
                           new Annotate(
                               "foo",
                               null,
                               null,
                               "Some text"
                           )),
        CommandTestCase.of(new SlashCommand(
                               List.of("mr", "annotate"),
                               List.of(
                                   new StringOption(StringOptionIdentifier.ANNOTATION, "Some text"),
                                   new StringOption(StringOptionIdentifier.ROLL_NAME, "foo"),
                                   new IntegerOption(IntegerOptionIdentifier.ARITY, 0L)
                               )),
                           new Annotate(
                               "foo",
                               0,
                               null,
                               "Some text"
                           )),
        CommandTestCase.of(new SlashCommand(
                               List.of("mr", "annotate"),
                               List.of(
                                   new StringOption(StringOptionIdentifier.ANNOTATION, "Some text"),
                                   new StringOption(StringOptionIdentifier.ROLL_NAME, "foo"),
                                   new StringOption(StringOptionIdentifier.PARAMETER, "arg")
                               )),
                           new Annotate(
                               "foo",
                               null,
                               "arg",
                               "Some text"
                           )),
        CommandTestCase.of(new SlashCommand(
                               List.of("mr", "annotate"),
                               List.of(
                                   new StringOption(StringOptionIdentifier.ANNOTATION, "Some text"),
                                   new StringOption(StringOptionIdentifier.ROLL_NAME, "foo"),
                                   new IntegerOption(IntegerOptionIdentifier.ARITY, 1L),
                                   new StringOption(StringOptionIdentifier.PARAMETER, "arg")
                               )),
                           new Annotate(
                               "foo",
                               1,
                               "arg",
                               "Some text"
                           ))
    ).map(testCase -> arguments(testCase.input(), testCase.output()));
  }

  private static Stream<Arguments> badExpressions() {
    return Stream.of(
        "2d 6",
        "-2d6",
        "2d-6",
        "{n}d6",
        "2d6 e4 e4",
        "2d6 t4 t4",
        "2d6 e4 ie4",
        "2d6 r4 ir4"
    ).map(badDicePool -> arguments(new SlashCommand(
        List.of("mr", "roll"),
        List.of(new StringOption(StringOptionIdentifier.DICE_POOL, badDicePool)))));
  }

}
