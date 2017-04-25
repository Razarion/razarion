package com.btxtech.webglemulator.razarion;

import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import javax.ws.rs.core.NewCookie;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

/**
 * Created by Beat
 * 25.04.2017.
 */
public interface DevToolConnectionDefault {
    default WebSocketClient init(String destUri, Object websocket) throws Exception {
        WebSocketClient client = new WebSocketClient();
        NewCookie newCookie = HttpConnectionEmu.getInstance().getSessionCookie();
        CookieStore cookieStore = new HttpCookieStore();
        cookieStore.add(new URI(destUri), new HttpCookie(newCookie.getName(), newCookie.getValue()));
        client.setCookieStore(cookieStore);
        client.start();
        client.connect(websocket, new URI(destUri), new ClientUpgradeRequest());
        return client;
    }
}
