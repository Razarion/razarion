package com.btxtech.server.service.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps what is handed to it until the test runs it - or runs it at once, for tests that are about
 * what happens, not when.
 */
class QueuedEngineEventExecutor extends EngineEventExecutor {
    private final boolean inline;
    private final List<Runnable> queued = new ArrayList<>();

    QueuedEngineEventExecutor(boolean inline) {
        this.inline = inline;
    }

    @Override
    public void execute(String what, Runnable runnable) {
        if (inline) {
            runnable.run();
        } else {
            queued.add(runnable);
        }
    }

    int queuedCount() {
        return queued.size();
    }

    void runQueued() {
        List<Runnable> toRun = new ArrayList<>(queued);
        queued.clear();
        toRun.forEach(Runnable::run);
    }
}
