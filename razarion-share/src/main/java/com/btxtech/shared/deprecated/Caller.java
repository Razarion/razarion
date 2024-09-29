package com.btxtech.shared.deprecated;

import javax.inject.Inject;

public class Caller<T> {

    @Inject
    public Caller() {
    }

    public T call() {
        throw new UnsupportedOperationException();
    }

    public T call(RemoteCallback<?> callback) {
        throw new UnsupportedOperationException();
    }

    public T call(RemoteCallback<?> callback, ErrorCallback<?> errorCallback) {
        throw new UnsupportedOperationException();
    }

}
