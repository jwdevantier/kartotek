package it.defmacro.kartotek.jartotek;

import it.defmacro.kartotek.jartotek.model.Note;
import it.defmacro.kartotek.jartotek.persistence.NoteStore;
import it.defmacro.kartotek.jartotek.ui.NoteLoadCallback;
import it.defmacro.kartotek.jartotek.util.Checksum;
import it.defmacro.kartotek.jartotek.util.LinkStyleRange;
import it.defmacro.kartotek.jartotek.util.NoteParser;
import it.defmacro.kartotek.jartotek.util.StyleRange;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.Subscription;

public class KartotekTabController {
    @FXML TextField txtTag = new TextField();
    @FXML FlowPane tags = new FlowPane();
    @FXML CodeArea editor = new CodeArea();

    protected NoteStore store;
    protected Note note = null;
    protected ExecutorService executor;
    protected Subscription highlight;
    protected String contentChecksum;
    protected List<LinkStyleRange> links;
    protected boolean isClickingLink = false;
    protected boolean isPinned = false;
    protected NoteLoadCallback _loadNote;

    public KartotekTabController(NoteStore store, Note note, NoteLoadCallback cb) {
        if (store == null) {
            throw new IllegalArgumentException("store cannot be null");
        }
        if (note == null) {
            throw new IllegalArgumentException("note cannot be null");
        }
        this.store = store;
        this.note = note;
        this._loadNote = cb;
        this.links = new ArrayList<>();
    }

    public boolean isPinned() {
        // TODO: consider re-enabling when done testing save/load
        //       For now, useful to exercise ability to save notes.
        //return isPinned || this.isDirty();
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public Note getNote() {
        return note;
    }

    protected void editorSetContent(String content) {
        editor.replaceText(content);
        links.clear();
        this.contentChecksum = Checksum.md5Sum(editor.getText());
    }

    public boolean isDirty() {
        String currentChecksum = calcTabChecksum();
        return ! currentChecksum.equals(this.contentChecksum);
    }

    protected void addTagButton(Note note, String tag) {
        FXMLLoader fxmlLoader = new FXMLLoader(KartotekApp.class.getResource("tagbtn.fxml"));
        try {
            Button btn = fxmlLoader.load();
            btn.setText(tag);
            btn.setOnAction(event -> {
                tags.getChildren().remove(btn);
                note.tags.get().remove(tag);
            });
            tags.getChildren().add(btn);
        } catch (IOException e) {
            throw new RuntimeException("failed to load tab FXML (UI definition)", e);
        }
    }

    protected void listenNoteChanges(boolean enable) {
        if (!enable && highlight != null) {
            highlight.unsubscribe();
            highlight = null;
            return;
        }
        highlight = editor.multiPlainChanges()
                .successionEnds(Duration.ofMillis(200))
                .retainLatestUntilLater(executor)
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(editor.multiPlainChanges())
                .filterMap(t -> {
                    if (t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        System.out.println("failed to compute highlighting");
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);
    }

    protected boolean openLink(String href){
        String href_lower = href.toLowerCase();
        if (href_lower.startsWith("http") || href_lower.startsWith("www")) {
            ProcessBuilder pb = new ProcessBuilder("open", href);
            try {
                pb.start();
                System.out.println("started");
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            return true;
        }
        Note n = store.getNotes().get(href);
        if (n != null) {
            if (_loadNote != null) {
                _loadNote.call(n);
            }
            return true;
        }
        return false;
    }

    public void initialize() {
        // add line numbers to editor
        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));
        //editor.setStyle("-fx-font-family: Menlo; -fx-font-size: 10pt;");
        //editor.setStyle("-fx-font-family: 'Lucida Grande'; -fx-font-size: 10pt;");
        // TODO: some fonts won't actually render bold etc, have to pick the magic font.
        editor.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 10pt");
        editor.setWrapText(true);
        executor = Executors.newSingleThreadExecutor(); /* TODO: call executor.stop() when closing tab */
        listenNoteChanges(true);

        editor.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                CharacterHit chit = editor.hit(mouseEvent.getX(), mouseEvent.getY());
                if (chit.getCharacterIndex().isPresent()) {
                    int chrNdx = chit.getCharacterIndex().getAsInt();
                    for (LinkStyleRange lsr : links) {
                        if (chrNdx >= lsr.start() && chrNdx <= lsr.end()) {
                            if (openLink(lsr.href())) {
                                mouseEvent.consume();
                                isClickingLink = true;
                            }
                            break;
                        }
                    }
                }
            }
        });

