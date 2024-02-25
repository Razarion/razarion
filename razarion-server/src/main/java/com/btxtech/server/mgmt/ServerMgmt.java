package com.btxtech.server.mgmt;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ClientGameConnection;
import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.ServerState;
import com.btxtech.shared.dto.UserBackendInfo;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 05.09.2017.
 */
@Singleton
public class ServerMgmt {
    // private Logger logger = Logger.getLogger(ServerMgmt.class.getName());
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
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private ServerUnlockService serverUnlockService;
    @Inject
    private HistoryPersistence historyPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerTerrainShapeService serverTerrainShapeService;
    @Inject
    private ClientSystemConnectionService systemConnectionService;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    private ServerState serverState = ServerState.UNKNOWN;

    public ServerState getServerState() {
        return serverState;
    }

    public void setServerState(ServerState serverState) {
        this.serverState = serverState;
    }

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
            OnlineInfo onlineInfo = new OnlineInfo().setType(OnlineInfo.Type.NORMAL).setSessionId(clientSystemConnection.getHttpSessionId()).setTime(clientSystemConnection.getTime()).setDuration(clientSystemConnection.getDuration());
            try {
                if (sessionService.checkSession(clientSystemConnection.getHttpSessionId())) {
                    PlayerSession playerSession = sessionService.getSession(clientSystemConnection.getHttpSessionId());
                    if (playerSession.getUserContext() != null) {
                        onlineInfo.setName(playerSession.getUserContext().getName()).setUserId(playerSession.getUserContext().getUserId());
                    }
                    onlineInfo.setSessionTime(playerSession.getTime());
                } else {
                    onlineInfo.setType(OnlineInfo.Type.NO_SESSION);
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
            ClientGameConnection clientGameConnection = gameSessionUuids.remove(clientSystemConnection.getGameSessionUuid());
            if (clientGameConnection != null) {
                onlineInfo.setMultiplayerDate(clientGameConnection.getTime()).setMultiplayerDuration(clientGameConnection.getDuration()).setMultiplayerPlanet(Integer.toString(serverGameEngineControl.getPlanetConfig().getId()));
            }
            onlineInfos.add(onlineInfo);
        });
        for (ClientGameConnection orphans : gameSessionUuids.values()) {
            onlineInfos.add(new OnlineInfo().setType(OnlineInfo.Type.ORPHAN).setMultiplayerDate(orphans.getTime()).setMultiplayerDuration(orphans.getDuration()).setMultiplayerPlanet(Integer.toString(serverGameEngineControl.getPlanetConfig().getId())));
        }
        for (ClientGameConnection unknown : unknowns) {
            onlineInfos.add(new OnlineInfo().setType(OnlineInfo.Type.UNKNOWN).setMultiplayerDate(unknown.getTime()).setMultiplayerDuration(unknown.getDuration()).setMultiplayerPlanet(Integer.toString(serverGameEngineControl.getPlanetConfig().getId())));
        }

        return onlineInfos;
    }

    @SecurityCheck
    public UserBackendInfo loadBackendUserInfo(int playerId) {
//        UserBackendInfo userBackendInfo = userService.findUserBackendInfo(playerId);
//        if (userBackendInfo != null) {
//            userBackendInfo.setGameHistoryEntries(historyPersistence.readUserHistory(playerId));
//            return userBackendInfo;
//        }
//        HumanPlayerId humanPlayerId = new HumanPlayerId().setPlayerId(playerId);
//        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
//        if (playerSession != null) {
//            // Anonymous user from session
//            userBackendInfo = setupUnregisteredUserBackendInfo(humanPlayerId, playerSession);
//        } else {
//            // Anonymous user session is gone
//            userBackendInfo = new UserBackendInfo();
//            userBackendInfo.setHumanPlayerId(new HumanPlayerId().setPlayerId(playerId));
//        }
//        userBackendInfo.setGameHistoryEntries(historyPersistence.readUserHistory(playerId));
//        return userBackendInfo;
        throw new UnsupportedOperationException("...TODO...");
    }

    @SecurityCheck
    public UserBackendInfo removeCompletedQuest(int playerId, int questId) {
//        UserBackendInfo userBackendInfo = userService.removeCompletedQuest(playerId, questId);
//        if (userBackendInfo != null) {
//            return userBackendInfo;
//        }
//
//        HumanPlayerId humanPlayerId = new HumanPlayerId().setPlayerId(playerId);
//        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
//        if (playerSession == null) {
//            throw new IllegalArgumentException("Can not find registered oder unregistered user for playerId: " + playerId);
//        }
//
//        if (playerSession.getUnregisteredUser() != null) {
//            playerSession.getUnregisteredUser().removeCompletedQuestId(questId);
//        }
//        return setupUnregisteredUserBackendInfo(humanPlayerId, playerSession);
        throw new UnsupportedOperationException("...TODO...");
    }

    @SecurityCheck
    public UserBackendInfo addCompletedQuest(int playerId, int questId) {
//        UserBackendInfo userBackendInfo = userService.addCompletedQuest(playerId, questId);
//        if (userBackendInfo != null) {
//            return userBackendInfo;
//        }
//
//        HumanPlayerId humanPlayerId = new HumanPlayerId().setPlayerId(playerId);
//        PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
//        if (playerSession == null) {
//            throw new IllegalArgumentException("Can not find registered oder unregistered user for playerId: " + playerId);
//        }
//
//        if (playerSession.getUnregisteredUser() != null) {
//            playerSession.getUnregisteredUser().addCompletedQuestId(questId);
//        }
//        return setupUnregisteredUserBackendInfo(humanPlayerId, playerSession);
        throw new UnsupportedOperationException("...TODO...");
    }

    @SecurityCheck
    public UserBackendInfo removeUnlockedItem(int playerId, int unlockItemId) {
//        HumanPlayerId humanPlayerId = sessionService.findPlayerSession(new HumanPlayerId().setPlayerId(playerId)).getUserContext().getHumanPlayerId();
//        serverUnlockService.removeUnlocked(humanPlayerId, unlockItemId);
//        return loadBackendUserInfo(playerId);
        throw new UnsupportedOperationException("...TODO...");
    }

    @SecurityCheck
    public void sendRestartLifecycle() {
        serverState = ServerState.SHUTTING_DOWN;
        clientSystemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(LifecyclePacket.Type.RESTART));
    }

    @SecurityCheck
    public void restartPlanetWarm() {
        restartPlanet(LifecyclePacket.Type.PLANET_RESTART_WARM);
    }

    @SecurityCheck
    public void restartPlanetCold() {
        restartPlanet(LifecyclePacket.Type.PLANET_RESTART_COLD);
    }

    private void restartPlanet(LifecyclePacket.Type type) {
        try {
            systemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(LifecyclePacket.Type.HOLD).setDialog(LifecyclePacket.Dialog.PLANET_RESTART));
            serverTerrainShapeService.createTerrainShape(serverGameEngineCrudPersistence.read().get(0).getPlanetConfigId());
            serverGameEngineControl.restartPlanet();
            systemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(type));
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
