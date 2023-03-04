package it.defmacro.kartotek.jartotek.settings;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Paths;

public class SettingsReader {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Path _confPath;

    static {
        mapper.registerModule(new Jdk8Module());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        _confPath = Paths.get(System.getProperty("user.home")).resolve(".jartotek.json");
    }

    public static Settings read() throws IOException, MissingConfException {
        if (Files.exists(_confPath)) {
            File f = _confPath.toFile();
            Settings s = mapper.readValue(f, Settings.class);
            return s;
        } else {
            throw new MissingConfException(_confPath);
        }
    }

    public static void write(Settings s) throws IOException {
        mapper.writeValue(_confPath.toFile(), s);
    }
}
