package com.btxtech.common.system;

import com.btxtech.common.GwtCommonUtils;
import com.btxtech.common.WebSocketHelper;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import elemental2.dom.EventListener;
import elemental2.dom.WebSocket;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 05.03.2018.
 */

public class WebSocketWrapper {
    private static final int MAX_RETRIES = 5;
    private static final int MAX_ESTABLISH_CONNECTION_TIMEOUT = 5000;
    private final Logger logger = Logger.getLogger(WebSocketWrapper.class.getName());

    private SimpleExecutorService simpleExecutorService;
    private int retries;
    private String url;
    private Runnable openCallback;
    private EventListener messageEventCallback;
    private Runnable serverRestartCallback;
    private Runnable connectionLostCallback;
    private InnerWebSocket innerWebSocket;

    @Inject
    public WebSocketWrapper(SimpleExecutorService simpleExecutorService) {
        this.simpleExecutorService = simpleExecutorService;
    }

    public void start(String url, Runnable openCallback, EventListener messageEventCallback, Runnable serverRestartCallback, Runnable connectionLostCallback) {
        this.url = url;
        this.openCallback = openCallback;
        this.messageEventCallback = messageEventCallback;
        this.serverRestartCallback = serverRestartCallback;
        this.connectionLostCallback = connectionLostCallback;
        createNewSocket();
    }

    public void send(String text) {
        innerWebSocket.send(text);
    }

    public void close() {
        innerWebSocket.close();
        innerWebSocket = null;
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

    private class InnerWebSocket {
        private WebSocket webSocket;
        private boolean open;
        private boolean timedOut;
        private SimpleScheduledFuture establishConnectionTimer;

        private void open() {
            establishConnectionTimer = simpleExecutorService.schedule(MAX_ESTABLISH_CONNECTION_TIMEOUT, () -> {
                logger.severe("WebSocketWrapper WebSocket establish connection timeout");
                establishConnectionTimer = null;
                if (open) {
                    return;
                }
                timedOut = true;
                open = false;
                try {
                    webSocket.close();
                } catch (Throwable t) {
                    logger.log(Level.WARNING, t.getMessage(), t);
                }
                webSocket = null;
                createNewSocket();
            }, SimpleExecutorService.Type.ESTABLISH_CONNECTION);
            webSocket = new WebSocket(WebSocketHelper.getUrl(url));
            webSocket.onerror = (evt -> {
                if (!open) {
                    return;
                }
                try {
                    logger.severe("WebSocketWrapper WebSocket OnError: " + GwtCommonUtils.jsonStringify(evt));
                } catch (Throwable t) {
                    logger.log(Level.WARNING, t.getMessage(), t);
                }
            });
            webSocket.onclose = (closeEvent -> {
                try {
                    if (timedOut || !open || webSocket == null) {
                        return;
                    }
                    open = false;
                    webSocket = null;
                    if (closeEvent.code == 1001) {
                        serverRestartCallback.run();
                    } else {
                        logger.severe("WebSocketWrapper WebSocket Close. Code: " + closeEvent.code + " Reason: " + closeEvent.reason + " WasClean: " + closeEvent.wasClean);
                        createNewSocket();
                    }
                } catch (Throwable t) {
                    logger.log(Level.WARNING, t.getMessage(), t);
                }
            });
            webSocket.onmessage = (evt -> messageEventCallback.handleEvent(evt));
            webSocket.onopen = (evt -> {
                establishConnectionTimer.cancel();
                establishConnectionTimer = null;
                if (timedOut) {
                    return;
                }
                retries = 0;
                open = true;
                openCallback.run();
            });
        }

        private void send(String text) {
            webSocket.send(text);
        }

        private void close() {
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
                logger.log(Level.WARNING, "WebSocketWrapper.close()" + t.getMessage(), t);
            }
        }

    }
}
