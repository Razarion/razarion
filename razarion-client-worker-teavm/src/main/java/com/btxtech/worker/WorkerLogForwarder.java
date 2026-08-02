package com.btxtech.worker;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import java.util.function.Consumer;

/**
 * Gives the worker a voice.
 * <p>
 * A dedicated worker has a console of its own. DevTools happens to display it, but it is a
 * different object than the page's console, so the Angular hook that forwards console output to
 * the server never sees a single worker line - every warning and every engine stack trace written
 * in here has been invisible to the server log so far.
 * <p>
 * The hook is installed on the console itself rather than on {@link com.btxtech.worker.jso.JsConsole},
 * because most of what matters is not written through JsConsole: the shared game engine logs with
 * java.util.logging, which TeaVM funnels into console.warn/console.error. Patching the console
 * catches both, plus anything a library prints.
 */
public final class WorkerLogForwarder {
    /**
     * The client throttles its own console-to-server forwarding to 20 lines per 10 seconds. The
     * same budget applies here, so a worker failing in a loop cannot eat the client's share - and
     * so the forwarding never becomes the load itself.
     */
    private static final int WINDOW_MS = 10_000;
    private static final int MAX_PER_WINDOW = 20;
    /** Stack traces are welcome, novels are not. */
    private static final int MAX_MESSAGE_LENGTH = 1500;

    private static Consumer<String> sink;
    private static long windowStart;
    private static int sentInWindow;

    private WorkerLogForwarder() {
    }

    public static void install(Consumer<String> sink) {
        WorkerLogForwarder.sink = sink;
        patchConsole(WorkerLogForwarder::onConsoleLine);
    }

    private static void onConsoleLine(String message) {
        if (sink == null || message == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - windowStart > WINDOW_MS) {
            windowStart = now;
            sentInWindow = 0;
        }
        if (sentInWindow >= MAX_PER_WINDOW) {
            return;
        }
        sentInWindow++;
        try {
            sink.accept(message.length() > MAX_MESSAGE_LENGTH
                    ? message.substring(0, MAX_MESSAGE_LENGTH) + " ..."
                    : message);
        } catch (Throwable ignore) {
            // Nowhere left to complain to - complaining would go through the console we are inside of.
        }
    }

    // The `busy` flag matters: the sink marshals and posts a message, and anything that logs on
    // that path would land back in here. The original console function is always called first, so
    // the worker's own console keeps working exactly as before.
    @JSBody(params = {"callback"}, script =
            "var busy = false;"
                    + "var wrap = function (original, level) {"
                    + "  return function () {"
                    + "    original.apply(console, arguments);"
                    + "    if (busy) { return; }"
                    + "    busy = true;"
                    + "    try {"
                    + "      callback(level + ' ' + Array.prototype.map.call(arguments, String).join(' '));"
                    + "    } catch (e) {"
                    + "    } finally {"
                    + "      busy = false;"
                    + "    }"
                    + "  };"
                    + "};"
                    + "console.warn = wrap(console.warn, 'WARN');"
                    + "console.error = wrap(console.error, 'ERROR');")
    private static native void patchConsole(LineCallback callback);

    @JSFunctor
    public interface LineCallback extends JSObject {
        void onLine(String message);
    }
}
