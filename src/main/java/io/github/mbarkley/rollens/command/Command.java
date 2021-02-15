package io.github.mbarkley.rollens.command;

import io.github.mbarkley.rollens.format.Formatter;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface Command {

    /**
     * @return the text for a message to send in response
     */
    CompletableFuture<String> execute(Message message, Formatter formatter);
}
