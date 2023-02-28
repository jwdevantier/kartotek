package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.model.Note;

public record TitleExpr(String val) implements Expression {
    @Override
    public boolean eval(Note n) {
        return n.title.get().toLowerCase().contains(val);
    }
}
