package it.defmacro.kartotek.jartotek.util;

import java.util.regex.Pattern;

public class Strings {
    public static String trim(String s, char c) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() > 0 && sb.charAt(0) == c) {
            sb.deleteCharAt(0);
        }
        int sub = 0;
        for (int i = sb.length() -1 ; i > -1; i--) {
            if (sb.charAt(i) == c) {
                sub += 1;
            } else {
                break;
            }
        }
        if (sub != 0) {
            sb.setLength(sb.length() - sub);
        }

        return sb.toString();
    }

    public static String rstrip(String s, String end) {
        if (s.isEmpty() || end.isEmpty() || !s.endsWith(end)) {
            return s;
        }
        return s.substring(0, s.length() - end.length());
    }
}
