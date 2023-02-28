package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.model.Note;

public record OrExpr(Expression left, Expression right) implements Expression {
    @Override
    public boolean eval(Note n) {
        return left.eval(n) || right.eval(n);
    }
}
