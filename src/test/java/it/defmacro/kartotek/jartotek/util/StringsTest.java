package it.defmacro.kartotek.jartotek.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringsTest {

    @Test
    void trimWorks() {
        assertEquals("hello", Strings.trim("hello", '-'));
        assertEquals("hello", Strings.trim("hello-", '-'));
        assertEquals("hello", Strings.trim("-hello", '-'));
        assertEquals("hello", Strings.trim("--hello--", '-'));
        assertEquals("--hello", Strings.trim("--hello", '/'));
    }

    @Test
    void rstrip() {
        assertEquals("hello", Strings.rstrip("hello", "!!"));
        assertEquals("hello!!o", Strings.rstrip("hello!!o", "!!"));
        assertEquals("hello", Strings.rstrip("hello!!", "!!"));
    }

}