package it.defmacro.kartotek.jartotek.search;

import it.defmacro.kartotek.jartotek.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchParserTest {
    @Test
    void parseSingleTitle() {
        Parser<Expression, Integer> p = SearchParser.initParser("hello");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new TitleExpr("hello")),
                exprs
        );
    }
    @Test
    void parseTwoTitles() {
        Parser<Expression, Integer> p = SearchParser.initParser("one two");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new TitleExpr("one"),
                        new TitleExpr("two")),
                exprs
        );
    }

    @Test
    void parseTag() {
        Parser<Expression, Integer> p = SearchParser.initParser("#something");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new TagExpr("something")),
                exprs
        );
    }
    @Test
    void parseTitleAndTag() {
        Parser<Expression, Integer> p = SearchParser.initParser("one #two");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new TitleExpr("one"),
                        new TagExpr("two")),
                exprs
        );
    }
    @Test
    void parseAnd() {
        Parser<Expression, Integer> p = SearchParser.initParser("one and #two");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new AndExpr(new TitleExpr("one"), new TagExpr("two"))),
                exprs
        );
    }
    @Test
    void parseOr() {
        Parser<Expression, Integer> p = SearchParser.initParser("one or #two");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new OrExpr(new TitleExpr("one"), new TagExpr("two"))),
                exprs
        );
    }
    @Test
    void parseCondsLeftAssoc() {
        // Test showing that and/or are left-associative, e.g.:
        // 'x or y and z' == '(x or y) and z'
        Parser<Expression, Integer> p = SearchParser.initParser("one or #two and three");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new AndExpr(new OrExpr(new TitleExpr("one"), new TagExpr("two")), new TitleExpr("three"))),
                exprs
        );
    }

    @Test
    void parseParens() {
        // 'x or (y and z)' overrides the normal left-associative ('(x or y) and z' order)
        Parser<Expression, Integer> p = SearchParser.initParser("one or (#two and three)");
        List<Expression> exprs = p.parseProgram();
        assertEquals(
                List.of(new OrExpr(
                        new TitleExpr("one"),
                        new AndExpr(new TagExpr("two"), new TitleExpr("three")))),
                exprs
        );
    }
}