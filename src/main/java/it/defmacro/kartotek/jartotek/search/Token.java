package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.parser.IToken;

public record Token (int tokType, String lexeme)
        implements IToken<Integer> {
    @Override
    public Integer type() {
        return this.tokType;
    }
}
