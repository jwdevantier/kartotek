package it.defmacro.kartotek.jartotek.util;
import org.fxmisc.richtext.model.StyleSpans;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NoteParserTest {
    static void ppStyleRanges(String input, List<StyleRange> styles) {
        for (StyleRange sr : styles) {
            System.out.printf("%s [%d;%d[: %s\n", sr.type().toString(), sr.start(), sr.end(), input.substring(sr.start(), sr.end()));
        }
    }

    @Test
    void testTokenize() {
        String doc = String.join(System.lineSeparator(), Arrays.asList(
                "something **else** cool __way__ cool, __really",
                "# omg, **did** I just do this?",
                "```",
                "1 + 2",
                "else...",
                "```",
                "hole in **one**?",
                /* Note: markup inside an inline block is being ignored */
                "woohee `1 + 2 = **smth**` else"
        ));
        List<StyleRange> styles = NoteParser.tokenize(doc);
        ppStyleRanges(doc, styles);
        assertEquals(Arrays.asList(
                new StyleRange(StyleType.BOLD, 10, 18),
                new StyleRange(StyleType.UNDERLINE, 24, 31),
                new StyleRange(StyleType.HEADER, 47, 77),
                new StyleRange(StyleType.CODE_BLOCK, 78, 99),
                new StyleRange(StyleType.BOLD, 108, 115),
                new StyleRange(StyleType.CODE_INLINE, 124, 142)
                ),
                styles);
    }

    @Test
    void tokenizeAsStyleRanges() {
        String input = "hello __something **really //cool// and //awesome//**..__ lol";
        List<StyleRange> sranges = NoteParser.tokenize(input);
        StyleSpans<Collection<String>> sspans = NoteParser.styleRanges2StyleSpans(sranges, input.length());
        assertEquals(1, 2); // TODO: find way to check and enforce this...
    }
}
