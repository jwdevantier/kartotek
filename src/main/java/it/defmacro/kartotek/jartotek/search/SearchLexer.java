package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.parser.Lexer;
import it.defmacro.kartotek.jartotek.parser.IToken;

import java.util.Set;

public class SearchLexer implements Lexer<Integer> {
    protected String _query;
    protected int _offset;
    protected Set<Character> whitespace = Set.of(
            ' ', '\t', '\r', '\n'
    );
    protected Set<Character> identSepChrs = Set.of(
            ' ', '\t', '\r', '\n', ':', ')');

    public SearchLexer(String query) {
        this._query = query;
        this._offset = 0;
    }

    @Override
    public boolean hasNext() {
        return _offset < _query.length();
    }

    public int lexUntil(int start, char ch) {
        int end = start;
        while ((end < _query.length()) && _query.charAt(end) != ch) {
            end++;
        }
        return end;
    }

    public int lexUntil(int start, Set<Character> chSet) {
        int end = start;
        while ((end < _query.length()) && !chSet.contains(_query.charAt(end))) {
            end++;
        }
        return end;
    }

    protected void skipWhitespace() {
        int off = _offset;
        while (off < _query.length() && whitespace.contains(_query.charAt(off))) {
            off++;
        }
        _offset = off;
    }

    @Override
    public Token next() {
        skipWhitespace();
        if (!hasNext()) {
            return new Token(TokType.EOF, "");
        }

        int start = _offset;

        char ch = _query.charAt(_offset);
        if (ch == '"') {
            int end = lexUntil(_offset + 1, '"');
            _offset = end + 1;
            return new Token(TokType.IDENT, _query.substring(start + 1, end));
        } else if (ch == '#') {
            _offset += 1;
            return new Token(TokType.HASH, "#");
        } else if (ch == '(') {
            _offset += 1;
            return new Token(TokType.LPAR, "(");
        } else if (ch == ')') {
            _offset += 1;
            return new Token(TokType.RPAR, ")");
        } else {
            int end = lexUntil(_offset, identSepChrs);
            _offset = end;
            String res = _query.substring(start, end);
            if (res.equalsIgnoreCase("and")) {
                return new Token(TokType.AND, "");
            } else if (res.equalsIgnoreCase("or")) {
                return new Token(TokType.OR, "");
            }
            return new Token(TokType.IDENT, _query.substring(start, end));
        }
    }
}
