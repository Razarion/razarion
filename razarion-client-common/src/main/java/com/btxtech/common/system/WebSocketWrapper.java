package com.btxtech.common.system;

import com.btxtech.common.GwtCommonUtils;
import com.btxtech.common.WebSocketHelper;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import elemental.client.Browser;
import elemental.events.CloseEvent;
import elemental.events.EventListener;
import elemental.html.WebSocket;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 05.03.2018.
 */
@Dependent
public class WebSocketWrapper {
    private static final int MAX_RETRIES = 5;
    private static final int MAX_ESTABLISH_CONNECTION_TIMEOUT = 5000;
    private Logger logger = Logger.getLogger(WebSocketWrapper.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private int retries;
    private String url;
    private Runnable openCallback;
    private EventListener messageEventCallback;
    private Runnable serverRestartCallback;
    private Runnable connectionLostCallback;
    private InnerWebSocket innerWebSocket;

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
                    exceptionHandler.handleException(t);
                }
                webSocket = null;
                createNewSocket();
            }, SimpleExecutorService.Type.ESTABLISH_CONNECTION);
            webSocket = Browser.getWindow().newWebSocket(WebSocketHelper.getUrl(url));
            webSocket.setOnerror(evt -> {
                if (!open) {
                    return;
                }
                try {
                    logger.severe("WebSocketWrapper WebSocket OnError: " + GwtCommonUtils.jsonStringify(evt));
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
            webSocket.setOnclose(evt -> {
                try {
                    if (timedOut || !open || webSocket == null) {
                        return;
                    }
                    open = false;
                    webSocket = null;
                    CloseEvent closeEvent = (CloseEvent) evt;
                    if (closeEvent.getCode() == 1001) {
                        serverRestartCallback.run();
                    } else {
                        logger.severe("WebSocketWrapper WebSocket Close. Code: " + closeEvent.getCode() + " Reason: " + closeEvent.getReason() + " WasClean: " + closeEvent.isWasClean());
                        createNewSocket();
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
            webSocket.setOnmessage(messageEventCallback);
            webSocket.setOnopen(evt -> {
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
            } catch (Throwable throwable) {
                exceptionHandler.handleException("WebSocketWrapper.close()", throwable);
            }
        }

    }
}
