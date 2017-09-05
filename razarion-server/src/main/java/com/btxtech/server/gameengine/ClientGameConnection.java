package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;

/**
 * Created by Beat
 * 20.04.2017.
 */
@ServerEndpoint(value = RestUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT, configurator = WebSocketEndpointConfigAware.class)
public class ClientGameConnection {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SessionService sessionService;
    @Inject
    private ClientGameConnectionService clientGameConnectionService;
    @Inject
    private CommandService commandService;
    private ObjectMapper mapper = new ObjectMapper();
    private EndpointConfig config;
    private RemoteEndpoint.Async async;
    private Date time;

    @OnMessage
    public void onMessage(Session session, String text) {
        try {
            GameConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, GameConnectionPacket.class);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = mapper.readValue(payload, packet.getTheClass());
            onPackageReceived(packet, param);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        time = new Date();
        this.config = config;
        async = session.getAsyncRemote();
        clientGameConnectionService.onOpen(this, getSession());
    }

    @OnError
    public void error(Session session, Throwable error) {
        System.out.println("**************   error: " + error);
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        clientGameConnectionService.onClose(this);
        async = null;
    }

    protected void onPackageReceived(GameConnectionPacket packet, Object param) {
        PlayerSession playerSession = getSession();
        switch (packet) {
            case CREATE_BASE:
                baseItemService.createHumanBaseWithBaseItem(playerSession.getUserContext().getLevelId(), playerSession.getUserContext().getHumanPlayerId(), playerSession.getUserContext().getName(), (DecimalPosition) param);
                break;
            case FACTORY_COMMAND:
            case UNLOAD_CONTAINER_COMMAND:
            case ATTACK_COMMAND:
            case BUILDER_COMMAND:
            case BUILDER_FINALIZE_COMMAND:
            case HARVESTER_COMMAND:
            case LOAD_CONTAINER_COMMAND:
            case MOVE_COMMAND:
            case PICK_BOX_COMMAND:
                commandService.executeCommand((BaseCommand) param);
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }

    public void sendToClient(String text) {
        async.sendText(text);
    }

    public PlayerSession getSession() {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(WebSocketEndpointConfigAware.HTTP_SESSION_KEY);
        return sessionService.getSession(httpSession.getId());
    }

    public Date getTime() {
        return time;
    }

    public int getDuration() {
        return (int) (System.currentTimeMillis() - time.getTime());
    }
}
