package com.btxtech.shared.deprecated;

public interface ErrorCallback<T> {
    boolean error(T message, Throwable throwable);
}
