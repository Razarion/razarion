package com.btxtech.server.mgmt;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ClientGameConnection;
import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.QuestPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 05.09.2017.
 */
@Singleton
public class ServerMgmt {
    @Inject
    private ClientSystemConnectionService clientSystemConnectionService;
    @Inject
    private ClientGameConnectionService clientGameConnectionService;
    @Inject
    private ServerGameEngineControl serverGameEngineControl;
    @Inject
    private UserService userService;
    @Inject
    private SessionService sessionService;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private QuestPersistence questPersistence;
    @Inject
    private ServerUnlockService serverUnlockService;

    @SecurityCheck
    public List<OnlineInfo> loadAllOnlines() {
        Map<String, ClientGameConnection> gameSessionUuids = new HashMap<>();
        Collection<ClientGameConnection> unknowns = new ArrayList<>();
        clientGameConnectionService.getClientGameConnections().forEach(clientGameConnection -> {
            String gameSessionUuid = clientGameConnection.getGameSessionUuid();
            if (gameSessionUuid != null) {
                gameSessionUuids.put(gameSessionUuid, clientGameConnection);
            } else {
                unknowns.add(clientGameConnection);
            }
        });

        List<OnlineInfo> onlineInfos = new ArrayList<>();
        clientSystemConnectionService.getClientSystemConnections().forEach(clientSystemConnection -> {
            OnlineInfo onlineInfo = new OnlineInfo().setType(OnlineInfo.Type.NORMAL).setSessionId(clientSystemConnection.getSession().getHttpSessionId()).setTime(clientSystemConnection.getTime()).setDuration(clientSystemConnection.getDuration());
            if (clientSystemConnection.getSession().getUserContext() != null) {
                onlineInfo.setHumanPlayerId(clientSystemConnection.getSession().getUserContext().getHumanPlayerId());
            }
            ClientGameConnection clientGameConnection = gameSessionUuids.remove(clientSystemConnection.getGameSessionUuid());
            if (clientGameConnection != null) {
                onlineInfo.setMultiplayerDate(clientGameConnection.getTime()).setMultiplayerDuration(clientGameConnection.getDuration()).setMultiplayerPlanet(Integer.toString(serverGameEngineControl.getPlanetConfig().getPlanetId()));
            }
            onlineInfos.add(onlineInfo);
        });
        for (ClientGameConnection orphans : gameSessionUuids.values()) {
            onlineInfos.add(new OnlineInfo().setType(OnlineInfo.Type.ORPHAN).setMultiplayerDate(orphans.getTime()).setMultiplayerDuration(orphans.getDuration()).setMultiplayerPlanet(Integer.toString(serverGameEngineControl.getPlanetConfig().getPlanetId())));
        }
        for (ClientGameConnection unknown : unknowns) {
            onlineInfos.add(new OnlineInfo().setType(OnlineInfo.Type.UNKNOWN).setMultiplayerDate(unknown.getTime()).setMultiplayerDuration(unknown.getDuration()).setMultiplayerPlanet(Integer.toString(serverGameEngineControl.getPlanetConfig().getPlanetId())));
        }

        return onlineInfos;
    }

    @SecurityCheck
    public UserBackendInfo loadBackendUserInfo(int playerId) {
        UserBackendInfo userBackendInfo = userService.findUserBackendInfo(playerId);
        if (userBackendInfo != null) {
            return userBackendInfo;
        }
        HumanPlayerId humanPlayerId = new HumanPlayerId().setPlayerId(playerId);
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession == null) {
            throw new IllegalArgumentException("Can not find registered oder unregistered user for playerId: " + playerId);
        }
        return setupUnregisteredUserBackendInfo(humanPlayerId, playerSession);
    }

    private UserBackendInfo setupUnregisteredUserBackendInfo(HumanPlayerId humanPlayerId, PlayerSession playerSession) {
        UserBackendInfo userBackendInfo;
        userBackendInfo = new UserBackendInfo().setHumanPlayerId(humanPlayerId);
        if (playerSession.getUserContext() != null) {
            userBackendInfo.setLevelNumber(levelPersistence.getLevelNumber4Id(playerSession.getUserContext().getLevelId()));
            userBackendInfo.setXp(playerSession.getUserContext().getXp());
        }
        if (playerSession.getUnregisteredUser() != null) {
            userBackendInfo.setCrystals(playerSession.getUnregisteredUser().getCrystals());
            if (playerSession.getUnregisteredUser().getActiveQuest() != null) {
                userBackendInfo.setActiveQuest(new QuestBackendInfo().setId(playerSession.getUnregisteredUser().getActiveQuest().getId()).setInternalName(playerSession.getUnregisteredUser().getActiveQuest().getInternalName()));
            }
            if (playerSession.getUnregisteredUser().getCompletedQuestIds() != null && !playerSession.getUnregisteredUser().getCompletedQuestIds().isEmpty()) {
                userBackendInfo.setCompletedQuests(playerSession.getUnregisteredUser().getCompletedQuestIds().stream().map(questId -> questPersistence.findQuestBackendInfo(questId)).collect(Collectors.toList()));
            }
        }
        return userBackendInfo;
    }

    @SecurityCheck
    public UserBackendInfo removeCompletedQuest(int playerId, int questId) {
        UserBackendInfo userBackendInfo = userService.removeCompletedQuest(playerId, questId);
        if (userBackendInfo != null) {
            return userBackendInfo;
        }

        HumanPlayerId humanPlayerId = new HumanPlayerId().setPlayerId(playerId);
        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
        if (playerSession == null) {
            throw new IllegalArgumentException("Can not find registered oder unregistered user for playerId: " + playerId);
        }

        if (playerSession.getUnregisteredUser() != null) {
            playerSession.getUnregisteredUser().removeCompletedQuestId(questId);
        }
        return setupUnregisteredUserBackendInfo(humanPlayerId, playerSession);
    }

    @SecurityCheck
    public UserBackendInfo setLevelNumber(int playerId, int levelNumber) {
        HumanPlayerId humanPlayerId = userService.findHumanPlayerId(playerId);
        UserContext userContext = userService.getUserContext(humanPlayerId);
        LevelEntity newLevel = levelPersistence.getLevel4Number(levelNumber);
        userContext.setLevelId(newLevel.getId());
        clientSystemConnectionService.onLevelUp(humanPlayerId, userContext, serverUnlockService.gatherAvailableUnlocks(humanPlayerId, newLevel.getId()));
        serverGameEngineControl.onLevelChanged(humanPlayerId, newLevel.getId());
        if (humanPlayerId.getUserId() != null) {
            userService.persistLevel(humanPlayerId.getUserId(), newLevel);
        }
        return loadBackendUserInfo(playerId);
    }

    @SecurityCheck
    public UserBackendInfo setXp(int playerId, int xp) {
        HumanPlayerId humanPlayerId = userService.findHumanPlayerId(playerId);
        UserContext userContext = userService.getUserContext(humanPlayerId);
        userContext.setXp(xp);
        clientSystemConnectionService.onXpChanged(humanPlayerId, xp);
        if (humanPlayerId.getUserId() != null) {
            userService.persistXp(humanPlayerId.getUserId(), xp);
        }
        return loadBackendUserInfo(playerId);
    }
}
