package io.github.mbarkley.rollens.command;

import java.util.concurrent.CompletableFuture;

public interface Command {

    /**
     * @return the text for a message to send in response
     */
    CompletableFuture<String> execute();
}
