package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.model.Note;

public record TagExpr(String val) implements Expression {
    @Override
    public boolean eval(Note n) {
        return n.tags.get().contains(val);
    }
}
