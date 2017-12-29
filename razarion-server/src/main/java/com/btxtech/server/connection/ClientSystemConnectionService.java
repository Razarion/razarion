package com.btxtech.server.connection;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
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
    private ObjectMapper mapper = new ObjectMapper();
    private final MapCollection<PlayerSession, ClientSystemConnection> systemGameConnections = new MapCollection<>();

    public void onOpen(ClientSystemConnection clientSystemConnection) {
        synchronized (systemGameConnections) {
            systemGameConnections.put(clientSystemConnection.getSession(), clientSystemConnection);
        }
    }

    public void onClose(ClientSystemConnection clientSystemConnection) {
        synchronized (systemGameConnections) {
            systemGameConnections.remove(clientSystemConnection.getSession(), clientSystemConnection);
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

    public void onLevelUp(HumanPlayerId humanPlayerId, UserContext newLevelId, List<LevelUnlockConfig> levelUnlockConfigs) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.LEVEL_UPDATE_SERVER, new LevelUpPacket().setUserContext(newLevelId).setLevelUnlockConfigs(levelUnlockConfigs));
        }
    }

    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.BOX_PICKED, boxContent);
        }
    }

    public void onUnlockedItemLimit(HumanPlayerId humanPlayerId, Map<Integer, Integer> unlockedItemLimit) {
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.UNLOCKED_ITEM_LIMIT, new UnlockedItemPacket().setUnlockedItemLimit(unlockedItemLimit));
        }
    }

    public void sendLifecyclePacket(LifecyclePacket lifecyclePacket) {
        sendToClients(SystemConnectionPacket.LIFECYCLE_CONTROL, lifecyclePacket);
    }

    public void sendChatMessage(ChatMessage chatMessage) {
        sendToClients(SystemConnectionPacket.CHAT_RECEIVE_MESSAGE, chatMessage);
    }

    private void sendToClient(PlayerSession playerSession, SystemConnectionPacket packet, Object object) {
        Collection<ClientSystemConnection> clientSystemConnections;
        synchronized (systemGameConnections) {
            clientSystemConnections = systemGameConnections.get(playerSession);
            if (clientSystemConnections == null) {
                return;
            }
        }

        sendToClient(packet, object, clientSystemConnections);
    }

    private void sendToClients(SystemConnectionPacket packet, Object object) {
        Collection<ClientSystemConnection> clientSystemConnections;
        synchronized (systemGameConnections) {
            clientSystemConnections = systemGameConnections.getAll();
        }
        sendToClient(packet, object, clientSystemConnections);
    }

    private void sendToClient(SystemConnectionPacket packet, Object object, Collection<ClientSystemConnection> clientSystemConnections) {
        try {
            String text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
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
        synchronized (systemGameConnections) {
            return systemGameConnections.getAll();
        }
    }
}
