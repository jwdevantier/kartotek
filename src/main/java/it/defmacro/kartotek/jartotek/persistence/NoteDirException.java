package it.defmacro.kartotek.jartotek.persistence;

import java.nio.file.Path;

public class NoteDirException extends Exception {
    public Path getNoteDir() {
        return noteDir;
    }

    protected Path noteDir;
    public NoteDirException(Path note_dir, String message) {
        super(message);
        this.noteDir = note_dir;
    }
}
