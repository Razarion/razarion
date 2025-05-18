package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@Scope("prototype")
public class ClientGameConnection {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ClientGameConnection.class);
    private final BaseItemService baseItemService;
    private final PlanetService planetService;
    private final SessionService sessionService;
    private final CommandService commandService;
    private WebSocketSession wsSession;
    private String httpSessionId;
    private String gameSessionUuid; // TODO

    public ClientGameConnection(BaseItemService baseItemService,
                                PlanetService planetService,
                                SessionService sessionService,
                                CommandService commandService) {
        this.baseItemService = baseItemService;
        this.planetService = planetService;
        this.sessionService = sessionService;
        this.commandService = commandService;
        logger.info("ClientGameConnection created {}", System.identityHashCode(this));
    }

    public void init(WebSocketSession wsSession, String httpSessionId) {
        this.wsSession = wsSession;
        this.httpSessionId = httpSessionId;
    }

    public void handleMessage(WebSocketMessage<?> message) {
        try {
            var text = message.getPayload().toString();
            GameConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, GameConnectionPacket.class);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = MAPPER.readValue(payload, packet.getTheClass());
            onPackageReceived(packet, param);
        } catch (Throwable t) {
            logger.warn("message: {} session: {}", message, wsSession, t);
        }
    }

    public void sendToClient(String text) throws IOException {
        wsSession.sendMessage(new TextMessage(text));
    }

    public void sendInitialSlaveSyncInfo(int userId) {
        try {
            sendToClient(GameConnectionPacket.INITIAL_SLAVE_SYNC_INFO, planetService.generateSlaveSyncItemInfo(userId));
        } catch (Throwable throwable) {
            logger.warn("throwable", throwable);
        }
    }

    private void onPackageReceived(GameConnectionPacket packet, Object param) {
        switch (packet) {
            case CREATE_BASE:
                UserContext userContext = getUserContext();
                baseItemService.createHumanBaseWithBaseItem(
                        userContext.getLevelId(),
                        userContext.getUnlockedItemLimit(),
                        userContext.getUserId(),
                        userContext.getName(),
                        (DecimalPosition) param);
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
                // TODO serverInventoryService.useInventoryItem((UseInventoryItem) param, getPlayerSession(), getPlayerBaseFull());
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

    private void sendToClient(GameConnectionPacket packet, Object object) {
        try {
            String text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
            wsSession.sendMessage(new TextMessage(text));
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    public UserContext getUserContext() {
        return getPlayerSession().getUserContext();
    }

    private PlayerSession getPlayerSession() {
        logger.info("ClientGameConnection getPlayerSession(): {} {}", System.identityHashCode(this), httpSessionId);
        return sessionService.getSession(httpSessionId);
    }

    private PlayerBase getPlayerBase() {
        return baseItemService.getPlayerBase4UserId(getPlayerSession().getUserContext().getUserId());
    }

    private void sendTickSync() {
        try {
            sendToClient(ConnectionMarshaller.marshall(GameConnectionPacket.TICK_COUNT_RESPONSE, MAPPER.writeValueAsString((double) planetService.getTickCount())));
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }
}
