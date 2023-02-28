package it.defmacro.kartotek.jartotek.util;

/**
 * Proposed Syntax
 * <p>
 * **BOLD**
 * //emph//
 * # h1
 * ## h2
 * [[lbl|url]]
 * `inline code`
 * ```
 * multiline
 * code
 * ```
 * <p>
 * Main inspiration:
 * https://github.com/FXMisc/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/JavaKeywordsAsyncDemo.java
 * ... and Kartotek, NoteEdit.py, rgx_*
 */

/*
 * problems.
 * Some tags (header, code-block) should be EXCLUSIVE, nothing else can effect
 *    the content they capture.
 *
 * Others, (bold/emph/underline) should TOGGLE.
 *
 * PROPOSED DESIGN
 * 1) Foreach line, check if RGX_HEADER or RGX_CODE_BLOCK applies
 *    If so, apply and ignore everything else
 *    (Code block to be refined, check for open tag, ignore lines until closing tag)
 * 2) OTHERWISE, use RGX_TOKS,
 *    collect text and start/end tags
 *    determine grouping
 *    apply style as appropriate
 */

public class StyleType {
    public static String BOLD = "editor_bold";
    public static String EMPH = "editor_emph";
    public static String UNDERLINE = "editor_underline";
    public static String CODE_INLINE = "editor_code_inline";
    public static String CODE_BLOCK = "editor_code_block";
    public static String HEADER = "editor_header";
    public static String LINK = "editor_link";
}
