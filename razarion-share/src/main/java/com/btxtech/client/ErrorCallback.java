package com.btxtech.client;

public interface ErrorCallback<T> {
    boolean error(T message, Throwable throwable);
}
