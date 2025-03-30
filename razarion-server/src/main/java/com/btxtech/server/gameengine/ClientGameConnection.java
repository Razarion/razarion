package com.btxtech.server.gameengine;

import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

public class ClientGameConnection {
    private final Logger logger = LoggerFactory.getLogger(ClientGameConnection.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private String gameSessionUuid;

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            var text = message.getPayload().toString();
            GameConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, GameConnectionPacket.class);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = mapper.readValue(payload, packet.getTheClass());
            onPackageReceived(packet, param);
        } catch (Throwable t) {
            logger.warn("message: {} session: {}", message, session, t);
        }
    }

    public void onOpen(WebSocketSession session) {
        sendInitialSlaveSyncInfo(session);
    }

    private void onPackageReceived(GameConnectionPacket packet, Object param) {
        switch (packet) {
            case CREATE_BASE:
                // UserContext userContext = getUserContext();
                // TODO baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getUserId(), userContext.getName(), (DecimalPosition) param);
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
                // TODO commandService.executeCommand((BaseCommand) param);
                break;
            case SELL_ITEMS:
                // TODO baseItemService.sellItems(((IdsDto) param).getIds(), getPlayerBase());
                break;
            case USE_INVENTORY_ITEM:
                // TODO serverInventoryService.useInventoryItem((UseInventoryItem) param, getPlayerSession(), getPlayerBaseFull());
                break;
            case SET_GAME_SESSION_UUID:
                gameSessionUuid = (String) param;
                break;
            case TICK_COUNT_REQUEST:
                System.out.println("TICK COUNT REQUEST");
                // TODO sendTickSync();
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }

    private void sendInitialSlaveSyncInfo(WebSocketSession session) {
        try {
            // TODO ----------------------
            InitialSlaveSyncItemInfo initialSlaveSyncItemInfo = new InitialSlaveSyncItemInfo();
            initialSlaveSyncItemInfo.setPlayerBaseInfos(new ArrayList<>());
            initialSlaveSyncItemInfo.setSyncBaseItemInfos(new ArrayList<>());
            // ----------------------
            sendToClient(session, GameConnectionPacket.INITIAL_SLAVE_SYNC_INFO, initialSlaveSyncItemInfo/*TODOplanetServiceInstance.get().generateSlaveSyncItemInfo(userId)*/);
        } catch (Throwable throwable) {
            logger.warn("throwable", throwable);
        }
    }

    private void sendToClient(WebSocketSession session, GameConnectionPacket packet, Object object) {
        try {
            String text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
            session.sendMessage(new TextMessage(text));
        } catch (Throwable throwable) {
            logger.warn("throwable", throwable);
        }
    }
}
