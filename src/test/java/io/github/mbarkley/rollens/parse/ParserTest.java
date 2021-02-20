package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.eval.*;
import io.github.mbarkley.rollens.jda.TestMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.mbarkley.rollens.eval.OperationMapper.Op.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ParserTest {
  Parser parser = new Parser();

  @ParameterizedTest(name = "call \"{0}\" as {1}")
  @MethodSource("calls")
  public void should_saved_rolls(String input, Object result) {
    final TestMessage message = new TestMessage(input);
    final Optional<Command> parsed = parser.parse(message.getContentRaw());
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @Test
  public void should_parse_help() {
    final TestMessage message = new TestMessage("!mr help");
    final Optional<Command> parsed = parser.parse(message.getContentRaw());
    Assertions.assertEquals(Optional.of(Help.INSTANCE), parsed);
  }

  @Test
  public void should_parse_list() {
    final TestMessage message = new TestMessage("!mr list");
    final Optional<Command> parsed = parser.parse(message.getContentRaw());
    Assertions.assertEquals(Optional.of(ListSaved.INSTANCE), parsed);
  }

  @ParameterizedTest(name = "delete \"{0}\" as {1}")
  @MethodSource("deletes")
  public void should_parse_delete(String input, Object result) {
    final TestMessage message = new TestMessage(input);
    final Optional<Command> parsed = parser.parse(message.getContentRaw());
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "save \"{0}\" as {1}")
  @MethodSource("saves")
  public void should_parse_save(String input, Object result) {
    final TestMessage message = new TestMessage(input);
    final Optional<Command> parsed = parser.parse(message.getContentRaw());
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "roll \"{0}\" as {1}")
  @MethodSource("rolls")
  public void should_parse_roll(String input, Object result) {
    final TestMessage message = new TestMessage(input);
    final Optional<Command> parsed = parser.parse(message.getContentRaw());
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "bad expression \"{0}\"")
  @MethodSource("badExpressions")
  public void should_not_parse_bad_expressions(String input) {
    final TestMessage message = new TestMessage(input);
    final Optional<Command> parsed = parser.parse(message.getContentRaw());
    Assertions.assertEquals(Optional.empty(), parsed);
  }

  private static Stream<Arguments> calls() {
    return Stream.of(
        arguments("!mr foo", new Invoke("foo", new int[0])),
        arguments("!mr foo 1337 13", new Invoke("foo", new int[] {1337, 13}))
    );
  }

  private static Stream<Arguments> deletes() {
    return Stream.of(
        arguments("!mr delete foo 3", new Delete("foo", 3))
    );
  }

  private static Stream<Arguments> saves() {
    return Stream.of(
        arguments("!mr save (foo a b c) = 2d6", new Save("foo", List.of("a", "b", "c"), "2d6")),
        arguments("!mr save (foo a b c) = {a}d{b} t{c} f1", new Save("foo", List.of("a", "b", "c"), "{a}d{b} t{c} f1")),
        arguments("!mr save (foo a b c) = {a}d{b} + {c}", new Save("foo", List.of("a", "b", "c"), "{a}d{b} + {c}"))
    );
  }

  private static Stream<Arguments> rolls() {
    return Stream.of(
        // sum rolls
        arguments("!mr 2d6", new RollCommand(new DicePool(new UniformDicePool(2, 6)), List.of(), new SumMapper())),
        arguments("!mr 1d10", new RollCommand(new DicePool(new UniformDicePool(1, 10)), List.of(), new SumMapper())),
        arguments("!mr 2D6", new RollCommand(new DicePool(new UniformDicePool(2, 6)), List.of(), new SumMapper())),
        arguments("!mr d6", new RollCommand(new DicePool(new UniformDicePool(1, 6)), List.of(), new SumMapper())),
        arguments("!mr D6", new RollCommand(new DicePool(new UniformDicePool(1, 6)), List.of(), new SumMapper())),
        arguments("!mr 2d6 + 3d4", new RollCommand(new DicePool(new UniformDicePool(2, 6), new UniformDicePool(3, 4)), List.of(), new SumMapper())),
        // operators
        arguments("!mr 2d6 + 2", new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(new SumMapper(), PLUS, 2)
                  )
        ),
        arguments("!mr 2d6 - 2", new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(new SumMapper(), MINUS, 2)
                  )
        ),
        arguments("!mr 2d6 * 2", new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(new SumMapper(), MULTIPLY, 2)
                  )
        ),
        arguments("!mr 2d6 / 2", new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(new SumMapper(), DIVIDE, 2)
                  )
        ),
        arguments("!mr 2d6 * 2 + 1", new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(new OperationMapper(new SumMapper(), MULTIPLY, 2), PLUS, 1)
                  )
        ),
        // success counts
        arguments("!mr 2d6 t6", new RollCommand(new DicePool(new UniformDicePool(2, 6)), List.of(), new SuccessCountMapper(6, 0))),
        arguments("!mr 2d6 f1", new RollCommand(new DicePool(new UniformDicePool(2, 6)), List.of(), new SuccessCountMapper(Integer.MAX_VALUE, 1))),
        arguments("!mr 2d10 t7", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List.of(), new SuccessCountMapper(7, 0))),
        arguments("!mr 2d10 t7 f2", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List.of(), new SuccessCountMapper(7, 2))),
        arguments("!mr 2d10 f2 t7", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List.of(), new SuccessCountMapper(7, 2))),
        arguments("!mr 2d10 t7 e10 f1", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List
            .of(new ExplodingModifier(10, 1)), new SuccessCountMapper(7, 1))),
        // exploding sum
        arguments("!mr 2d6 e6", new RollCommand(new DicePool(new UniformDicePool(2, 6)), List.of(new ExplodingModifier(6, 1)), new SumMapper())),
        arguments("!mr 2d6 ie6", new RollCommand(new DicePool(new UniformDicePool(2, 6)), List
            .of(new ExplodingModifier(6, Integer.MAX_VALUE)), new SumMapper())),
        // exploding success count
        arguments("!mr 2d10 t7 e10", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List
            .of(new ExplodingModifier(10, 1)), new SuccessCountMapper(7, 0))),
        arguments("!mr 2d10 t7 ie10", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List
            .of(new ExplodingModifier(10, Integer.MAX_VALUE)), new SuccessCountMapper(7, 0))),
        arguments("!mr 2d10 t7 f2 e10", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List
            .of(new ExplodingModifier(10, 1)), new SuccessCountMapper(7, 2))),
        arguments("!mr 2d10 t7 f2 ie10", new RollCommand(new DicePool(new UniformDicePool(2, 10)), List
            .of(new ExplodingModifier(10, Integer.MAX_VALUE)), new SuccessCountMapper(7, 2)))
    );
  }

  private static Stream<Arguments> badExpressions() {
    return Stream.of(
        arguments("!foo 2d6"),
        arguments("!mr 2 d6"),
        arguments("!mr 2d 6"),
        arguments("!mr -2d6"),
        arguments("!mr 2d-6"),
        arguments("! mr 2d6"),
        arguments("!mr notsave (foo a b c) = 2d6"),
        arguments("!mr {n}d6")
    );
  }

}
