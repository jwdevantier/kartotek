package it.defmacro.kartotek.jartotek.parser;

public abstract class InfixParser<Expr, TokType> {
    abstract  public int getPrecedence();
    abstract public Expr newInfixExpr(IToken<TokType> token, Expr left, Expr right);
    public Expr parse(Parser<Expr, TokType> parser, Expr left) {
        IToken<TokType> token = parser.getCurrentToken();
        int precedence = parser.precedenceOf(token);
        parser.nextToken();
        return newInfixExpr(token, left, parser.parseExpr(precedence));
    }
}
