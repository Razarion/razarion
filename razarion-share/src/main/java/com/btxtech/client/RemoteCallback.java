package com.btxtech.client;

public interface RemoteCallback<R> {
    void callback(R response);
}
