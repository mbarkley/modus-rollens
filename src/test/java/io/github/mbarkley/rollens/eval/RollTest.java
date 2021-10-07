package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.DbUtil;
import io.github.mbarkley.rollens.eval.Command.CommandOutput;
import io.github.mbarkley.rollens.eval.Command.ExecutionContext;
import io.github.mbarkley.rollens.jda.TestCommandEvent;
import io.github.mbarkley.rollens.jda.TestGuild;
import io.github.mbarkley.rollens.jda.TestMember;
import io.github.mbarkley.rollens.parse.Parser;
import lombok.Value;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RollTest {
  TestCommandEvent testCommandEvent;
  Jdbi jdbi;
  ExecutorService executorService;
  Parser parser;

  @BeforeEach
  public void setup() throws IOException {
    testCommandEvent = new TestCommandEvent("");
    final TestMember member = new TestMember();
    member.setNickname("Test User");
    testCommandEvent.setMember(member);
    testCommandEvent.setGuild(new TestGuild(123));

    final File dbFile = File.createTempFile("modus-rollens-", ".db");
    dbFile.deleteOnExit();
    jdbi = DbUtil.initDb(dbFile.getAbsolutePath());
    executorService = Executors.newFixedThreadPool(10);

    parser = new Parser();
  }

  @AfterEach
  public void tearDown() {
    executorService.shutdownNow();
  }


  @ParameterizedTest(name = "roll \"{1}\" as {2}")
  @MethodSource("rolls")
  public void should_parse_and_execute_roll_in_guild(Random rand, String input, String result) throws InterruptedException, ExecutionException {
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertNotEquals(Optional.empty(), parsed);
    ExecutionContext ctx = new ExecutionContext(executorService, jdbi, parser, () -> rand, testCommandEvent);
    final CompletableFuture<? extends CommandOutput> executed = parsed.orElseThrow(() -> new AssertionError(""))
                                                                      .execute(ctx);
    Assertions.assertTrue(executed.isDone(), "Returned future is not complete");
    final CommandOutput observed = executed.get();
    switch (observed) {
      case Command.StringOutput s -> Assertions.assertEquals(result, s.value());
      default -> Assertions.fail("Unexpected output: " + observed);
    }
  }

  @MethodSource("rolls")
  @ParameterizedTest(name = "roll \"{1}\" should have result \"{2}\"")
  public void should_parse_and_execute_roll_in_direct_message(Random rand, String input, String result) throws ExecutionException, InterruptedException {
    testCommandEvent.setGuild(null);
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertNotEquals(Optional.empty(), parsed);
    ExecutionContext ctx = new ExecutionContext(executorService, jdbi, parser, () -> rand, testCommandEvent);
    final CompletableFuture<? extends CommandOutput> executed = parsed.orElseThrow(() -> new AssertionError(""))
                                                                      .execute(ctx);
    Assertions.assertTrue(executed.isDone(), "Returned future is not complete");
    final CommandOutput observed = executed.get();
    switch (observed) {
      case Command.StringOutput s -> Assertions.assertEquals(result, s.value());
      default -> Assertions.fail("Unexpected output: " + observed);
    }
  }

  @Value
  private static class RollTestCase {
    long seed;
    String roll;
    String result;

    static RollTestCase of(long seed, String roll, String result) {
      return new RollTestCase(seed, roll, result);
    }
  }

  private static Stream<Arguments> rolls() {
    return Stream.of(
        // sum rolls
        RollTestCase.of(1337, "2d6", """
            Test User roll: `[2, 1]`
            Result: 3"""),
        RollTestCase.of(1337, "1d10", """
            Test User roll: `[2]`
            Result: 2"""),
        RollTestCase.of(1337, "2D6", """
            Test User roll: `[2, 1]`
            Result: 3"""),
        RollTestCase.of(1337, "d6", """
            Test User roll: `[2]`
            Result: 2"""),
        RollTestCase.of(1337, "D6", """
            Test User roll: `[2]`
            Result: 2"""),
        RollTestCase.of(1337, "2d6 + 3d4", """
            Test User roll: `[2, 1][3, 4, 4]`
            Result: 14"""),
        // operators
        RollTestCase.of(1337, "2d6 + 2", """
            Test User roll: `[2, 1]`
            Result: 5"""),
        RollTestCase.of(1337, "2d6 - 2", """
            Test User roll: `[2, 1]`
            Result: 1"""),
        RollTestCase.of(1337, "2d6 * 2", """
            Test User roll: `[2, 1]`
            Result: 6"""),
        RollTestCase.of(1337, "2d6 / 2", """
            Test User roll: `[2, 1]`
            Result: 1"""),
        RollTestCase.of(1337, "2d6 * 2 + 1", """
            Test User roll: `[2, 1]`
            Result: 7"""),
        // success counts
        RollTestCase.of(1337, "2d6 t6", """
            Test User roll: `[2, 1]`
            Result: 0"""),
        RollTestCase.of(1337, "2d6 f1", """
            Test User roll: `[2, 1]`
            Result: -1"""),
        RollTestCase.of(1337, "2d10 t7", """
            Test User roll: `[2, 5]`
            Result: 0"""),
        RollTestCase.of(1337, "2d10 t7 f2", """
            Test User roll: `[2, 5]`
            Result: -1"""),
        RollTestCase.of(1337, "2d10 f2 t7", """
            Test User roll: `[2, 5]`
            Result: -1"""),
        RollTestCase.of(1337, "2d10 t7 e2 f1", """
            Test User roll: `[2, 5], [10, 3]`
            Result: 1"""),
        // exploding sum
        RollTestCase.of(1337, "2d6 e2", """
            Test User roll: `[2, 1], [6]`
            Result: 9"""),
        RollTestCase.of(1337, "d6 + d6 e2", """
            Test User roll: `[2][1], [6][]`
            Result: 9"""),
        RollTestCase.of(1337, "2d6 ie2", """
            Test User roll: `[2, 1], [6], [5], [4], [5], [6], [5], [2], [4], [5], [2], [1], []`
            Result: 48"""),
        RollTestCase.of(1337, "1d6 + 1d6 ie2", """
            Test User roll: `[2][1], [6][], [5], [4], [5], [6], [5], [2], [4], [5], [2], [1], []`
            Result: 48"""),
        // re-roll low dice
        RollTestCase.of(1337, "2d6 r1", """
            Test User roll: `[2, `~~`1`~~`], [6]`
            Result: 8"""),
        RollTestCase.of(1337, "d6 + d6 r1", """
            Test User roll: `[2][`~~`1`~~`], [6]`
            Result: 8"""),
        RollTestCase.of(1337, "d6 + d6 r2", """
            Test User roll: `[`~~`2`~~`][`~~`1`~~`], [6][5]`
            Result: 11"""),
        RollTestCase.of(1337, "5d10 k2 r1", """
            Test User roll: `[10, 10, `~~`2, 5, 3`~~`]`
            Result: 20"""),
        RollTestCase.of(1337, "d6 ir2", """
            Test User roll: `[`~~`2`~~`], [`~~`1`~~`], [6]`
            Result: 6"""),
        RollTestCase.of(1337, "d6 + d6 ir2", """
            Test User roll: `[`~~`2`~~`][`~~`1`~~`], [6][5]`
            Result: 11"""),
        RollTestCase.of(1337, "5d10 ir5", """
            Test User roll: `[10, 10, `~~`2, 5, 3`~~`], [9, `~~`4, 5`~~`], [8, 8]`
            Result: 45"""),
        // keep highest
        RollTestCase.of(1337, "2d6 k1", """
            Test User roll: `[2, `~~`1`~~`]`
            Result: 2"""),
        RollTestCase.of(1337, "2d6 k2", """
            Test User roll: `[2, 1]`
            Result: 3"""),
        RollTestCase.of(1337, "2d6 k3", """
            Test User roll: `[2, 1]`
            Result: 3"""),
        RollTestCase.of(1337, "2d6 k1 e1", """
            Test User roll: `[`~~`2, 1`~~`], [6, `~~`5`~~`]`
            Result: 6"""),
        RollTestCase.of(1337, "2d6 e1 k1", """
            Test User roll: `[`~~`2, 1`~~`], [6, `~~`5`~~`]`
            Result: 6"""),
        // keep lowest
        RollTestCase.of(1337, "2d6 kl1", """
            Test User roll: `[1, `~~`2`~~`]`
            Result: 1"""),
        RollTestCase.of(1337, "2d6 kl2", """
            Test User roll: `[2, 1]`
            Result: 3"""),
        RollTestCase.of(1337, "2d6 kl3", """
            Test User roll: `[2, 1]`
            Result: 3"""),
        RollTestCase.of(1337, "2d6 kl1 e1", """
            Test User roll: `[1, `~~`2`~~`], [`~~`6, 5`~~`]`
            Result: 1"""),
        RollTestCase.of(1337, "2d6 e1 kl1", """
            Test User roll: `[1, `~~`2`~~`], [`~~`6, 5`~~`]`
            Result: 1"""),
        // drop lowest
        RollTestCase.of(1337, "2d6 d1", """
            Test User roll: `[2, `~~`1`~~`]`
            Result: 2"""),
        RollTestCase.of(1337, "2d6 d0", """
            Test User roll: `[2, 1]`
            Result: 3"""),
        RollTestCase.of(1337, "2d6 d2", """
            Test User roll: `[`~~`2, 1`~~`]`
            Result: 0"""),
        RollTestCase.of(1337, "2d6 d3", """
            Test User roll: `[`~~`2, 1`~~`]`
            Result: 0"""),
        RollTestCase.of(1337, "2d6 d1 e1", """
            Test User roll: `[2, `~~`1`~~`], [6, 5]`
            Result: 13"""),
        RollTestCase.of(1337, "2d6 e1 d1", """
            Test User roll: `[2, `~~`1`~~`], [6, 5]`
            Result: 13"""),
        // exploding success count
        RollTestCase.of(1337, "2d10 t7 e5", """
            Test User roll: `[2, 5], [10]`
            Result: 1"""),
        RollTestCase.of(1337, "2d10 t7 ie5", """
            Test User roll: `[2, 5], [10], [3], []`
            Result: 1"""),
        RollTestCase.of(1337, "2d10 t7 f2 e5", """
            Test User roll: `[2, 5], [10]`
            Result: 0"""),
        RollTestCase.of(1337, "2d10 t7 f2 ie5", """
            Test User roll: `[2, 5], [10], [3], []`
            Result: 0"""),
        // nested expressions
        RollTestCase.of(1337, "(2d10 f2 t7)", """
            Test User roll: `[2, 5]`
            Result: -1"""),
        RollTestCase.of(1337, "(2d10 f2 t7) + 2", """
            Test User roll: `[2, 5]`
            Result: 1"""),
        RollTestCase.of(1337, "(2d10 f2 t7 + 2)", """
            Test User roll: `[2, 5]`
            Result: 1"""),
        RollTestCase.of(1337, "(2d10 f2 t7 + 2) - 2", """
            Test User roll: `[2, 5]`
            Result: -1"""),
        RollTestCase.of(1337, "(d10 - 1) * 10 + d10", """
            Test User roll: `[2][5]`
            Result: 15"""),
        RollTestCase.of(1337, "(d10 e2 - 1) * 10 + d10", """
            Test User roll: `[2][10], [5]`
            Result: 70"""),
        RollTestCase.of(1337, "d10 * d10", """
            Test User roll: `[2][5]`
            Result: 10"""),
        RollTestCase.of(1337, "d10 - d10", """
            Test User roll: `[2][5]`
            Result: -3"""),
        RollTestCase.of(1337, "(d10 + 10) / d10", """
            Test User roll: `[2][5]`
            Result: 2"""),
        RollTestCase.of(1337, "2 d10 e2", """
            Test User roll: `[2, 10], [5, 3]`
            Result: 20"""),
        RollTestCase.of(1337, "2 (d10 t5 f1)", """
            Test User roll: `[2, 5]`
            Result: 1"""),
        RollTestCase.of(1337, "2 (d10 t5 f1) + (d4 t3 f1)", """
            Test User roll: `[2, 5][3]`
            Result: 2"""),
        RollTestCase.of(1337, "2 (d10 t5 f1 + d4 t3 f1)", """
            Test User roll: `[2, 10][1, 4]`
            Result: 1"""),
        // precedence
        RollTestCase.of(1337, "d10 + d10 * d10", """
            Test User roll: `[2][5][10]`
            Result: 52"""),
        RollTestCase.of(1337, "d10 - d10 * d10", """
            Test User roll: `[2][5][10]`
            Result: -48"""),
        RollTestCase.of(1337, "d10 + d10 / d3", """
            Test User roll: `[2][5][3]`
            Result: 3"""),
        RollTestCase.of(1337, "d10 - d10 / d3", """
            Test User roll: `[2][5][3]`
            Result: 1"""),
        RollTestCase.of(1337, "d10 * d10 + d10", """
            Test User roll: `[2][5][10]`
            Result: 20"""),
        RollTestCase.of(1337, "d10 / d3 + d10", """
            Test User roll: `[2][1][10]`
            Result: 12"""),
        RollTestCase.of(1337, "d10 / d3 - d10", """
            Test User roll: `[2][1][10]`
            Result: -8"""),
        RollTestCase.of(1337, "d10 * d10 / d3", """
            Test User roll: `[2][5][3]`
            Result: 3"""),
        RollTestCase.of(1337, "(d10 + 10) / d10 * d10", """
            Test User roll: `[2][5][10]`
            Result: 20"""),
        RollTestCase.of(1337, "d10 * d10 / d10", """
            Test User roll: `[2][5][10]`
            Result: 1"""),
        RollTestCase.of(1337, "d10 * 2 + 1", """
            Test User roll: `[2]`
            Result: 5"""),
        RollTestCase.of(1337, "d10 + 2 * 3", """
            Test User roll: `[2]`
            Result: 8"""),
        RollTestCase.of(1337, "d10 * 2 + 1 + 3", """
            Test User roll: `[2]`
            Result: 8"""),
        RollTestCase.of(1337, "d10 * 2 + 1 * 3", """
            Test User roll: `[2]`
            Result: 7"""),
        RollTestCase.of(1337, "d10 + 2 * 3 * 3", """
            Test User roll: `[2]`
            Result: 20"""),
        RollTestCase.of(1337, "(d10 + 2) * 3", """
            Test User roll: `[2]`
            Result: 12"""),
        RollTestCase.of(1337, "d10 + (2 * 3)", """
            Test User roll: `[2]`
            Result: 8"""),
        RollTestCase.of(1337, "d10 * (2 + 3) / 2", """
            Test User roll: `[2]`
            Result: 5""")
    ).flatMap(testCase -> Stream.of(
        arguments(new Random(testCase.getSeed()), "!mr " + testCase.getRoll(), testCase.getResult()),
        arguments(new Random(testCase.getSeed()), "!mr roll " + testCase.getRoll(), testCase.getResult()),
        arguments(new Random(testCase.getSeed()), "/mr " + testCase.getRoll(), testCase.getResult()),
        arguments(new Random(testCase.getSeed()), "/mr roll " + testCase.getRoll(), testCase.getResult())
    ));
  }
}
