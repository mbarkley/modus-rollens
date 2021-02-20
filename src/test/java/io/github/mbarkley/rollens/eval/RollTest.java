package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.DbUtil;
import io.github.mbarkley.rollens.eval.Command.ExecutionContext;
import io.github.mbarkley.rollens.jda.TestGuild;
import io.github.mbarkley.rollens.jda.TestMember;
import io.github.mbarkley.rollens.jda.TestMessage;
import io.github.mbarkley.rollens.parse.Parser;
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
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RollTest {
  TestMessage testMessage;
  Jdbi jdbi;
  ExecutorService executorService;
  Parser parser;

  @BeforeEach
  public void setup() throws IOException {
    testMessage = new TestMessage("");
    final TestMember member = new TestMember();
    member.setNickname("Test User");
    testMessage.setMember(member);
    testMessage.setGuild(new TestGuild(123));

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
  public void should_parse_and_execute_roll_in_guild(Random rand, String input, String result) throws InterruptedException, ExecutionException, TimeoutException {
    final Optional<Command> parsed = parser.parse(input);
    Assertions.assertNotEquals(Optional.empty(), parsed);
    ExecutionContext ctx = new ExecutionContext(executorService, jdbi, parser, rand, testMessage);
    final CompletableFuture<String> executed = parsed.orElseThrow(() -> new AssertionError(""))
                                                     .execute(ctx);
    Assertions.assertTrue(executed.isDone(), "Returned future is not complete");
    final String observed = executed.get();
    Assertions.assertEquals(result, observed);
  }

  @MethodSource("rolls")
  @ParameterizedTest(name = "roll \"{1}\" should have result \"{2}\"")
  public void should_parse_and_execute_roll_in_direct_message(Random rand, String input, String result) throws ExecutionException, InterruptedException {
    testMessage.setGuild(null);
    final Optional<Command> parsed = parser.parse(input);
    Assertions.assertNotEquals(Optional.empty(), parsed);
    ExecutionContext ctx = new ExecutionContext(executorService, jdbi, parser, rand, testMessage);
    final CompletableFuture<String> executed = parsed.orElseThrow(() -> new AssertionError(""))
                                                     .execute(ctx);
    Assertions.assertTrue(executed.isDone(), "Returned future is not complete");
    final String observed = executed.get();
    Assertions.assertEquals(result, observed);
  }

  private static Stream<Arguments> rolls() {
    return Stream.of(
        // sum rolls
        arguments(new Random(1337),
                  "!mr 2d6",
                  """
                      Test User roll: `[2, 1]`
                      Result: 3"""),
        arguments(new Random(1337),
                  "!mr 1d10",
                  """
                      Test User roll: `[2]`
                      Result: 2"""),
        arguments(new Random(1337),
                  "!mr 2D6",
                  """
                      Test User roll: `[2, 1]`
                      Result: 3"""),
        arguments(new Random(1337),
                  "!mr d6",
                  """
                      Test User roll: `[2]`
                      Result: 2"""),
        arguments(new Random(1337),
                  "!mr D6",
                  """
                      Test User roll: `[2]`
                      Result: 2"""),
        arguments(new Random(1337),
                  "!mr 2d6 + 3d4",
                  """
                      Test User roll: `[2, 1, 3, 4, 4]`
                      Result: 14"""),
        // operators
        arguments(new Random(1337),
                  "!mr 2d6 + 2",
                  """
                      Test User roll: `[2, 1]`
                      Result: 5"""),
        arguments(new Random(1337),
                  "!mr 2d6 - 2",
                  """
                      Test User roll: `[2, 1]`
                      Result: 1"""),
        arguments(new Random(1337),
                  "!mr 2d6 * 2",
                  """
                      Test User roll: `[2, 1]`
                      Result: 6"""),
        arguments(new Random(1337),
                  "!mr 2d6 / 2",
                  """
                      Test User roll: `[2, 1]`
                      Result: 1"""),
        arguments(new Random(1337),
                  "!mr 2d6 * 2 + 1",
                  """
                      Test User roll: `[2, 1]`
                      Result: 7"""),
        // success counts
        arguments(new Random(1337),
                  "!mr 2d6 t6",
                  """
                      Test User roll: `[2, 1]`
                      Result: 0"""),
        arguments(new Random(1337),
                  "!mr 2d6 f1",
                  """
                      Test User roll: `[2, 1]`
                      Result: -1"""),
        arguments(new Random(1337),
                  "!mr 2d10 t7",
                  """
                      Test User roll: `[2, 5]`
                      Result: 0"""),
        arguments(new Random(1337),
                  "!mr 2d10 t7 f2",
                  """
                      Test User roll: `[2, 5]`
                      Result: -1"""),
        arguments(new Random(1337),
                  "!mr 2d10 f2 t7",
                  """
                      Test User roll: `[2, 5]`
                      Result: -1"""),
        arguments(new Random(1337),
                  "!mr 2d10 t7 e2 f1",
                  """
                      Test User roll: `[2, 5]` `[10, 3]`
                      Result: 1"""),
        // exploding sum
        arguments(new Random(1337),
                  "!mr 2d6 e2",
                  """
                      Test User roll: `[2, 1]` `[6]`
                      Result: 9"""),
        arguments(new Random(1337),
                  "!mr 2d6 ie2",
                  """
                      Test User roll: `[2, 1]` `[6, 5, 4, 5, 6, 5, 2, 4, 5, 2, 1]`
                      Result: 48"""),
        // exploding success count
        arguments(new Random(1337),
                  "!mr 2d10 t7 e5",
                  """
                      Test User roll: `[2, 5]` `[10]`
                      Result: 1"""),
        arguments(new Random(1337),
                  "!mr 2d10 t7 ie5",
                  """
                      Test User roll: `[2, 5]` `[10, 3]`
                      Result: 1"""),
        arguments(new Random(1337),
                  "!mr 2d10 t7 f2 e5",
                  """
                      Test User roll: `[2, 5]` `[10]`
                      Result: 0"""),
        arguments(new Random(1337),
                  "!mr 2d10 t7 f2 ie5",
                  """
                      Test User roll: `[2, 5]` `[10, 3]`
                      Result: 0""")
    );
  }
}