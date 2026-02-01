package com.btxtech.shared.datatypes;


import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

/**
 * User: beat
 * Date: 13.01.13
 * Time: 13:14
 */
@JsType
@JsonPersistable
public class I18nString {
    private String string;

    public static String convert() {
        return "";
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public I18nString string(String string) {
        setString(string);
        return this;
    }
}
