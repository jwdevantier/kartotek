package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.model.Note;

public interface Expression {
    boolean eval(Note n);
}
