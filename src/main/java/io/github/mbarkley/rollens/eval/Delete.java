package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Delete implements Command<StringOutput> {
  private final String identifier;
  private final int arity;

  @Override
  public CompletableFuture<StringOutput> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      return doDelete(context);
    } else {
      return completedFuture(new StringOutput("Cannot delete rolls in direct messages"));
    }
  }

  @NotNull
  private CompletableFuture<StringOutput> doDelete(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
      try (Handle handle = context.jdbi().open()) {
        long guildId = context.commandEvent().getGuild().getIdLong();
        final SavedRollsDao dao = handle.attach(SavedRollsDao.class);
        // FIXME make atomic delete
        final Optional<SavedRoll> found = dao.find(guildId, identifier, (byte) arity);
        if (found.isPresent()) {
          dao.delete(guildId, identifier, (byte) arity);

          return new StringOutput(
              "Deleted `%s`".formatted(found.get().toAssignmentString())
          );
        } else {
          return new StringOutput(
              "No saved roll found for `%s %d`".formatted(identifier, arity)
          );
        }
      }
    }, context.executorService());
  }
}
