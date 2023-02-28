package it.defmacro.kartotek.jartotek.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import it.defmacro.kartotek.jartotek.model.Note;
import it.defmacro.kartotek.jartotek.util.Timestamp;
import javafx.collections.FXCollections;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class NoteMetaReader {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new Jdk8Module());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
    }

    public static Note read(File f) throws IOException {
        String[] fparts = f.getName().split("_", 2);
        LocalDateTime ts = Timestamp.deserialize(fparts[0]);
        NoteMeta nm = mapper.readValue(f, NoteMeta.class);
        Note n = new Note();
        n.title.set(nm.getTitle());
        if (nm.getTags() != null) {
            List<String> tags = nm.getTags().stream().filter(Objects::nonNull).toList();
            n.tags.set(FXCollections.observableArrayList(tags));
        } else {
            n.tags.set(FXCollections.observableArrayList());
        }
        n.ts.set(ts);
        n.updateDiskKey();
        return n;
    }

    public static void write(File f, Note n) throws IOException {
        NoteMeta nm = new NoteMeta();
        nm.setTitle(n.title.get());
        nm.setTags(n.tags.get());
        mapper.writeValue(f, nm);
    }
}
