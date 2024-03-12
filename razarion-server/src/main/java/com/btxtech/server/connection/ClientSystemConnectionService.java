package com.btxtech.server.connection;

import com.btxtech.server.persistence.tracker.ConnectionTrackingPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    @Inject
    private ConnectionTrackingPersistence connectionTrackingPersistence;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MapCollection<PlayerSession, ClientSystemConnection> systemConnections = new MapCollection<>();

    public void onOpen(ClientSystemConnection clientSystemConnection) {
        synchronized (systemConnections) {
            systemConnections.put(clientSystemConnection.getSession(), clientSystemConnection);
        }
        connectionTrackingPersistence.onSystemConnectionOpened(clientSystemConnection.getSession().getHttpSessionId(), clientSystemConnection.getSession());
    }

    public void onClose(ClientSystemConnection clientSystemConnection) {
        synchronized (systemConnections) {
            systemConnections.remove(clientSystemConnection.getSession(), clientSystemConnection);
        }
        connectionTrackingPersistence.onSystemConnectionClosed(clientSystemConnection.getSession().getHttpSessionId(), clientSystemConnection.getSession());
    }

    public void onQuestProgressInfo(int userId, QuestProgressInfo questProgressInfo) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_PROGRESS_CHANGED, questProgressInfo);
        }
    }

    public void onQuestActivated(int userId, QuestConfig quest) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_ACTIVATED, quest);
        }
    }

    public void onQuestPassed(int userId, QuestConfig quest) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_PASSED, quest);
        }
    }

    public void onXpChanged(int userId, int xp) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.XP_CHANGED, xp);
        }
    }

    public void onLevelUp(int userId, UserContext newLevelId, boolean availableUnlocks) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.LEVEL_UPDATE_SERVER, new LevelUpPacket().userContext(newLevelId).availableUnlocks(availableUnlocks));
        }
    }

    public void onBoxPicked(int userId, BoxContent boxContent) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.BOX_PICKED, boxContent);
        }
    }

    public void onUnlockedItemLimit(int userId, Map<Integer, Integer> unlockedItemLimit, boolean blinking) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.UNLOCKED_ITEM_LIMIT, new UnlockedItemPacket().unlockedItemLimit(unlockedItemLimit).availableUnlocks(blinking));
        }
    }

    public void sendLifecyclePacket(LifecyclePacket lifecyclePacket) {
        sendToClients(SystemConnectionPacket.LIFECYCLE_CONTROL, lifecyclePacket);
    }

    public void sendChatMessage(ChatMessage chatMessage) {
        sendToClients(SystemConnectionPacket.CHAT_RECEIVE_MESSAGE, chatMessage);
    }

    public void sendChatMessage(PlayerSession playerSession, ChatMessage chatMessage) {
        sendToClient(playerSession, SystemConnectionPacket.CHAT_RECEIVE_MESSAGE, chatMessage);
    }

    public void sendEmailVerifiedToClient(PlayerSession playerSession) {
        sendToClient(playerSession, SystemConnectionPacket.EMAIL_VERIFIED, null);
    }

    private void sendToClient(PlayerSession playerSession, SystemConnectionPacket packet, Object object) {
        Collection<ClientSystemConnection> clientSystemConnections;
        synchronized (systemConnections) {
            clientSystemConnections = systemConnections.get(playerSession);
            if (clientSystemConnections == null) {
                return;
            }
        }

        sendToClient(packet, object, clientSystemConnections);
    }

    private void sendToClients(SystemConnectionPacket packet, Object object) {
        Collection<ClientSystemConnection> clientSystemConnections;
        synchronized (systemConnections) {
            clientSystemConnections = systemConnections.getAll();
        }
        sendToClient(packet, object, clientSystemConnections);
    }

    private void sendToClient(SystemConnectionPacket packet, Object object, Collection<ClientSystemConnection> clientSystemConnections) {
        try {
            String text;
            if (packet.getTheClass() == Void.class) {
                text = ConnectionMarshaller.marshall(packet, null);
            } else {
                text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
            }
            clientSystemConnections.forEach(clientSystemConnection -> {
                try {
                    clientSystemConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    exceptionHandler.handleException(throwable);
                }
            });
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    public Collection<ClientSystemConnection> getClientSystemConnections() {
        synchronized (systemConnections) {
            return systemConnections.getAll();
        }
    }
}
