package com.ezgrader.pdfgrader;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCombination;

// For viewing shortcuts within app
public class ShortcutDef {


    private SimpleObjectProperty<KeyCombination> keyCombo;
    private SimpleStringProperty action;

    public ShortcutDef(KeyCombination keyCombo, String action) {
        this.keyCombo = new SimpleObjectProperty<>(keyCombo);
        this.action = new SimpleStringProperty(action);
    }

    public String getKeyCombo() {
        return keyCombo.get().getName();
    }

    public String getAction() {
        return action.get();
    }

    public void setKeyCombo(KeyCombination keyCombo) {
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
