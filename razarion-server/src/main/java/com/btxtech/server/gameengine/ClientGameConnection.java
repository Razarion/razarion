package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.04.2017.
 */
@ServerEndpoint(value = CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT, configurator = WebSocketEndpointConfigAware.class)
public class ClientGameConnection {
    @Inject
    private Logger logger;
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
    @Inject
    private ServerInventoryService serverInventoryService;
    @Inject
    private PlanetService planetService;
    private ObjectMapper mapper = new ObjectMapper();
    private RemoteEndpoint.Async async;
    private Date time;
    private String gameSessionUuid;
    private String httpSessionId;

    @OnMessage
    public void onMessage(Session session, String text) {
        try {
            GameConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, GameConnectionPacket.class);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = mapper.readValue(payload, packet.getTheClass());
            onPackageReceived(packet, param);
        } catch (Throwable t) {
            exceptionHandler.handleException("text: " + text, t);
        }
    }

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        time = new Date();
        async = session.getAsyncRemote();
        httpSessionId = (String) config.getUserProperties().get(WebSocketEndpointConfigAware.HTTP_SESSION_KEY);
        clientGameConnectionService.onOpen(this, getUserId());
    }

    @OnError
    public void error(Session session, Throwable error) {
        logger.log(Level.WARNING, "ClientGameConnection.error(). Session: " + session, error);
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        clientGameConnectionService.onClose(this);
        async = null;
    }

    protected void onPackageReceived(GameConnectionPacket packet, Object param) {
        switch (packet) {
            case CREATE_BASE:
                UserContext userContext = getUserContext();
                baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getUserId(), userContext.getName(), (DecimalPosition) param);
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
            case SELL_ITEMS:
                baseItemService.sellItems(((IdsDto) param).getIds(), getPlayerBase());
                break;
            case USE_INVENTORY_ITEM:
                serverInventoryService.useInventoryItem((UseInventoryItem) param, getPlayerSession(), getPlayerBaseFull());
                break;
            case SET_GAME_SESSION_UUID:
                gameSessionUuid = (String) param;
                break;
            case TICK_COUNT_REQUEST:
                sendTickSync();
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }

    public void sendToClient(String text) {
        async.sendText(text);
    }

    private void sendTickSync() {
        try {
            sendToClient(ConnectionMarshaller.marshall(GameConnectionPacket.TICK_COUNT_RESPONSE, mapper.writeValueAsString((double) planetService.getTickCount())));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public Date getTime() {
        return time;
    }

    public int getDuration() {
        return (int) (System.currentTimeMillis() - time.getTime());
    }

    public int getUserId() {
        return getPlayerSession().getUserContext().getUserId();
    }

    private UserContext getUserContext() {
        return getPlayerSession().getUserContext();
    }

    private PlayerBase getPlayerBase() {
        return baseItemService.getPlayerBase4UserId(getUserId());
    }

    private PlayerBaseFull getPlayerBaseFull() {
        return baseItemService.getPlayerBaseFull4UserId(getUserId());
    }

    private PlayerSession getPlayerSession() {
        return sessionService.getSession(httpSessionId);
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }
}
