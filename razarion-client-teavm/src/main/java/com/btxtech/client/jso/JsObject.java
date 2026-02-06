package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

public abstract class JsObject implements JSObject {

    @JSBody(params = {"obj"}, script = "return obj;")
    public static native JsObject cast(JSObject obj);

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

    public Integer getNullableInt(String key) {
        if (isNullOrUndefined(key)) {
            return null;
        }
        return getInt(key);
    }

    public void setNullableInt(String key, Integer value) {
        if (value == null) {
            setNull(key);
        } else {
            set(key, value.intValue());
        }
    }

    public Double getNullableDouble(String key) {
        if (isNullOrUndefined(key)) {
            return null;
        }
        return getDouble(key);
    }

    public Boolean getNullableBoolean(String key) {
        if (isNullOrUndefined(key)) {
            return null;
        }
        return getBoolean(key);
    }

    @JSBody(params = {"key"}, script = "this[key] = null;")
    public abstract void setNull(String key);

    @JSBody(params = {"obj"}, script = "return Object.keys(obj);")
    public static native JsArray<JSObject> getKeys(JSObject obj);

    @JSBody(params = {"obj"}, script = "return typeof obj === 'string' ? obj : '' + obj;")
    public static native String jsToString(JSObject obj);

    @JSBody(script = "return this === null || this === undefined;")
    public abstract boolean isSelfNull();
}
