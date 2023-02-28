package it.defmacro.kartotek.jartotek.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Timestamp {
    protected static DateTimeFormatter dfmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss",Locale.US);
    public static LocalDateTime deserialize(String dstr) {
        return LocalDateTime.parse(dstr, dfmt);
    }

    public static String serialize(LocalDateTime dt) {
        return dfmt.format(dt);
    }
}
