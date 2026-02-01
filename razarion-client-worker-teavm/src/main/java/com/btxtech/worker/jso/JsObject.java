package com.btxtech.worker.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * TeaVM JSO interface for JavaScript Object (property map)
 */
public abstract class JsObject implements JSObject {

    @JSBody(script = "return {};")
    public static native JsObject create();

    @JSBody(params = {"key", "value"}, script = "this[key] = value;")
    public abstract void set(String key, JSObject value);

    @JSBody(params = {"key", "value"}, script = "this[key] = value;")
    public abstract void set(String key, String value);

    @JSBody(params = {"key", "value"}, script = "this[key] = value;")
    public abstract void set(String key, int value);

    @JSBody(params = {"key", "value"}, script = "this[key] = value;")
    public abstract void set(String key, double value);

    @JSBody(params = {"key", "value"}, script = "this[key] = value;")
    public abstract void set(String key, boolean value);

    @JSBody(params = {"key"}, script = "return this[key];")
    public abstract JSObject get(String key);

    @JSBody(params = {"key"}, script = "return this[key];")
    public abstract String getString(String key);

    @JSBody(params = {"key"}, script = "return this[key];")
    public abstract int getInt(String key);

    @JSBody(params = {"key"}, script = "return this[key];")
    public abstract double getDouble(String key);

    @JSBody(params = {"key"}, script = "return this[key];")
    public abstract boolean getBoolean(String key);

    @JSBody(params = {"key"}, script = "return this[key] === null || this[key] === undefined;")
    public abstract boolean isNullOrUndefined(String key);

    // Helper method to get nullable int - returns -1 if null/undefined
    // Caller must check with isNullOrUndefined first
    public Integer getNullableInt(String key) {
        if (isNullOrUndefined(key)) {
            return null;
        }
        return getInt(key);
    }

    // Helper method to set nullable int
    public void setNullableInt(String key, Integer value) {
        if (value == null) {
            setNull(key);
        } else {
            set(key, value.intValue());
        }
    }

    // Helper method to get nullable double
    public Double getNullableDouble(String key) {
        if (isNullOrUndefined(key)) {
            return null;
        }
        return getDouble(key);
    }

    // Helper method to get nullable boolean
    public Boolean getNullableBoolean(String key) {
        if (isNullOrUndefined(key)) {
            return null;
        }
        return getBoolean(key);
    }

    @JSBody(params = {"key"}, script = "this[key] = null;")
    public abstract void setNull(String key);
}
