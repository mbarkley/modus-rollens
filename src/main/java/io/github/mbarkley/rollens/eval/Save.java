package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Save implements Command {
  private final String identifier;
  private final List<String> parameters;
  private final String rhs;

  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      return doSave(context);
    } else {
      return CompletableFuture.completedFuture("Cannot save rolls in direct messages");
    }
  }

  @NotNull
  private CompletableFuture<String> doSave(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
      try (Handle handle = context.jdbi().open()) {
        long guildId = context.commandEvent().getGuild().getIdLong();
        SavedRoll savedRoll = new SavedRoll(guildId, identifier, parameters, rhs);
        handle.attach(SavedRollsDao.class).insertOrReplace(savedRoll);

        return format("Saved (%s)", Stream.concat(Stream.of(identifier), parameters.stream())
                                          .collect(Collectors.joining(" ")));
      }
    }, context.executorService());
  }
}
