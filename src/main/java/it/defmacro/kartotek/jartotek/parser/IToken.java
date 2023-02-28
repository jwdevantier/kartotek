package it.defmacro.kartotek.jartotek.parser;

public interface IToken<T> {
    public T type();
    public String lexeme();
    @Override
    public String toString();
}
