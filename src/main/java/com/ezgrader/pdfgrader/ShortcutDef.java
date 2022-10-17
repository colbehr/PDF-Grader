package com.ezgrader.pdfgrader;

import javafx.beans.property.SimpleStringProperty;

// For viewing shortcuts within app
public class ShortcutDef {
    public String getKeyCombo() {
        return keyCombo.get();
    }

    public String getAction() {
        return action.get();
    }

    public void setKeyCombo(String keyCombo) {
        this.keyCombo.set(keyCombo);
    }

    public void setAction(String action) {
        this.action.set(action);
    }

    public SimpleStringProperty keyComboProperty() {
        return keyCombo;
    }

    public SimpleStringProperty actionProperty() {
        return action;
    }

    private SimpleStringProperty keyCombo;
    private SimpleStringProperty action;

    public ShortcutDef(String keyCombo, String action) {
        this.keyCombo = new SimpleStringProperty(keyCombo);
        this.action = new SimpleStringProperty(action);
    }
}
