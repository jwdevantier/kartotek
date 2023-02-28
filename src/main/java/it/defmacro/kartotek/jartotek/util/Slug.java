package it.defmacro.kartotek.jartotek.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class Slug {
    protected static Pattern p_syms = Pattern.compile("[^a-z0-9]+");
    protected static Pattern p_ddash = Pattern.compile("[-]{2,}");

    public static String slugify(String title) {
        // 1. normalize
        // 2. lower-case
        // 3. replace all non-alphanumeric characters with '-'
        // 4. replace all instances of 2 or more consecutive '-' by single '-'
        // 5. trim leading- and trailing '-' characters
        return Strings.trim(
                p_ddash.matcher(p_syms.matcher(
                        Normalizer.normalize(title, Normalizer.Form.NFKD).toLowerCase()
                ).replaceAll("-")).replaceAll("-"),
                '-'
        );
    }
}
