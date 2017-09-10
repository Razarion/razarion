package com.btxtech.client.editor.i18n;

import com.btxtech.shared.datatypes.I18nStringEditor;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Bindable
public class I18nStringEditorModel {
    private boolean dirty;
    private I18nStringEditor i18nStringEditor;

    public void fromI18nStringEditor(I18nStringEditor i18nStringEditor) {
        this.i18nStringEditor = i18nStringEditor;
    }

    public I18nStringEditor toI18nStringEditor() {
        return i18nStringEditor;
    }

    public int getId() {
        if (i18nStringEditor == null) {
            return -1;
        }
        return i18nStringEditor.getId();
    }

    public String getEnString() {
        if (i18nStringEditor == null) {
            return "???";
        }
        return i18nStringEditor.getEnString();
    }

    public void setEnString(String enString) {
        i18nStringEditor.setEnString(enString);
        dirty = true;
    }

    public String getDeString() {
        if (i18nStringEditor == null) {
            return "???";
        }
        return i18nStringEditor.getDeString();
    }

    public void setDeString(String deString) {
        i18nStringEditor.setDeString(deString);
        dirty = true;
    }

    public boolean checkDirty() {
        return dirty;
    }

    public boolean hasEnString() {
        return i18nStringEditor.getEnString() != null && !i18nStringEditor.getEnString().trim().isEmpty();
    }

    public boolean hasDeString() {
        return i18nStringEditor.getDeString() != null && !i18nStringEditor.getDeString().trim().isEmpty();
    }
}
