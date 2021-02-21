package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.antlr.CommandLexer;
import io.github.mbarkley.rollens.antlr.CommandParser;
import io.github.mbarkley.rollens.antlr.CommandParserBaseVisitor;
import io.github.mbarkley.rollens.dice.*;
import io.github.mbarkley.rollens.eval.*;
import io.github.mbarkley.rollens.math.Operator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class Parser {
  private static final Pattern DICE = Pattern.compile("(\\d+)?[dD](\\d+)");

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

  public Optional<RollCommand> parseRoll(String input) {
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
        case 4 -> visitDelete(ctx.delete());
        case 5 -> visitHelp(ctx.help());
        case 6 -> visitInvocation(ctx.invocation());
        default -> throw new IllegalStateException("Unknown alt number " + ctx.getAltNumber());
      });
    }

    @Override
    public Command visitHelp(CommandParser.HelpContext ctx) {
      return Help.INSTANCE;
    }

    @Override
    public Command visitDelete(CommandParser.DeleteContext ctx) {
      return new Delete(ctx.IDENTIFIER(1).getText(), Integer.parseInt(ctx.NUMBER().getText()));
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
    public RollCommand visitRoll(CommandParser.RollContext ctx) {
      return new RollCommand(new RollExpressionVisitor().visitRollExpression(ctx.rollExpression()));
    }
  }

  private static class RollExpressionVisitor extends CommandParserBaseVisitor<RollExpression> {
    @Override
    public RollExpression visitRollExpression(CommandParser.RollExpressionContext ctx) {
      if (log.isTraceEnabled()) {
        log.trace("Visiting roll context: {}", ctx.getText());
      }
      return switch(ctx.getAltNumber()) {
        case 1 -> visitRollExpressionBasis(ctx.rollExpressionBasis());
        case 2 -> {
          try {
            final int num = Integer.parseInt(ctx.NUMBER().getText());
            final RollExpression rollExpression = visitRollExpression(ctx.rollExpression(0));
            yield new RepeatRollExpression(rollExpression, num);
          } catch (NumberFormatException nfe) {
            throw new ParseCancellationException(nfe);
          }
        }
        case 3 -> visitRollExpression(ctx.rollExpression(0)); // LB rollExpression RB
        case 4 -> visitRollExpressionWithConstantOp(ctx);
        case 5 -> visitComplexExpression(ctx);
        default -> throw new UnsupportedOperationException("" + ctx.getAltNumber() + ": " + ctx.getText());
      };
    }

    private ComplexRollExpression visitComplexExpression(CommandParser.RollExpressionContext ctx) {
      final Operator operator;
      if (ctx.PLUS() != null) {
        operator = Operator.PLUS;
      } else if (ctx.MINUS() != null) {
        operator = Operator.MINUS;
      } else if (ctx.TIMES() != null) {
        operator = Operator.MULTIPLY;
      } else if (ctx.DIVIDE() != null) {
        operator = Operator.DIVIDE;
      } else {
        throw new UnsupportedOperationException(ctx.getText());
      }
      return new ComplexRollExpression(operator, visitRollExpression(ctx.rollExpression(0)), visitRollExpression(ctx.rollExpression(1)));
    }

    private RollExpression visitRollExpressionWithConstantOp(CommandParser.RollExpressionContext ctx) {
      final Operator operator;
      if (ctx.PLUS() != null) {
        operator = Operator.PLUS;
      } else if (ctx.MINUS() != null) {
        operator = Operator.MINUS;
      } else if (ctx.TIMES() != null) {
        operator = Operator.MULTIPLY;
      } else if (ctx.DIVIDE() != null) {
        operator = Operator.DIVIDE;
      } else {
        throw new UnsupportedOperationException();
      }

      final int right = Integer.parseInt(ctx.NUMBER().getText());
      final RollExpression rollExpression = visitRollExpression(ctx.rollExpression(0));

      return new ComplexRollExpression(operator, rollExpression, new ConstantRollExpression(right));
    }

    public RollExpression visitRollExpressionBasis(CommandParser.RollExpressionBasisContext ctx) {
      final UniformDicePool[] uniformDicePools =
          ctx.DICE()
             .stream()
             .map(ParseTree::getText)
             .map(DICE::matcher)
             .map(matcher -> {
               if (matcher.matches()) {
                 final int numberOfDice = matcher.group(1) == null ?
                     1 :
                     Integer.parseInt(matcher.group(1));
                 final int numberOfSides = Integer.parseInt(matcher.group(2));
                 return new UniformDicePool(numberOfDice, numberOfSides);
               } else {
                 throw new ParseCancellationException();
               }
             })
             .toArray(UniformDicePool[]::new);
      final DicePool base = new DicePool(uniformDicePools);
      final ModifierVisitor.Modifiers modifiers = new ModifierVisitor().visitModifiers(ctx.modifiers());

      return new SimpleRollExpression(base, modifiers.rollModifiers, modifiers.resultAggregator);
    }
  }

  @RequiredArgsConstructor
  private static class ModifierVisitor extends CommandParserBaseVisitor<ModifierVisitor.Modifiers> {

    @Data
    private static class Modifiers {
      List<RollModifier> rollModifiers = new ArrayList<>(2);
      ResultAggregator resultAggregator = new SumAggregator();
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
            if (modifiers.resultAggregator instanceof SuccessCountAggregator) {
              modifiers.resultAggregator = ((SuccessCountAggregator) modifiers.resultAggregator)
                  .withSuccessThreshold(successThreshold);
            } else {
              modifiers.resultAggregator = new SuccessCountAggregator(successThreshold, 0);
            }
          }
          if (successCtx.FNUM() != null) {
            int failureThreshold = Integer.parseInt(successCtx.FNUM().getText().substring(1));
            if (modifiers.resultAggregator instanceof SuccessCountAggregator) {
              modifiers.resultAggregator = ((SuccessCountAggregator) modifiers.resultAggregator)
                  .withFailureThreshold(failureThreshold);
            } else {
              modifiers.resultAggregator = new SuccessCountAggregator(Integer.MAX_VALUE, failureThreshold);
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
