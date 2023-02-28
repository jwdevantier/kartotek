package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.model.Note;

import java.util.List;

public record AllExpr(List<Expression> exprs) implements Expression {
    @Override
    public boolean eval(Note n) {
        for (Expression expr: exprs) {
            if (!expr.eval(n)) {
                return false;
            }
        }
        return true;
    }
}
