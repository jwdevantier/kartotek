package it.defmacro.kartotek.jartotek.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parser<Expr, TokType> {
    protected Lexer<TokType> lexer;
    protected TokType tokenEOF;
    protected Map<TokType, PrefixParseFn<Expr, TokType>> prefixParsers;
    protected Map<TokType, InfixParser<Expr, TokType>> infixParsers;

    protected IToken<TokType> currentToken;
    protected IToken<TokType> peekToken;

    public final int PRECEDENCE_LOWEST = 1; // 0 is reserved for special cases.

    public IToken<TokType> getCurrentToken() {
        return currentToken;
    }

    public IToken<TokType> getPeekToken() {
        return peekToken;
    }

    public Parser(Lexer<TokType> lexer, TokType tokenEOF,
                  Map<TokType, PrefixParseFn<Expr, TokType>> prefixParsers,
                  Map<TokType, InfixParser<Expr, TokType>> infixParsers) {
        this.lexer = lexer;
        this.tokenEOF = tokenEOF;
        this.prefixParsers = prefixParsers;
        this.infixParsers = infixParsers;
        this.currentToken = this.lexer.next();
        this.peekToken = this.lexer.next();
    }

    public void nextToken() {
        this.currentToken = this.peekToken;
        this.peekToken = this.lexer.next();
    }

    public int precedenceOf(IToken<TokType> token) {
        InfixParser<Expr, TokType> ip = this.infixParsers.get(token.type());
        if (ip == null) {
            return PRECEDENCE_LOWEST;
        }
        return ip.getPrecedence();
    }

    public Expr parseExpr(int precedence) {
        if (this.currentToken.type() == tokenEOF) {
            throw new RuntimeException("parseExpr> no prefix parser for EOF");
        }
        PrefixParseFn<Expr, TokType> pfn = this.prefixParsers.get(this.currentToken.type());
        if (pfn == null) {
            throw new RuntimeException(String.format("No prefix parser for %s", this.currentToken.type().toString()));
        }
        Expr left = pfn.run(this);

        while (precedence < this.precedenceOf(this.peekToken)) {
            InfixParser<Expr, TokType> ip = this.infixParsers.get(this.getPeekToken().type());
            if (ip == null) {
                return left;
            }
            this.nextToken();
            left = ip.parse(this, left);
        }

        return left;
    }

    public Expr parseExpr() {
        return parseExpr(PRECEDENCE_LOWEST);
    }

    public List<Expr> parseProgram() {
        List<Expr> lst = new ArrayList<>();
        while (this.currentToken.type() != tokenEOF) {
            lst.add(this.parseExpr());
            this.nextToken();
        }
        return lst;
    }


}
