package it.defmacro.kartotek.jartotek.parser;

import java.util.Iterator;

public interface Lexer<T> extends Iterator<IToken<T>> {
    @Override
    public boolean hasNext();
    @Override
    public IToken<T> next();
}
