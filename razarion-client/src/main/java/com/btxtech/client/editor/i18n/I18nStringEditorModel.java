package com.btxtech.client.editor.i18n;

import com.btxtech.shared.datatypes.I18nStringEditor;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Bindable
public class I18nStringEditorModel {
    // private Logger logger = Logger.getLogger(I18nStringEditorModel.class.getName());
    private boolean dirty;
    private I18nStringEditor i18nStringEditor;
    private int id;
    private String enString;
    private String deString;


    public void fromI18nStringEditor(I18nStringEditor i18nStringEditor) {
        this.i18nStringEditor = i18nStringEditor;
        id = i18nStringEditor.getId();
        enString = i18nStringEditor.getEnString();
        deString = i18nStringEditor.getDeString();
    }

    public I18nStringEditor toI18nStringEditor() {
        i18nStringEditor.setDeString(deString);
        i18nStringEditor.setEnString(enString);
        return i18nStringEditor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getEnString() {
        return enString;
    }

    public void setEnString(String enString) {
        this.enString = enString;
        dirty = true;
    }

    public String getDeString() {
        return deString;
    }

    public void setDeString(String deString) {
        this.deString = deString;
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
