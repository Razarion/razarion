package com.btxtech.worker;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.worker.jso.JsCloseEvent;
import com.btxtech.worker.jso.JsConsole;
import com.btxtech.worker.jso.JsMessageEvent;
import com.btxtech.worker.jso.JsWebSocket;

import jakarta.inject.Inject;
import java.util.function.Consumer;

/**
 * TeaVM implementation of WebSocket wrapper
 * Uses TeaVM JSO interfaces instead of elemental2
 */
public class TeaVMWebSocketWrapper {
    private static final int MAX_RETRIES = 5;
    private static final int MAX_ESTABLISH_CONNECTION_TIMEOUT = 5000;

    private final SimpleExecutorService simpleExecutorService;
    private int retries;
    private String url;
    private Runnable openCallback;
    private Consumer<JsMessageEvent> messageEventCallback;
    private Runnable serverRestartCallback;
    private Runnable connectionLostCallback;
    private InnerWebSocket innerWebSocket;

    @Inject
    public TeaVMWebSocketWrapper(SimpleExecutorService simpleExecutorService) {
        this.simpleExecutorService = simpleExecutorService;
    }

    public void start(String url, Runnable openCallback, Consumer<JsMessageEvent> messageEventCallback,
                      Runnable serverRestartCallback, Runnable connectionLostCallback) {
        this.url = url;
        this.openCallback = openCallback;
        this.messageEventCallback = messageEventCallback;
        this.serverRestartCallback = serverRestartCallback;
        this.connectionLostCallback = connectionLostCallback;
        createNewSocket();
    }

    public void send(String text) {
        if (innerWebSocket != null) {
            innerWebSocket.send(text);
        }
    }

    public void close() {
        if (innerWebSocket != null) {
            innerWebSocket.close();
            innerWebSocket = null;
        }
    }

    private void createNewSocket() {
        retries++;
        if (retries > MAX_RETRIES) {
            connectionLostCallback.run();
            return;
        }
        innerWebSocket = new InnerWebSocket();
        innerWebSocket.open();
    }

    private String getWebSocketUrl(String url) {
        // Convert http/https URL to ws/wss
        if (url.startsWith("http://")) {
            return "ws://" + url.substring(7);
        } else if (url.startsWith("https://")) {
            return "wss://" + url.substring(8);
        } else if (url.startsWith("/")) {
            // Relative URL - construct full WebSocket URL
            // In worker context, we need to build the full URL
            return url;
        }
        return url;
    }

    private class InnerWebSocket {
        private JsWebSocket webSocket;
        private boolean open;
        private boolean timedOut;
        private SimpleScheduledFuture establishConnectionTimer;

        void open() {
            establishConnectionTimer = simpleExecutorService.schedule(MAX_ESTABLISH_CONNECTION_TIMEOUT, () -> {
                JsConsole.error("TeaVMWebSocketWrapper: WebSocket establish connection timeout");
                establishConnectionTimer = null;
                if (open) {
                    return;
                }
                timedOut = true;
                open = false;
                try {
                    if (webSocket != null) {
                        webSocket.close();
                    }
                } catch (Throwable t) {
                    JsConsole.warn("Error closing timed out WebSocket: " + t.getMessage());
                }
                webSocket = null;
                createNewSocket();
            }, SimpleExecutorService.Type.ESTABLISH_CONNECTION);

            webSocket = JsWebSocket.create(getWebSocketUrl(url));

            webSocket.setOnError(evt -> {
                if (!open) {
                    return;
                }
                JsConsole.error("TeaVMWebSocketWrapper: WebSocket OnError");
            });

            webSocket.setOnClose(evt -> {
                try {
                    JsCloseEvent closeEvent = (JsCloseEvent) evt;
                    if (timedOut || !open || webSocket == null) {
                        return;
                    }
                    open = false;
                    webSocket = null;
                    if (closeEvent.getCode() == 1001) {
                        serverRestartCallback.run();
                    } else {
                        JsConsole.error("TeaVMWebSocketWrapper: WebSocket Close. Code: " + closeEvent.getCode()
                                + " Reason: " + closeEvent.getReason()
                                + " WasClean: " + closeEvent.isWasClean());
                        createNewSocket();
                    }
                } catch (Throwable t) {
                    JsConsole.warn("Error handling close event: " + t.getMessage());
                }
            });

            webSocket.setOnMessage(evt -> {
                JsMessageEvent messageEvent = (JsMessageEvent) evt;
                messageEventCallback.accept(messageEvent);
            });

            webSocket.setOnOpen(evt -> {
                if (establishConnectionTimer != null) {
                    establishConnectionTimer.cancel();
                    establishConnectionTimer = null;
                }
                if (timedOut) {
                    return;
                }
                retries = 0;
                open = true;
                openCallback.run();
            });
        }

        void send(String text) {
            if (webSocket != null && open) {
                webSocket.send(text);
            }
        }

        void close() {
            try {
                if (webSocket == null) {
                    return;
                }
                if (!open) {
                    webSocket = null;
                    return;
                }
                open = false;
                webSocket.close();
                webSocket = null;
            } catch (Throwable t) {
                JsConsole.warn("TeaVMWebSocketWrapper.close(): " + t.getMessage());
            }
        }
    }
}
