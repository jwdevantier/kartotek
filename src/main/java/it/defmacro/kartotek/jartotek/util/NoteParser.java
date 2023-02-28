package it.defmacro.kartotek.jartotek.util;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

// NOTE: fix for overlapping style ranges (error); when applying BOLD/EMPH/UNDERLINE, when finding opening tag, reset all others

public class NoteParser {
    // public static final Pattern RGX_HEADER = Pattern.compile("^(?<h>#+)(?<txt>.*)$", Pattern.MULTILINE);
    // public static final Pattern RGX_CODE_BLOCK = Pattern.compile("^```.*?^```", Pattern.MULTILINE | Pattern.DOTALL);

    public static final String R_BOLD = "\\*\\*";
    public static final String R_EMPH = "//";
    public static final String R_UNDERLINE = "__";

    /* mix in code inline to effectively capture and ignore toks inside the range */
    public static final String R_CODE_INLINE = "`(?<ICODE>.+?)`";

    // ex: [[phoronix|https://phoronix.com]]
    // ex [[www.google.com]]
    public static final String R_LINK = "\\[\\[(?:(?<LBL>[^\\]^|]+)\s*\\|\s*)?(?<HREF>[^\\]]+)\\]\\]";
    // TODO: add link rgx, also add as inline.

    public static final Pattern RGX_TOKS = Pattern.compile(
            "(?<B>" + R_BOLD +")"
            + "|(?<E>" + R_EMPH + ")"
            + "|(?<U>" + R_UNDERLINE + ")"
            + "|(" + R_CODE_INLINE + ")"
            + "|(" + R_LINK + ")"
    );

    public static List<StyleRange> tokenize(String input) {
        ArrayList<StyleRange> res = new ArrayList<>();
        int line_off = 0; /* offset of start of line within the entire string */
        int code_block_off = -1;
        for (String line: input.split(System.lineSeparator())) {
            if (code_block_off == -1) {
                /* normal parsing mode */
                if (line.startsWith("#")) { /* line is a header */
                    res.add(new StyleRange(StyleType.HEADER, line_off, line_off + line.length()));
                } else if (line.startsWith("```")) { /* line starts a code block */
                    code_block_off = line_off;
                } else { /* parsing toks */
                    Matcher m = RGX_TOKS.matcher(line);
                    int bold_start = -1;
                    int emph_start = -1;
                    int underline_start = -1;
                    while (m.find()) {
                        if (m.group("B") != null) {
                            if (bold_start != -1) {
                                res.add(new StyleRange(StyleType.BOLD, bold_start, line_off + m.end()));
                                bold_start = -1;
                            } else {
                                bold_start = m.start() + line_off;
                            }
                        } else if (m.group("E") != null) {
                            if (emph_start != -1) {
                                res.add(new StyleRange(StyleType.EMPH, emph_start, line_off + m.end()));
                                emph_start = -1;
                            } else {
                                emph_start = m.start() + line_off;
                            }
                        } else if (m.group("U") != null) {
                            if (underline_start != -1) {
                                res.add(new StyleRange(StyleType.UNDERLINE, underline_start, line_off + m.end()));
                                underline_start = -1;
                            } else {
                                underline_start = m.start() + line_off;
                            }
                        } else if (m.group("ICODE") != null) {
                            res.add(new StyleRange(StyleType.CODE_INLINE, line_off + m.start(), line_off + m.end()));
                        } else if (m.group("HREF") != null) {
                            res.add(new LinkStyleRange(m.group("HREF"), Optional.ofNullable(m.group("LBL")), line_off + m.start(), line_off + m.end()));
                        }
                    }
                }
            } else {
                if (line.startsWith("```")) {
                    res.add(new StyleRange(StyleType.CODE_BLOCK, code_block_off, line_off + line.length()));
                    code_block_off = -1;
                }
            }
            line_off += line.length() + 1;
        }
        return res.stream().sorted(Comparator.comparingInt(StyleRange::start)).toList();
    }

    public static StyleSpans<Collection<String>> styleRanges2StyleSpans(List<StyleRange> sranges, int txt_len) {
        StyleSpansBuilder<Collection<String>> sb = new StyleSpansBuilder<>();
        int off = 0;
        Stack<StyleRange> stack = new Stack<>();
        for (StyleRange sr : sranges) {
            if (stack.empty()) {
                sb.add(Collections.emptyList(), sr.start() - off);
                off = sr.start();
                stack.add(sr);
                continue;
            }
            StyleRange shead = stack.peek();
            if (shead.end() > sr.end()) {
                /* sr is nested, SR end chrs take space too, so no SRs' can end at same offset */
                // 1) render up to SR start (all styles BUT sr)
                List<String> _styles = stack.stream().map(StyleRange::type).toList();
                sb.add(_styles, sr.start() - off);
                off = sr.start();
                // push SR to queue, cannot know if there are nested SRs, so cannot render range yet
                stack.push(sr);
            } else {
                // SR not nested, -> SR is a sibling node

                // 1) render stack sibling node (have rendered up to SHEAD.start() already)
                List<String> _styles = stack.stream().map(StyleRange::type).toList();
                sb.add(_styles, shead.end() - off);
                off = shead.end();
                stack.pop(); //discard style-range, rendered its text

                // 2) render text up to SR.start()
                _styles = stack.stream().map(StyleRange::type).toList();
                sb.add(_styles, sr.start() - off);
                off = sr.start();

                // push SR to queue, cannot know if there's nested SRs, so cannot render range yet
                stack.push(sr);
            }
        }
        // done iterating, can now render text covered by remaining SRs (shead == most nested SR)
        while (!stack.isEmpty()) {
            StyleRange shead = stack.peek();
            List<String> _styles = stack.stream().map(StyleRange::type).toList();
            sb.add(_styles, shead.end() - off);
            off = shead.end();
            stack.pop();
        }
        // ensure trailing text (if any) gets an empty style span
        if (off < txt_len) {
            sb.add(Collections.emptyList(), txt_len - off);
        } else if (off == 0) {
            // empty notes can trigger this scenario. If sb has no spans
            // before create is called, an IllegalStateException is raised.
            sb.add(Collections.emptyList(), 0);
        }
        return sb.create();
    }
}

