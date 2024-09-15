package com.btxtech.client;

public interface Caller<T> {
    T call();

    T call(RemoteCallback<?> callback);

    T call(RemoteCallback<?> callback, ErrorCallback<?> errorCallback);

}
