package it.defmacro.kartotek.jartotek.model;

import it.defmacro.kartotek.jartotek.util.Slug;
import it.defmacro.kartotek.jartotek.util.Timestamp;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.Optional;

public class Note {
    public final ObjectProperty<LocalDateTime> ts = new SimpleObjectProperty<>();
    public final StringProperty title = new SimpleStringProperty();
    public final ListProperty<String> tags = new SimpleListProperty<>();

    /// noteKey, but only updated when note is saved and/or renamed
    protected Optional<String> diskKey;

    protected StringBinding noteKey = new StringBinding() {
        {
            super.bind(ts, title);
        }

        @Override
        protected String computeValue() {
            return String.format("%s_%s", Timestamp.serialize(ts.get()), Slug.slugify(title.get()));
        }
    };

    protected StringBinding id = new StringBinding() {
        @Override
        protected String computeValue() {
            return String.format(Timestamp.serialize(ts.get()));
        }
    };

    /**
     * Update note's disk key.
     *
     * This is called when the note has been saved back to disk to ensure that
     * the disk key value reflects the current value from getNoteKey().
     *
     */
    public void updateDiskKey() {
        this.diskKey = Optional.of(this.noteKey.get());
    }

    /**
     * Get the note's disk key.
     *
     * The disk key is the full name (`<ts><slug>`) of the note on disk.
     * This is set initially when a note is loaded from disk or whenever the
     * note is saved back to disk.
     *
     * Note:
     *  - if empty, the note is not yet saved to disk
     *  - if non-empty but different from getNoteKey(), then the note title
     *    has changed and the note is due for being renamed when saved back to disk.
     *
     * @return the Note's current disk key, if any. Otherwise, if the note
     * is not yet saved to disk, diskKey will be empty.
     */
    public Optional<String> getDiskKey() {
        return this.diskKey;
    }

    /**
     * Get the current note key.
     *
     * The note key is generated from a timestamp marking the note's initial
     * creation and a slug derived from the note title.
     * This implies that the note key changes whenever the note's title changes.
     *
     * @return the note's title.
     */
    public String getNoteKey() {
        return noteKey.get();
    }

    /**
     * Get the note ID.
     *
     * @return The fixed ID of the note, corresponding to the timestamp of the
     *         note's initial creation.
     */
    public String getId() {
        return id.get();
    }

    public Note() {
        this.diskKey = Optional.empty();
    }
}
