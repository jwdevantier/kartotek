package it.defmacro.kartotek.jartotek.search;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchLexerTest {
    List<Token> getTokens(SearchLexer sl) {
        List<Token> lst = new ArrayList<>();
        while (true) {
            Token tok = sl.next();
            if (tok.type() == TokType.EOF) {
                break;
            }
            lst.add(tok);
        }
        return lst;
    }

    List<Token> tokenize(String query) {
        return getTokens(new SearchLexer(query));
    }

    @Test
    void simpleIdent() {
        assertEquals(
                List.of(
                        new Token(TokType.IDENT, "hello")
                ),
                tokenize("hello")
        );
    }

    @Test
    void whitespaceSkipping() {
        assertEquals(Arrays.asList(
                new Token(TokType.IDENT, "hello"),
                new Token(TokType.IDENT, "world"),
                new Token(TokType.IDENT, "again")),
                tokenize("hello world again")
        );
    }

    @Test
    void singleParen() {
        assertEquals(Arrays.asList(
                        new Token(TokType.LPAR, "("),
                        new Token(TokType.IDENT, "x"),
                        new Token(TokType.AND, ""),
                        new Token(TokType.IDENT, "y"),
                        new Token(TokType.RPAR, ")")
                ),
                tokenize("(x and y)")
        );
    }

    @Test
    void hashTag() {
        assertEquals(Arrays.asList(
                        new Token(TokType.HASH, "#"),
                        new Token(TokType.IDENT, "foo")
                ),
                tokenize("#foo")
        );
    }
    @Test
    void quotedIdent() {
        assertEquals(Arrays.asList(
                        new Token(TokType.IDENT, "foo bar"),
                        new Token(TokType.IDENT, "baz")
                ),
                tokenize("\"foo bar\" baz")
        );
    }
    @Test
    void quotedHashIdent() {
        assertEquals(Arrays.asList(
                        new Token(TokType.HASH, "#"),
                        new Token(TokType.IDENT, "foo bar")
                ),
                tokenize("#\"foo bar\"")
        );
    }

    @Test
    void andOrConds() {
        assertEquals(Arrays.asList(
                new Token(TokType.IDENT, "a"),
                new Token(TokType.AND, ""),
                new Token(TokType.IDENT, "b"),
                new Token(TokType.OR, ""),
                new Token(TokType.IDENT, "c")
        ),
                tokenize("a and b or c"));
    }
}