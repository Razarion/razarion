package com.btxtech.client;

import javax.inject.Inject;

public class Event<T> {
    @Inject
    public Event() {
    }

    public void fire(Object... any) {

    }

    public Event<T> select(Object...any) {
        return this;
    }
}
