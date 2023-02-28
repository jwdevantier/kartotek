package it.defmacro.kartotek.jartotek.ui;

import it.defmacro.kartotek.jartotek.model.Note;

@FunctionalInterface
public interface NoteLoadCallback {
    void call(Note n);
}
