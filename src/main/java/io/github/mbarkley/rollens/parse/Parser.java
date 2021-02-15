package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.antlr.CommandLexer;
import io.github.mbarkley.rollens.antlr.CommandParser;
import io.github.mbarkley.rollens.antlr.CommandParserBaseVisitor;
import io.github.mbarkley.rollens.eval.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Parser {
  private static final Pattern ROLL = Pattern.compile("(\\d+)?[dD](\\d+)");

  public Optional<Command> parse(Message message) {
    final CommandLexer lexer = new CommandLexer(CharStreams.fromString(message.getContentRaw()));
    lexer.removeErrorListeners();
    final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    final CommandParser parser = new CommandParser(tokenStream);
    parser.removeErrorListeners();
    parser.setErrorHandler(new BailErrorStrategy());

    try {
      final CommandParser.CommandContext commandContext = parser.command();
      final CommandParserVisitor visitor = new CommandParserVisitor();

      if (commandContext.START() != null && commandContext.START().getText().equals("!mr")) {
        if (log.isTraceEnabled()) log.trace("Attempting to visit command: {}", message.getContentRaw());
        final Command result = visitor.visit(commandContext);
        if (log.isTraceEnabled()) log.trace("Visited command with result: {}", result);

        return Optional.ofNullable(result);
      } else {
        log.debug("Non-matching start, skipping command for message.id={}", message.getId());
        return Optional.empty();
      }
    } catch (ParseCancellationException ex) {
      log.debug("Non-matching command for message.id={}, skipping", message.getId());
      return Optional.empty();
    }
  }

  @RequiredArgsConstructor
  private static class CommandParserVisitor extends CommandParserBaseVisitor<Command> {

    @Override
    public Command visitCommand(CommandParser.CommandContext ctx) {
      return visitExpression(ctx.expression());
    }

    @Override
    public Command visitExpression(CommandParser.ExpressionContext ctx) {
      return Objects.requireNonNull(switch (ctx.getRuleIndex()) {
        case 1 -> visitRoll(ctx.roll());
        default -> throw new IllegalStateException("Unknown rule index " + ctx.getRuleIndex());
      });
    }

    @Override
    public Command visitRoll(CommandParser.RollContext ctx) {
      if (log.isTraceEnabled()) {
        log.trace("Visiting roll context: {}", ctx.getText());
      }
      final Matcher matcher = ROLL.matcher(ctx.ROLL().getText());
      if (matcher.matches()) {
        final int numberOfDice = matcher.group(1) == null ?
            1 :
            Integer.parseInt(matcher.group(1));
        final int numberOfSides = Integer.parseInt(matcher.group(2));
        final BaseRoll base = new BaseRoll(numberOfDice, numberOfSides);
        final ModifierVisitor.Modifiers modifiers = new ModifierVisitor().visitModifiers(ctx.modifiers());
        return new Roll(base, modifiers.rollModifiers, modifiers.resultMapper);
      } else {
        throw new IllegalStateException("Shouldn't be possible to parse roll that doesn't match regular expression. Input: " + ctx
            .getText());
      }
    }
  }

  @RequiredArgsConstructor
  private static class ModifierVisitor extends CommandParserBaseVisitor<ModifierVisitor.Modifiers> {

    @Data
    private static class Modifiers {
      List<RollModifier> rollModifiers = new ArrayList<>(2);
      ResultMapper resultMapper = new SumMapper();
    }

    @Override
    public Modifiers visitModifiers(CommandParser.ModifiersContext ctx) {
      if (ctx == null) {
        return new Modifiers();
      } else {
        final Modifiers modifiers = visitModifiers(ctx.modifiers());
        if (ctx.successModifiers() != null) {
          final CommandParser.SuccessModifiersContext successCtx = ctx.successModifiers();
          if (successCtx.TNUM() != null) {
            int successThreshold = Integer.parseInt(successCtx.TNUM().getText().substring(1));
            if (modifiers.resultMapper instanceof SuccessCountMapper) {
              modifiers.resultMapper = ((SuccessCountMapper) modifiers.resultMapper)
                  .withSuccessThreshold(successThreshold);
            } else {
              modifiers.resultMapper = new SuccessCountMapper(successThreshold, 0);
            }
          }
          if (successCtx.FNUM() != null) {
            int failureThreshold = Integer.parseInt(successCtx.FNUM().getText().substring(1));
            if (modifiers.resultMapper instanceof SuccessCountMapper) {
              modifiers.resultMapper = ((SuccessCountMapper) modifiers.resultMapper)
                  .withFailureThreshold(failureThreshold);
            } else {
              modifiers.resultMapper = new SuccessCountMapper(Integer.MAX_VALUE, failureThreshold);
            }
          }
        }

        if (ctx.explosionModifiers() != null) {
          final CommandParser.ExplosionModifiersContext explosionModifiersCtx = ctx.explosionModifiers();
          final int explosionThreshold;
          final int maxIterations;
          if (explosionModifiersCtx.ENUM() != null) {
            explosionThreshold = Integer.parseInt(explosionModifiersCtx.ENUM().getText().substring(1));
            maxIterations = 1;
          } else {
            explosionThreshold = Integer.parseInt(explosionModifiersCtx.IENUM().getText().substring(2));
            maxIterations = Integer.MAX_VALUE;
          }
          modifiers.rollModifiers.add(new ExplodingModifier(explosionThreshold, maxIterations));
        }

        return modifiers;
      }
    }
  }
}
