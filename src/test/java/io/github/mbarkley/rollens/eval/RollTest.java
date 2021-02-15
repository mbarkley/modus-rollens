package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.jda.TestMember;
import io.github.mbarkley.rollens.jda.TestMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RollTest {
  TestMessage testMessage;

  @BeforeAll
  public void setup() {
    testMessage = new TestMessage("");
    final TestMember member = new TestMember();
    member.setNickname("Test User");
    testMessage.setMember(member);
  }

  @MethodSource("correctResults")
  @ParameterizedTest(name = "Roll \"{1}\" should have result \"{2}\"")
  public void roll_with_fixed_seed_should_give_correct_result(Random rand, Roll roll, String result) throws ExecutionException, InterruptedException {
    final CompletableFuture<String> executed = roll.execute(rand, testMessage);
    Assertions.assertTrue(executed.isDone(), "Returned future is not complete");
    final String observed = executed.get();
    Assertions.assertEquals(result, observed);
  }

  private static Stream<Arguments> correctResults() {
    return Stream.of(
        // basic roll
        arguments(new Random(1337),
                  new Roll(
                      new BaseRoll(2, 6),
                      List.of(),
                      new SumMapper()
                  ),
                  """
                      Test User roll: `[2, 1]`
                      Result: 3"""
        ),
        // success count
        arguments(new Random(1337),
                  new Roll(
                      new BaseRoll(10, 10),
                      List.of(),
                      new SuccessCountMapper(6, 2)
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]`
                      Result: 4=5-1"""
        ),
        // exploding sums
        arguments(new Random(1337),
                  new Roll(
                      new BaseRoll(10, 10),
                      List.of(
                          new ExplodingModifier(10, 1)
                      ),
                      new SumMapper()
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]` `[7, 4]`
                      Result: 75"""
        ),
        arguments(new Random(1337),
                  new Roll(
                      new BaseRoll(10, 10),
                      List.of(
                          new ExplodingModifier(7, Integer.MAX_VALUE)
                      ),
                      new SumMapper()
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]` `[7, 4, 9, 1, 4, 1, 1]`
                      Result: 91"""
        ),
        arguments(new Random(1337),
                  new Roll(
                      new BaseRoll(10, 10),
                      List.of(
                          new ExplodingModifier(7, Integer.MAX_VALUE)
                      ),
                      new SuccessCountMapper(7, 1)
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]` `[7, 4, 9, 1, 4, 1, 1]`
                      Result: 4=7-3"""
        )
    );
  }
}
