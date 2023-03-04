package it.defmacro.kartotek.jartotek.persistence;

import it.defmacro.kartotek.jartotek.model.Note;
import it.defmacro.kartotek.jartotek.util.FUtils;
import it.defmacro.kartotek.jartotek.util.Strings;
import it.defmacro.kartotek.jartotek.util.Timestamp;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class NoteStore {
    protected Path note_dir;
    protected boolean _loaded = false;

    protected ObservableList<Note> _notesList = FXCollections.observableArrayList();
    protected ObservableMap<String, Note> _notesMap = FXCollections.observableHashMap();

    public void initialize() {
        if (_loaded) {
            throw new RuntimeException("cannot initialize store twice");
        }

        Stream.of(Objects.requireNonNull(this.note_dir.toFile().listFiles()))
                .filter(file -> {
                    if (file.isDirectory()) {
                        return false;
                    } else if (! file.getName().endsWith(".meta.json")) {
                        return false; // content file will be checked separately
                    }
                    String fname = String.format("%s.md", Strings.rstrip(file.getName(), ".meta.json"));
                    Path contentPath = note_dir.resolve(fname);
                    if (!contentPath.toFile().exists() || !contentPath.toFile().isFile()) {
                        return false;
                    }
                    return true;
                })
                .map(file -> {
                    try {
                        return NoteMetaReader.read(file);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(note -> {
                    _notesList.add(note);
                });
        _loaded = true;
    }

    protected String noteMapKey(Note note) {
        // generate from the note the key used for indexing into _notesMap
        return Timestamp.serialize(note.ts.get());
    }

    protected boolean _saveNote(Note note, String contents) {
        System.out.println("new save");
        Path contentPath = note_dir.resolve(String.format("%s.md", note.getNoteKey()));
        Path metaPath = note_dir.resolve(String.format("%s.meta.json", note.getNoteKey()));
        // TODO: still not totally safe - could succeed in atomically overwriting the first
        //       file, but fail in the second case - we would end up with one new file.
        return FUtils.safeOverwrite(metaPath, _metaPath -> {
            NoteMetaReader.write(_metaPath.toFile(), note);
            FUtils.safeOverwrite(contentPath, _contentPath -> {
                Files.write(_contentPath, contents.getBytes());
                return true;
            });
            return true;
        });
    }

    public boolean saveNote(Note note, String contents) {
        Optional<String> diskKey = note.getDiskKey();
        boolean res = false;
        if (diskKey.isEmpty()) {
            res = _saveNote(note, contents);
            if (res) {
                note.updateDiskKey();
            }
            return res;
        }

        // save, but do NOT update diskKey yet, want to delete
        // old files first!
        res = _saveNote(note, contents);
        if (!res) {
            return false;
        }

        if (!note.getDiskKey().get().equals(note.getNoteKey())) {
            res = _deleteNote(note, false);
            note.updateDiskKey();
        }

        return res;
    }

    protected boolean _deleteNote(Note note, boolean unlink) {
        Optional<String> diskKey = note.getDiskKey();
        if (diskKey.isEmpty()) {
            return true;
        }
        Path contentPath = note_dir.resolve(String.format("%s.md", diskKey.get()));
        Path metaPath = note_dir.resolve(String.format("%s.meta.json", diskKey.get()));
        boolean ret = metaPath.toFile().delete() && contentPath.toFile().delete();
        if (ret && unlink) {
            _notesList.removeAll(note);
        }
        return ret;
    }

    public boolean deleteNote(Note note) {
        return _deleteNote(note, true);
    }

    public String nodeContents(Note note) throws IOException {
        Optional<String> diskKey = note.getDiskKey();
        if (diskKey.isEmpty()) {
            return "";
        }
        Path contentPath = note_dir.resolve(String.format("%s.md", diskKey.get()));
        if (!contentPath.toFile().exists()) {
            return "";
        }
        return Files.readString(contentPath);
    }

    public Note createNote() {
        // TODO: return note or smth with which to get its index in the list
        //       so that the selection gets changed to the new note
        Note note = new Note();
        note.ts.set(LocalDateTime.now());
        note.title.set("<untitled>");
        note.tags.set(FXCollections.observableArrayList());
        //notes.put(Timestamp.serialize(note.ts.get()), note);
        _notesList.add(note);
        return note;
    }

    public Optional<Note> getByKey(String key) {
        return Optional.ofNullable(_notesMap.get(key));
    }

    public ObservableList<Note> getNotesList() {
        return _notesList;
    }

    public NoteStore(Path note_dir) throws NoteDirException {
        this.note_dir = note_dir;
        if (note_dir == null) {
            throw new IllegalArgumentException("note_dir must be non-null");
        }
        else if (!note_dir.toFile().exists()) {
            throw new NoteDirException(note_dir, "note dir does not exist");
        } else if (!note_dir.toFile().isDirectory()) {
            throw new NoteDirException(note_dir, "path at note_dir is not a directory");
        }
        _notesList.addListener((ListChangeListener<Note>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Note note : change.getAddedSubList()) {
                        _notesMap.put(noteMapKey(note), note);
                    }
                } else if (change.wasRemoved()) {
                    for (Note note : change.getRemoved()) {
                        _notesMap.remove(noteMapKey(note));
                    }
                }
            }
        });
    }
}
