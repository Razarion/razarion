package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 10.09.2017.
 */
public class I18nStringEditor {
    private int id;
    private String enString;
    private String deString;

    public int getId() {
        return id;
    }

    public I18nStringEditor setId(int id) {
        this.id = id;
        return this;
    }

    public String getEnString() {
        return enString;
    }

    public I18nStringEditor setEnString(String enString) {
        this.enString = enString;
        return this;
    }

    public String getDeString() {
        return deString;
    }

    public I18nStringEditor setDeString(String deString) {
        this.deString = deString;
        return this;
    }
}
