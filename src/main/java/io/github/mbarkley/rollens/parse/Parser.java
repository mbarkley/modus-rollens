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
import org.antlr.v4.runtime.*;
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

      if (commandContext.START() != null
            && (commandContext.START().getText().equals("!mr") || commandContext.START().getText().equals("/mr"))) {
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
        case 1 -> visitRepeatRollExpression(ctx);
        case 2 -> visitRollExpressionWithConstantOp(ctx);
        case 3 -> visitRollExpression(ctx.rollExpression(0)); // '(' rollExpression ')'
        case 4, 5 -> visitComplexExpression(ctx);
        case 6 -> visitRollExpressionBasis(ctx.simpleRoll());
        default -> throw new UnsupportedOperationException("" + ctx.getAltNumber() + ": " + ctx.getText());
      };
    }

    private RepeatRollExpression visitRepeatRollExpression(CommandParser.RollExpressionContext ctx) {
      final int num = parseNumeric(ctx.numeric());
      final RollExpression rollExpression = visitRollExpression(ctx.rollExpression(0));
      return new RepeatRollExpression(rollExpression, num);
    }

    private ComplexRollExpression visitComplexExpression(CommandParser.RollExpressionContext ctx) {
      return new ComplexRollExpression(operator(ctx.op), visitRollExpression(ctx.rollExpression(0)), visitRollExpression(ctx.rollExpression(1)));
    }

    private RollExpression visitRollExpressionWithConstantOp(CommandParser.RollExpressionContext ctx) {
      final RollExpression rollExpression = visitRollExpression(ctx.rollExpression(0));
      return visitUnaryConstantOp(rollExpression, ctx.unaryConstantOp());
    }

    @NotNull
    private RollExpression visitUnaryConstantOp(RollExpression rollExpression, CommandParser.UnaryConstantOpContext ctx) {
      final Operator operator = operator(ctx.op);
      return switch(ctx.getAltNumber()) {
        case 1 -> {
          final int constValue = parseNumeric(ctx.numeric());
          yield new ComplexRollExpression(operator, rollExpression, new ConstantRollExpression(constValue));
        }
        case 2 -> {
          final int constValue = new ConstExpressionVisitor().evaluateBinaryConstExpression(ctx.constExpression(0));
          yield new ComplexRollExpression(operator, rollExpression, new ConstantRollExpression(constValue));
        }
        case 3, 4 -> {
          final int rightValue = new ConstExpressionVisitor().evaluateConstExpression(ctx.constExpression(1));
          final RollExpression leftSubExpression =
              new ComplexRollExpression(operator, rollExpression, new ConstantRollExpression(rightValue));

          yield new ComplexRollExpression(operator(ctx.op2), leftSubExpression, new ConstantRollExpression(rightValue));
        }
        case 5 -> {
          final int constValue = new ConstExpressionVisitor().evaluateConstExpression(ctx.constExpression(0));
          yield new ComplexRollExpression(operator, rollExpression, new ConstantRollExpression(constValue));
        }
        default -> throw new UnsupportedOperationException(ctx.getText());
      };
    }

    public RollExpression visitRollExpressionBasis(CommandParser.SimpleRollContext ctx) {
      final UniformDicePool[] uniformDicePools =
          ctx.dice()
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

  private static class ConstExpressionVisitor {
    public int evaluateConstExpression(CommandParser.ConstExpressionContext ctx) {
      return switch(ctx.getAltNumber()) {
        case 1 -> parseNumeric(ctx.numeric());
        case 2 -> evaluateConstExpression(ctx.constExpression(0));
        case 3, 4 -> evaluateBinaryConstExpression(ctx);
        default -> throw new UnsupportedOperationException("" + ctx.getAltNumber() + ": " + ctx.getText());
      };
    }

    private int evaluateBinaryConstExpression(CommandParser.ConstExpressionContext ctx) {
      final int left = evaluateConstExpression(ctx.constExpression(0));
      final int right = evaluateConstExpression(ctx.constExpression(1));
      final Operator op = operator(ctx.op);

      return op.apply(left, right);
    }

  }

  private static int parseNumeric(RuleContext ctx) {
    final String text = ctx.getText();
    return parseNumeric(text);
  }

  private static int parseNumeric(String text) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException nfe) {
      throw new ParseCancellationException(nfe);
    }
  }

  @NotNull
  private static Operator operator(Token token) {
    return switch(token.getType()) {
      case CommandLexer.PLUS -> Operator.PLUS;
      case CommandLexer.MINUS -> Operator.MINUS;
      case CommandLexer.DIVIDE -> Operator.DIVIDE;
      case CommandLexer.TIMES -> Operator.MULTIPLY;
      default -> throw new UnsupportedOperationException(token.getText());
    };
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
        final Modifiers modifiers = new Modifiers();
        final List<CommandParser.ModifierContext> successCountModifiers =
            ctx.modifier()
               .stream()
               .filter(m -> m.TNUM() != null || m.FNUM() != null)
               .collect(Collectors.toList());
        if (!successCountModifiers.isEmpty()) {
          final var builder = SuccessCountAggregator.builder()
                                                    .successThreshold(Integer.MAX_VALUE)
                                                    .failureThreshold(0);
          boolean tnum = false, fnum = false;
          for (var modifier : successCountModifiers) {
            if (modifier.TNUM() != null) {
              if (tnum) throw new ParseCancellationException();
              builder.successThreshold(parseNumeric(modifier.TNUM().getText().substring(1)));
              tnum = true;
            } else if (modifier.FNUM() != null) {
              if (fnum) throw new ParseCancellationException();
              builder.failureThreshold(parseNumeric(modifier.FNUM().getText().substring(1)));
              fnum = true;
            } else {
              throw new UnsupportedOperationException("" + modifier.getAltNumber() + ": " + modifier.getText());
            }
          }

          modifiers.setResultAggregator(builder.build());
        }

        modifiers.setRollModifiers(
            ctx.modifier()
               .stream()
               .filter(m -> !(m.getAltNumber() == 1 || m.getAltNumber() == 2))
               .map(modifier -> switch (modifier.getAltNumber()) {
                 case 3 -> new ExplodingModifier(parseNumeric(modifier.ENUM().getText().substring(1)), 1);
                 case 4 -> new ExplodingModifier(parseNumeric(modifier.IENUM().getText().substring(2)), Integer.MAX_VALUE);
                 case 5 -> new KeepHighestModifier(parseNumeric(modifier.KNUM().getText().substring(1)));
                 case 6 -> new DropLowestModifier(parseNumeric(modifier.DNUM().getText().substring(1)));
                 case 7 -> new RerollModifier(parseNumeric(modifier.RNUM().getText().substring(1)), 1);
                 case 8 -> new RerollModifier(parseNumeric(modifier.IRNUM().getText().substring(2)), Integer.MAX_VALUE);
                 case 9 -> new KeepLowestModifier(parseNumeric(modifier.KLNUM().getText().substring(2)));
                 default -> throw new UnsupportedOperationException("" + modifier.getAltNumber() + ": " + modifier
                     .getText());
               })
               .sorted()
               .collect(Collectors.toList())
        );

        final long distinctModifiers = modifiers.getRollModifiers()
                                                .stream()
                                                .map(RollModifier::type)
                                                .distinct()
                                                .count();
        if (distinctModifiers != modifiers.getRollModifiers().size()) {
          throw new ParseCancellationException();
        }

        return modifiers;
      }
    }
  }
}
