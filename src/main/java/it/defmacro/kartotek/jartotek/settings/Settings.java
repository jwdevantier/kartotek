package it.defmacro.kartotek.jartotek.settings;

import java.nio.file.Path;

public class Settings {
    private Path _notesDir;

    public Path getNotesDir() {
        return _notesDir;
    }

    public void setNotesDir(Path notesDir) {
        _notesDir = notesDir;
    }
}
