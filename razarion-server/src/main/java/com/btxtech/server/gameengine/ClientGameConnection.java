package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.CommonUrl;
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
import java.util.List;

/**
 * Created by Beat
 * 20.04.2017.
 */
@ServerEndpoint(value = CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT, configurator = WebSocketEndpointConfigAware.class)
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
    @Inject
    private ServerInventoryService serverInventoryService;
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
        clientGameConnectionService.onOpen(this, getHumanPlayerId());
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
        switch (packet) {
            case CREATE_BASE:
                UserContext userContext = getUserContext();
                baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), (DecimalPosition) param);
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
                baseItemService.sellItems((List<Integer>) param, getPlayerBase());
                break;
            case USE_INVENTORY_ITEM:
                serverInventoryService.useInventoryItem((UseInventoryItem) param, getPlayerSession(), getPlayerBaseFull());
                break;
            case SET_GAME_SESSION_UUID:
                gameSessionUuid = (String) param;
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }

    public void sendToClient(String text) {
        async.sendText(text);
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

    public HumanPlayerId getHumanPlayerId() {
        return getPlayerSession().getUserContext().getHumanPlayerId();
    }

    private UserContext getUserContext() {
        return getPlayerSession().getUserContext();
    }

    private PlayerBase getPlayerBase() {
        return baseItemService.getPlayerBase4HumanPlayerId(getHumanPlayerId());
    }

    private PlayerBaseFull getPlayerBaseFull() {
        return baseItemService.getPlayerBaseFull4HumanPlayerId(getHumanPlayerId());
    }

    private PlayerSession getPlayerSession() {
        return sessionService.getSession(httpSessionId);
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }
}
