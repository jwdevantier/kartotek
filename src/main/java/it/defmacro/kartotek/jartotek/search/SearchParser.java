package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.parser.*;

import java.util.Map;

class AndParser extends InfixParser<Expression, Integer> {
    @Override
    public int getPrecedence() {
        return 10;
    }

    @Override
    public Expression newInfixExpr(IToken<Integer> token, Expression left, Expression right) {
        return new AndExpr(left, right);
    }
}

class OrParser extends InfixParser<Expression, Integer> {
    @Override
    public int getPrecedence() {
        return 10;
    }

    @Override
    public Expression newInfixExpr(IToken<Integer> token, Expression left, Expression right) {
        return new OrExpr(left, right);
    }
}

public class SearchParser {
    public static Parser<Expression, Integer> initParser(String query) {
        Lexer<Integer> lexer = new SearchLexer(query);

        Map<Integer, PrefixParseFn<Expression, Integer>> pp = Map.of(
                TokType.IDENT, p -> new TitleExpr(p.getCurrentToken().lexeme()),
                TokType.HASH, p -> {
                    p.nextToken(); // discard HASH itself
                    IToken<Integer> tok = p.getCurrentToken();
                    if (tok.type() != TokType.IDENT) {
                        throw new RuntimeException("unexpected token");
                    }
                    return new TagExpr(tok.lexeme());
                },
                TokType.LPAR, p -> {
                    p.nextToken(); // discard LPAR itself
                    IToken<Integer> tok = p.getCurrentToken();
                    if (tok.type() == TokType.RPAR) {
                        throw new RuntimeException("unexpected empty paren");
                    }
                    Expression pexpr = p.parseExpr(p.PRECEDENCE_LOWEST);
                    p.nextToken();
                    if (p.getCurrentToken().type() != TokType.RPAR) {
                        throw new RuntimeException("expected RPAR");
                    }
                    return pexpr;
                }
        );

        Map<Integer, InfixParser<Expression, Integer>> ip = Map.of(
                TokType.AND,  new AndParser(),
                TokType.OR, new OrParser()
        );

        return new Parser<>(lexer, TokType.EOF, pp, ip);
    }
}
