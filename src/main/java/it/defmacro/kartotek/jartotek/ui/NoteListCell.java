package it.defmacro.kartotek.jartotek.ui;

import it.defmacro.kartotek.jartotek.model.Note;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;

public class NoteListCell extends TextFieldListCell<Note> {
    protected NoteLoader loader;

    public NoteListCell(NoteLoader loader) {
        super();
        refreshConverter();
        this.loader = loader;
    }

    private void refreshConverter() {
        StringConverter<Note> converter = new StringConverter<>() {
            @Override
            public String toString(Note note) {
                return note.title.get();
            }

            @Override
            public Note fromString(String s) {
                Note note = getItem();
                note.title.set(s);
                return note;
            }
        };
        setConverter(converter);
    }

    @Override
    public void updateItem(Note note, boolean empty) {
        super.updateItem(note, empty);
        //refreshConverter();
        if (empty) {
            return;
        }

        setOnMouseClicked(ev -> {
            if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                if (note == null) {
                    return;
                }
                this.loader.load(note);
            }
        });
    }
}
