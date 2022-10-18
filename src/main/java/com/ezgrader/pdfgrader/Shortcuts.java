package com.ezgrader.pdfgrader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Shortcuts {

    private static final String USER_SHORTCUTS_FILE_PATH = "shortcuts.txt";
    private static Map<String, KeyCodeCombination> userShortcuts = ParseShortcutsFromFile();

    private static final Map<String, String> shortcutDescriptions = new HashMap<>(){{
       put("homeNew", "New");
       put("homeOpen", "Open");
       put("gradingSave", "Save");
       put("gradingNextTest", "Next Test");
       put("gradingPrevTest", "Previous Test");
    }};
    private static final Map<String, KeyCodeCombination> defaultShortcutKeys = new HashMap<>(){{
        put("homeNew", new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        put("homeOpen", new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        put("gradingSave", new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        put("gradingNextTest", new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN));
        put("gradingPrevTest", new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN));

    }};

    public static KeyCodeCombination get(String shortcutId) {
        if (userShortcuts.containsKey(shortcutId)) {
            return userShortcuts.get(shortcutId);
        } else if (defaultShortcutKeys.containsKey(shortcutId)) {
            return defaultShortcutKeys.get(shortcutId);
        } else {
            System.err.println("Tried to get non-existent shortcut \"" + shortcutId + "\".");
            return null;
        }
    }

    public static void ShowShortcutDialog(String[] shortcutIds, String title) {

        ObservableList<ShortcutDef> data = FXCollections.observableArrayList();
        for (String id : shortcutIds) {
            data.add(new ShortcutDef(id, get(id), shortcutDescriptions.get(id) != null ? shortcutDescriptions.get(id) : "Unknown"));
        }

        TableView<ShortcutDef> table = new TableView<>();
        TableColumn actionCol = new TableColumn("Action");
        actionCol.setCellValueFactory(
                new PropertyValueFactory<ShortcutDef, String>("action"));
        TableColumn keyComboCol = new TableColumn("Key Combo");
        keyComboCol.setCellValueFactory(
                new PropertyValueFactory<ShortcutDef, KeyCombination>("keyCombo"));
        table.setItems(data);
        table.getColumns().addAll(actionCol, keyComboCol);

        final Stage window = new Stage();
        window.setTitle(title);
        window.initModality(Modality.WINDOW_MODAL);
        window.initOwner(PDFGrader.getStage());
        VBox windowVbox = new VBox(50);
        windowVbox.getChildren().add(table);
        windowVbox.autosize();
        Scene dialogScene = new Scene(windowVbox);
        window.setScene(dialogScene);
        window.show();

        final Stage reassignDialog = new Stage();
        reassignDialog.initModality(Modality.APPLICATION_MODAL);
        reassignDialog.initOwner(window);
        VBox dialogVbox = new VBox(0);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.getChildren().add(new Text("Press new key. Esc to cancel."));
        dialogVbox.autosize();
        Scene reassignScene = new Scene(dialogVbox);
        reassignDialog.setScene(reassignScene);

        table.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                reassignDialog.setTitle("Set key for \"" + table.getSelectionModel().getSelectedItem().getAction() + "\"");
                reassignDialog.show();
            }
        });

        reassignScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ke -> {
            // Don't want the key event for the modifiers
            if (ke.getCode() == KeyCode.CONTROL || ke.getCode() == KeyCode.SHIFT || ke.getCode() == KeyCode.ALT) return;

            ArrayList<KeyCombination.Modifier> modifiers = new ArrayList<>();
            if (ke.isShiftDown()) modifiers.add(KeyCombination.SHIFT_DOWN);
            if (ke.isControlDown()) modifiers.add(KeyCombination.CONTROL_DOWN);
            if (ke.isAltDown()) modifiers.add(KeyCombination.ALT_DOWN);

            if (ke.getCode() == KeyCode.ESCAPE) {
                reassignDialog.close();
            } else {
                KeyCodeCombination newCombo;
                newCombo = new KeyCodeCombination(ke.getCode());
                if (modifiers.size() > 0) {
                    KeyCombination.Modifier[] modArr = modifiers.toArray(new KeyCombination.Modifier[0]);
                    newCombo = new KeyCodeCombination(ke.getCode(), modArr);
                }

                ShortcutDef def = table.getSelectionModel().getSelectedItem();
                def.setKeyCombo(newCombo); // update visually in table
                userShortcuts.put(def.id, newCombo); // update actual shortcut
                SaveUserShortcuts();
                reassignDialog.close();
            }
        });
    }

    private static HashMap<String, KeyCodeCombination> ParseShortcutsFromFile() {
        Scanner scanner;
        try {
            scanner = new Scanner(new File(USER_SHORTCUTS_FILE_PATH));
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        }
        HashMap<String, KeyCodeCombination> shortcuts = new HashMap<>();

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (!line.isBlank()) {
                String id = line.substring(0, line.indexOf('='));
                String[] keys = line.substring(line.indexOf('=') + 1).split("\\+");

                ArrayList<KeyCombination.Modifier> modifiers = new ArrayList<>();
                KeyCode mainKey = KeyCode.DEAD_TILDE; // placeholder keycode
                for (String key : keys) {
                    if ("shift".equalsIgnoreCase(key)) {
                        modifiers.add(KeyCombination.SHIFT_DOWN);
                    } else if ("ctrl".equalsIgnoreCase(key)) {
                        modifiers.add(KeyCombination.CONTROL_DOWN);
                    } else if ("alt".equalsIgnoreCase(key)) {
                        modifiers.add(KeyCombination.ALT_DOWN);
                    } else {
                        mainKey = KeyCode.getKeyCode(key);
                    }
                }
                KeyCodeCombination combo;
                combo = new KeyCodeCombination(mainKey);
                if (modifiers.size() > 0) {
                    KeyCombination.Modifier[] modArr = modifiers.toArray(new KeyCombination.Modifier[0]);
                    combo = new KeyCodeCombination(mainKey, modArr);
                }

                shortcuts.put(id, combo);
            }
        }
        return shortcuts;
    }

    public static void SaveUserShortcuts() {
        File file = new File(USER_SHORTCUTS_FILE_PATH);
        FileWriter writer;
        try {
            file.createNewFile();
            writer = new FileWriter(file, false);
            for (String sc : userShortcuts.keySet()) {
                writer.write(sc + "=" + userShortcuts.get(sc).getName() + "\n");
            }
            writer.close();
        } catch (IOException e) {

        }
    }
}