        editor.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            /* clicking a link may also register a mouse drag event which,
             * ordinarily, will create/alter a text selection.
             * If we are clicking a link we will clear the selection and
             * consume the drag event, stopping event propagation.
             */
            if (isClickingLink) {
                editor.selectRange(0, 0);
                e.consume();
            }
        });

        editor.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            // stop intercepting potential mouse drag events
            isClickingLink = false;
        });

        txtTag.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    String tag = txtTag.getText();
                    if (note.tags.get().contains(tag)) {
                        return;
                    }
                    note.tags.get().add(tag);
                    addTagButton(note, tag);
                    txtTag.setText("");
                }
            }
        });

        editorSetContent("Placeholder");
        loadNote(this.note);
    }

    protected String calcTabChecksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(note.title.get().getBytes());
            for (String tag : note.tags.get()) {
                md.update(tag.getBytes());
            }
            md.update(editor.getText().getBytes());
            return Checksum.toHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            // should really not happen, ever
            throw new RuntimeException("cannot find MD5 algorith in MessageDigest class.");
        }
    }

    protected void loadNote(Note note) {
        if (note == null) {
            return;
        }
        try {
            listenNoteChanges(false);
            String contents = store.nodeContents(note);
            editorSetContent(contents);
            this.note = note;
            tags.getChildren().clear();
            for (String tag : note.tags.get()) {
                addTagButton(note, tag);
            }
            this.contentChecksum = calcTabChecksum();
            ComputedStyle cs = computeHighlighting();
            applyHighlighting(cs);
        } catch (IOException e) {
            System.out.println("failed to load note");
            System.out.println(e);
        } finally {
            listenNoteChanges(true);
        }
    }

    public boolean saveNote() {
        if (!this.store.saveNote(this.note, editor.getText())) {
            return false;
        }
        this.contentChecksum = calcTabChecksum();
        return true;
    }

    public void reload(Note note) {
        if (note == null) {
            return;
        }
        if (isDirty()) {
            if (!this.saveNote()) {
                // TODO: log failure to save ..?
                return;  // abort loading new note.
            }
        }
        loadNote(note);
    }

    protected void applyHighlighting(ComputedStyle style) {
        links.clear();
        style.styleRanges.stream().filter(sr -> sr instanceof LinkStyleRange).forEach(sr -> {
            links.add((LinkStyleRange) sr);
        });
        editor.setStyleSpans(0, style.getStyleSpans());
    }

    protected ComputedStyle computeHighlighting() {
        String txt = editor.getText();
        List<StyleRange> sranges = NoteParser.tokenize(txt);
        StyleSpans<Collection<String>> sspans = NoteParser.styleRanges2StyleSpans(sranges, txt.length());
        return new ComputedStyle(sranges, sspans);
    }

    protected Task<ComputedStyle> computeHighlightingAsync() {
        Task<ComputedStyle> task = new Task<>() {
            @Override
            protected ComputedStyle call() throws Exception {
                return computeHighlighting();
            }
        };
        executor.execute(task);
        return task;
    }

    public void onClose() {
        listenNoteChanges(false);
        executor.shutdown();
    }
}

class ComputedStyle {
    protected List<StyleRange> styleRanges;
    protected StyleSpans<Collection<String>> styleSpans;

    public List<StyleRange> getStyleRanges() {
        return this.styleRanges;
    }

    public StyleSpans<Collection<String>> getStyleSpans() {
        return this.styleSpans;
    }

    public ComputedStyle(List<StyleRange> sranges, StyleSpans<Collection<String>> sspans) {
        this.styleRanges = sranges;
        this.styleSpans = sspans;
    }
}
