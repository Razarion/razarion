package com.btxtech.server.connection;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Singleton
public class ClientSystemConnectionService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SessionService sessionService;
    private ObjectMapper mapper = new ObjectMapper();
    private final Map<PlayerSession, ClientSystemConnection> systemGameConnections = new HashMap<>();

    public void onOpen(ClientSystemConnection clientSystemConnection) {
        synchronized (systemGameConnections) {
            systemGameConnections.put(clientSystemConnection.getSession(), clientSystemConnection);
        }
    }

    public void onClose(ClientSystemConnection clientSystemConnection) {
        synchronized (systemGameConnections) {
            systemGameConnections.remove(clientSystemConnection.getSession());
        }
    }

    public void onQuestProgressInfo(HumanPlayerId humanPlayerId, QuestProgressInfo questProgressInfo) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_PROGRESS_CHANGED, questProgressInfo);
        }
    }

    public void onQuestActivated(HumanPlayerId humanPlayerId, QuestConfig quest) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_ACTIVATED, quest);
        }
    }

    public void onQuestPassed(HumanPlayerId humanPlayerId, QuestConfig quest) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_PASSED, quest);
        }
    }

    public void onXpChanged(HumanPlayerId humanPlayerId, int xp) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.XP_CHANGED, xp);
        }

    }

    public void onLevelUp(HumanPlayerId humanPlayerId, UserContext newLevelId) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.LEVEL_UPDATE_SERVER, newLevelId);
        }
    }

    private void sendToClient(PlayerSession playerSession, SystemConnectionPacket packet, Object object) {
        ClientSystemConnection clientSystemConnection;
        synchronized (systemGameConnections) {
            clientSystemConnection = systemGameConnections.get(playerSession);
            if (clientSystemConnection == null) {
                return;
            }
        }

        try {
            String text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
            clientSystemConnection.sendToClient(text);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    public void iteratorClientSystemConnection(BiConsumer<PlayerSession, ClientSystemConnection> callback) {
        synchronized (systemGameConnections) {
            systemGameConnections.forEach(callback);
        }
    }
}
