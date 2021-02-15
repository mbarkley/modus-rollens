package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class ListSaved implements Command {
  public static ListSaved INSTANCE = new ListSaved();
  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    throw new UnsupportedOperationException();
  }
}
