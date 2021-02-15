package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Save implements Command {
  private final String identifier;
  private final List<String> parameters;
  private final String rhs;

  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    throw new UnsupportedOperationException();
  }
}
