package it.defmacro.kartotek.jartotek.persistence;

import it.defmacro.kartotek.jartotek.model.Note;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoteMetaTest {
    static File mkFile(String id, List<String> lines) throws IOException {
        File f = File.createTempFile(String.format("%s_", id), "meta.json");
        f.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(f)) {
            for(String line: lines) {
                pw.println(line);
            }
            pw.flush();
        }
        return f;
    }

    @Test
    void parseMetaWithTags() throws IOException {
        List<String> input = Arrays.asList(
                "{",
                "\"title\": \"hello, world\",",
                "\"tags\": [\"one\", \"two\", \"three\"]",
                "}"
        );
        String tsId = "20221023115004";

        File f = mkFile(tsId, input);
        Note n = NoteMetaReader.read(f);
        assertEquals("hello, world", n.title.get());
        assertEquals(Arrays.asList(
                "one", "two", "three"
        ), n.tags.get());
        assertEquals(tsId, n.getId());
    }

    @Test
    void parseMetaWithoutTags() throws IOException {
        List<String> input = Arrays.asList(
                "{",
                "\"title\": \"hello, world\"",
                "}"
        );
        String tsId = "20221023115004";

        File f = mkFile(tsId, input);
        Note n = NoteMetaReader.read(f);
        assertEquals("hello, world", n.title.get());
        assertEquals(Collections.emptyList(), n.tags.get());
        assertEquals(tsId, n.getId());
    }

    @Test
    void parseMetaWithUnknownKeys() throws IOException {
        List<String> input = Arrays.asList(
                "{",
                "\"title\": \"hello, world!\",",
                "\"tags\": [],",
                "\"unknown-key\": 3.14",
                "}"
        );
        String tsId = "20221023115004";

        File f = mkFile(tsId, input);
        Note n = NoteMetaReader.read(f);
        assertEquals("hello, world!", n.title.get());
        assertEquals(Collections.emptyList(), n.tags.get());
        assertEquals(tsId, n.getId());
    }
}