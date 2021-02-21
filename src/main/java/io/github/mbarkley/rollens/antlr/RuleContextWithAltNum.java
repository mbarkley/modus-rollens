package io.github.mbarkley.rollens.antlr;

import org.antlr.v4.runtime.ParserRuleContext;

public class RuleContextWithAltNum extends org.antlr.v4.runtime.RuleContextWithAltNum {
  public RuleContextWithAltNum() {
  }

  public RuleContextWithAltNum(ParserRuleContext parent, int invokingStateNumber) {
    super(parent, invokingStateNumber);
  }

  @Override
  public void copyFrom(ParserRuleContext ctx) {
    super.copyFrom(ctx);
    setAltNumber(ctx.getAltNumber());
  }
}
