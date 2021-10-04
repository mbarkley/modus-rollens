package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Delete implements Command {
  private final String identifier;
  private final int arity;

  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      return doDelete(context);
    } else {
      return CompletableFuture.completedFuture("Cannot delete rolls in direct messages");
    }
  }

  @NotNull
  private CompletableFuture<String> doDelete(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
      try (Handle handle = context.jdbi().open()) {
        long guildId = context.commandEvent().getGuild().getIdLong();
        final SavedRollsDao dao = handle.attach(SavedRollsDao.class);
        final Optional<SavedRoll> found = dao.find(guildId, identifier, (byte) arity);
        if (found.isPresent()) {
          dao.delete(guildId, identifier, (byte) arity);

          return format("Deleted `%s`", found.get().toAssignmentString());
        } else {
          return format("No saved roll found for `%s %d`", identifier, arity);
        }
      }
    }, context.executorService());
  }
}
