package it.defmacro.kartotek.jartotek;

import it.defmacro.kartotek.jartotek.model.Note;
import it.defmacro.kartotek.jartotek.persistence.NoteDirException;
import it.defmacro.kartotek.jartotek.persistence.NoteStore;
import it.defmacro.kartotek.jartotek.search.AllExpr;
import it.defmacro.kartotek.jartotek.search.Expression;
import it.defmacro.kartotek.jartotek.search.SearchParser;
import it.defmacro.kartotek.jartotek.settings.MissingConfException;
import it.defmacro.kartotek.jartotek.settings.Settings;
import it.defmacro.kartotek.jartotek.settings.SettingsReader;
import it.defmacro.kartotek.jartotek.ui.NoteListCell;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.reactfx.EventStreams;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class KartotekController {
    protected Settings _settings = null;
    ObservableList<Note> lstNotesData;
    FilteredList<Note> lstNotesDataFilter;
    @FXML
    ListView<Note> lstNotes = new ListView<>();
    @FXML
    TextField txtSearch = new TextField();
    @FXML TabPane editorTabs = new TabPane();

    @FXML Label statusLeft = new Label();
    @FXML Label statusRight = new Label();


    Map<Tab, KartotekTabController> tab2Ctrl = new HashMap<>();
    protected NoteStore store;

    protected void mkNoteStore(Path note_dir) {
        try {
            store = new NoteStore(note_dir);
        } catch( NoteDirException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.setTitle("Fatal Error");
            alert.showAndWait();
            System.exit(17);
        }
    }

    protected Optional<Tab> getNoteTab(Note note) {
        for (Tab tab: editorTabs.getTabs()) {
            KartotekTabController ctrl = tab2Ctrl.get(tab);
            if (ctrl.note == note) {
                return Optional.of(tab);
            }
        }
        return Optional.empty();
    }

    protected void loadNote(Note note) {
        /*
         * load note, changing to an existing tab or spawning a new tab as necessary.
         *
         * Attempt vscode-like behavior.
         * * if the note is already loaded into a tab, switch to it
         * * if the note is already shown on the active tab, pin it
         * * if the note is not shown in any tabs, make the temporary tab (right-most) show it
         *  * if no temporary tab exists, create it.
         */
        Tab tmpTab = null;
        for (Tab t : editorTabs.getTabs()) {
            KartotekTabController ctrl = tab2Ctrl.get(t);
            if (ctrl.note == note) {
                editorTabs.getSelectionModel().select(t);
                ctrl.setPinned(true);
                return;
            }
            if (!ctrl.isPinned()) {
                tmpTab = t;
            }
        }
        if (tmpTab == null) {
            KartotekTabController tabCtrl = new KartotekTabController(store, note, this::loadNote, this::setStatus);
            tmpTab = initNewTab(tabCtrl);
            tab2Ctrl.put(tmpTab, tabCtrl);
            editorTabs.getTabs().add(tmpTab);
        } else {
            KartotekTabController tabCtrl = tab2Ctrl.get(tmpTab);
            tabCtrl.reload(note);
        }
        tmpTab.textProperty().bind(note.title);
        editorTabs.getSelectionModel().select(tmpTab);
    }

    protected void setStatus(String text, boolean error) {
        statusRight.setText(text);
        if (error) {
            statusRight.setTextFill(Color.rgb(255, 0 , 0));
        } else {
            statusRight.setTextFill(Color.rgb(0x9f, 0x9f, 0x9f));
        }
    }

    protected Tab initNewTab(KartotekTabController tabCtrl) {
        Tab tab = new Tab();
        FXMLLoader fxmlLoader = new FXMLLoader(KartotekApp.class.getResource("EditorPane.fxml"));
        fxmlLoader.setController(tabCtrl);
        try {
            tab.setContent(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException("failed to load tab FXML (UI definition)", e);
        }

        // before closing
        tab.setOnCloseRequest(event -> {
            KartotekTabController ctrl = tab2Ctrl.get(tab);
            if (ctrl.isDirty() && !ctrl.saveNote()) {
                event.consume(); // deny closing because we could not save the note
            }
            ctrl.onClose();
        });

        // after tab is closed
        tab.setOnClosed(event -> tab2Ctrl.remove(tab));
        return tab;
    }

    public void initialize() {
        try {
            _settings = SettingsReader.read();
        } catch (IOException e) {
            System.out.println("failed to read config");
            System.exit(1);

        } catch (MissingConfException e) {
            System.out.println(e);
            System.exit(1);
        }

        // Configure status left/right fields to auto-clear some seconds after being set
        EventStreams.valuesOf(statusLeft.textProperty())
                .filter(s -> !s.equals(""))
                .successionEnds(Duration.ofMillis(4000))
                .retainLatestUntilLater()
                .subscribe(s -> statusLeft.setText(""));
        EventStreams.valuesOf(statusRight.textProperty())
                .filter(s -> !s.equals(""))
                .successionEnds(Duration.ofMillis(4000))
                .retainLatestUntilLater()
                .subscribe(s -> statusRight.setText(""));

        lstNotes.setOnEditCommit(event -> {
            // https://openjfx.io/javadoc/19/javafx.controls/javafx/scene/control/ListView.html#edit(int)
            // (Editing section) - set this handler to prevent an attempt to override the item in the list.
            // This would trigger an error as a FilteredList does not support this.
            //
            // Secondly, we work to disable the edit selection (getEditingIndex() to read, edit() to set)
            // by setting the edit selection to -1 (unset) before disabling the ability to edit the list again.
            lstNotes.edit(-1);
            lstNotes.setEditable(false);
            // edit cancel retains focus, commit loses it, request focus to maintain it.
            lstNotes.requestFocus();
        });
        lstNotes.setOnEditCancel(noteEditEvent -> {
            lstNotes.edit(-1);
            lstNotes.setEditable(false);
        });

        lstNotes.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Note> call(ListView<Note> noteListView) {
                ListCell<Note> cell = new NoteListCell(note -> loadNote(note));

                ContextMenu ctxMenu = new ContextMenu();

                MenuItem deleteItem = new MenuItem();
                deleteItem.textProperty().set("Delete note");
                deleteItem.setOnAction(event -> {
                    store.deleteNote(cell.getItem());
                });

                MenuItem renameItem = new MenuItem();
                renameItem.textProperty().set("Rename note");
                renameItem.setOnAction(event -> {
                    // `lstNotes.setEditable(true)` means double-clicking an
                    // item triggers `cell.startEdit()`.
                    // This work-around only enables the ability to edit right
                    // as we are triggering the cell to be edited.
                    lstNotes.setEditable(true);
                    cell.startEdit();
                });

                MenuItem newItem = new MenuItem();
                newItem.textProperty().set("New note");
                newItem.setOnAction(event -> {
                    onNewNote();
                });

                ctxMenu.getItems().addAll(deleteItem);
                cell.emptyProperty().addListener((lst, wasEmpty, isEmpty) -> {
                    ContextMenu menu;
                    if (isEmpty) {
                        menu = new ContextMenu();
                        menu.getItems().addAll(newItem);
                    } else {
                        menu = new ContextMenu();
                        menu.getItems().addAll(newItem, renameItem, deleteItem);
                    }
                    cell.setContextMenu(menu);
                });
                return cell;
            }
        });
        lstNotes.addEventHandler(ListView.editCommitEvent(), new EventHandler<ListView.EditEvent<Object>>() {
            @Override
            public void handle(ListView.EditEvent<Object> objectEditEvent) {
                Note note = lstNotes.getItems().get(objectEditEvent.getIndex());
                // TODO: inspect what Note.getMetaPath() is for a new note
                Optional<Tab> tab = getNoteTab(note);
                if (tab.isEmpty()) {
                    System.err.println("tab not found!?");
                    return;
                }
                tab2Ctrl.get(tab.get()).saveNote();
                objectEditEvent.consume();
            }
        });
        mkNoteStore(_settings.getNotesDir());
        store.initialize();
        lstNotesData = store.getNotesList();
        lstNotesDataFilter = new FilteredList<>(lstNotesData);
        lstNotes.setItems(lstNotesDataFilter);

        lstNotes.getSelectionModel().selectedItemProperty().addListener((obs, then, now) -> {
            if (now == null) {
                return;
            }
            loadNote(now);
        });

        txtSearch.textProperty().addListener( obs -> {
            String query = txtSearch.getText();
            ObservableList<String> styles = txtSearch.getStyleClass();
            if (query == null || query.length() == 0) {
                lstNotesDataFilter.setPredicate(n -> true);
                if (styles.contains("search_field_error")) {
                    styles.removeAll(Collections.singleton("search_field_error"));
                }
            } else {
                try {
                    List<Expression> exprs = SearchParser.initParser(query).parseProgram();
                    lstNotesDataFilter.setPredicate((new AllExpr(exprs))::eval);
                    if (styles.contains("search_field_error")) {
                        styles.removeAll(Collections.singleton("search_field_error"));
                    }
                } catch (Exception e) {
                    if (query.trim().length() != 0) {
                        if (!styles.contains("search_field_error")) {
                            styles.add("search_field_error");
                        }
                    }
                }
            }
        });
    }

    public void onNewNote() {
        Note n = store.createNote();
        int ndx = lstNotesData.indexOf(n);
        if (ndx == -1) {
            throw new RuntimeException("note not found in list - should have been added");
        }
        lstNotes.getSelectionModel().select(ndx);
        lstNotes.scrollTo(ndx);
    }

    public void onTabClose() {
        Tab tab = editorTabs.getSelectionModel().getSelectedItem();
        if (tab == null) {
            return;
        }
        closeTab(tab);
    }

    public void onRename() {
        int ndx = lstNotes.getSelectionModel().getSelectedIndex();
        ndx = lstNotesDataFilter.getSourceIndex(ndx);
        if (ndx == -1) {
            return;
        }
        lstNotes.setEditable(true);
        lstNotes.edit(ndx);
    }

    public void onSearch() {
        txtSearch.requestFocus();
    }

    protected void closeTab(Tab tab) {
        Event closeRequestEvent = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, closeRequestEvent);

        tab.getTabPane().getTabs().remove(tab);

        Event closedEvent = new Event(tab, tab, Tab.CLOSED_EVENT);
        Event.fireEvent(tab, closedEvent);
    }

    public void quit() {
        // On application close, trigger the tab close AND the
        // pre- (close request) and post- (closed event) tab close events.
        //
        // this allows those handlers to fire, which should save any notes
        // whose contents have changed.
        Tab[] tabArray = editorTabs.getTabs().toArray(new Tab[0]);
        for (Tab tab : tabArray) {
            closeTab(tab);
        }
    }
}
