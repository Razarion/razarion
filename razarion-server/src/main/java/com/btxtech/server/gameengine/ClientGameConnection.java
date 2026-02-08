package com.btxtech.server.gameengine;

import com.btxtech.server.user.UserService;
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
import java.util.Date;

@Component
@Scope("prototype")
public class ClientGameConnection {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ClientGameConnection.class);
    private final BaseItemService baseItemService;
    private final PlanetService planetService;
    private final UserService userService;
    private final CommandService commandService;
    private final ClientGameConnectionService clientGameConnectionService;
    private WebSocketSession wsSession;
    private String userId;
    private Date lastMessageSent;
    private Date lastMessageReceived;
    private String gameSessionUuid; // TODO

    public ClientGameConnection(BaseItemService baseItemService,
                                PlanetService planetService,
                                UserService userService,
                                CommandService commandService,
                                ClientGameConnectionService clientGameConnectionService) {
        this.baseItemService = baseItemService;
        this.planetService = planetService;
        this.userService = userService;
        this.commandService = commandService;
        this.clientGameConnectionService = clientGameConnectionService;
    }

    public void init(WebSocketSession wsSession, String userId) {
        this.wsSession = wsSession;
        this.userId = userId;
    }

    public void handleMessage(WebSocketMessage<?> message) {
        lastMessageReceived = new Date();
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
        lastMessageSent = new Date();
        wsSession.sendMessage(new TextMessage(text));
    }

    public void sendInitialSlaveSyncInfo(String userId) {
        try {
            sendToClient(GameConnectionPacket.INITIAL_SLAVE_SYNC_INFO, planetService.generateSlaveSyncItemInfo(userId));
        } catch (Throwable throwable) {
            logger.warn("throwable", throwable);
        }
    }

    private void onPackageReceived(GameConnectionPacket packet, Object param) {
        switch (packet) {
            case CREATE_BASE:
                UserContext userContext = userService.getUserContextTransactional(userId);
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
                BaseCommand cmd = (BaseCommand) param;
                cmd.setForwardedByConnection(true);
                commandService.executeCommand(cmd);
                clientGameConnectionService.broadcastCommand(packet, param, userId);
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
            lastMessageSent = new Date();
            String text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
            wsSession.sendMessage(new TextMessage(text));
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    public String getUserId() {
        return userId;
    }

    private PlayerBase getPlayerBase() {
        return baseItemService.getPlayerBase4UserId(userId);
    }

    private void sendTickSync() {
        try {
            sendToClient(ConnectionMarshaller.marshall(GameConnectionPacket.TICK_COUNT_RESPONSE, MAPPER.writeValueAsString((double) planetService.getTickCount())));
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    public Date getLastMessageSent() {
        return lastMessageSent;
    }

    public Date getLastMessageReceived() {
        return lastMessageReceived;
    }
}
