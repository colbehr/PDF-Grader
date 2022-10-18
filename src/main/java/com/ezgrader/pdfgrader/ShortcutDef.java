package com.ezgrader.pdfgrader;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

// For viewing shortcuts within app
public class ShortcutDef {


    private SimpleObjectProperty<KeyCodeCombination> keyCombo;
    private SimpleStringProperty action;
    public final String id;

    public ShortcutDef(String id, KeyCodeCombination keyCombo, String action) {
        this.id = id;
        this.keyCombo = new SimpleObjectProperty<>(keyCombo);
        this.action = new SimpleStringProperty(action);
    }

    public String getKeyCombo() {
        return keyCombo.get().getName();
    }

    public String getAction() {
        return action.get();
    }

    public void setKeyCombo(KeyCodeCombination keyCombo) {
        this.keyCombo.set(keyCombo);
    }

    public void setAction(String action) {
        this.action.set(action);
    }

    public SimpleObjectProperty keyComboProperty() {
        return keyCombo;
    }

    public SimpleStringProperty actionProperty() {
        return action;
    }
}
