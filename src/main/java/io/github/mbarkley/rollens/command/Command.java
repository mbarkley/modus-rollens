package io.github.mbarkley.rollens.command;

import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface Command {

    /**
     * @return the text for a message to send in response
     */
    CompletableFuture<String> execute(Message message);
}
