package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.antlr.CommandLexer;
import io.github.mbarkley.rollens.antlr.CommandParser;
import io.github.mbarkley.rollens.antlr.CommandParserBaseVisitor;
import io.github.mbarkley.rollens.eval.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class Parser {
  private static final Pattern ROLL = Pattern.compile("(\\d+)?[dD](\\d+)");

  public Optional<Command> parse(String input) {
    final CommandParser parser = initAntlrParser(input);

    try {
      final CommandParser.CommandContext commandContext = parser.command();
      final CommandParserVisitor visitor = new CommandParserVisitor(parser.getTokenStream());

      if (commandContext.START() != null && commandContext.START().getText().equals("!mr")) {
        if (log.isTraceEnabled()) log.trace("Attempting to visit command: {}", input);
        final Command result = visitor.visit(commandContext);
        if (log.isTraceEnabled()) log.trace("Visited command with result: {}", result);

        return Optional.ofNullable(result);
      } else {
        return Optional.empty();
      }
    } catch (ParseCancellationException ex) {
      return Optional.empty();
    }
  }

  public Optional<Roll> parseRoll(String input) {
    final CommandParser parser = initAntlrParser(input);

    final CommandParser.RollContext rollContext = parser.roll();
    final CommandParserVisitor visitor = new CommandParserVisitor(parser.getTokenStream());

    try {
      return Optional.ofNullable(visitor.visitRoll(rollContext));
    } catch (ParseCancellationException ex) {
      return Optional.empty();
    }
  }

  @NotNull
  private CommandParser initAntlrParser(String input) {
    final CommandLexer lexer = new CommandLexer(CharStreams.fromString(input));
    lexer.removeErrorListeners();
    final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    final CommandParser parser = new CommandParser(tokenStream);
    parser.removeErrorListeners();
    parser.setErrorHandler(new BailErrorStrategy());
    return parser;
  }

  @RequiredArgsConstructor
  private static class CommandParserVisitor extends CommandParserBaseVisitor<Command> {
    private final TokenStream tokenStream;

    @Override
    public Command visitCommand(CommandParser.CommandContext ctx) {
      return visitExpression(ctx.expression());
    }

    @Override
    public Command visitExpression(CommandParser.ExpressionContext ctx) {
      return Objects.requireNonNull(switch (ctx.getAltNumber()) {
        case 1 -> visitRoll(ctx.roll());
        case 2 -> visitSave(ctx.save());
        case 3 -> visitList(ctx.list());
        case 4 -> visitInvocation(ctx.invocation());
        default -> throw new IllegalStateException("Unknown alt number " + ctx.getAltNumber());
      });
    }

    @Override
    public Command visitInvocation(CommandParser.InvocationContext ctx) {
      final int[] arguments = ctx.NUMBER()
                                 .stream()
                                 .map(TerminalNode::getText)
                                 .mapToInt(Integer::parseInt)
                                 .toArray();
      return new Invoke(ctx.IDENTIFIER().getText(), arguments);
    }

    @Override
    public Command visitList(CommandParser.ListContext ctx) {
      return ListSaved.INSTANCE;
    }

    @Override
    public Command visitSave(CommandParser.SaveContext ctx) {
      final String identifier = ctx.IDENTIFIER(1).getText();
      final List<String> params = ctx.IDENTIFIER()
                                     .subList(2, ctx.IDENTIFIER().size())
                                     .stream()
                                     .map(TerminalNode::getText)
                                     .collect(Collectors.toList());
      final String rhs = tokenStream.getText(ctx.roll());

      return new Save(identifier, params, rhs);
    }

    @Override
    public Roll visitRoll(CommandParser.RollContext ctx) {
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
        throw new ParseCancellationException();
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
