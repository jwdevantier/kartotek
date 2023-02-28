package it.defmacro.kartotek.jartotek.parser;


public interface PrefixParseFn<Expr, TokType> {
    Expr run(Parser<Expr, TokType> parser);
}