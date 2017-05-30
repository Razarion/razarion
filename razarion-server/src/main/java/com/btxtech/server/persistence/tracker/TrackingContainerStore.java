package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;
import com.btxtech.shared.dto.GameUiControlInput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class TrackingContainerStore {
    private Map<String, Map<String, ServerTrackingContainer>> sessionTrackingContainers = new HashMap<>();

    public void onTrackingStart(String httpSessionId, TrackingStart trackingStart) {
        Map<String, ServerTrackingContainer> containers = sessionTrackingContainers.computeIfAbsent(httpSessionId, k -> new HashMap<>());
        containers.put(trackingStart.getGameSessionUuid(), new ServerTrackingContainer(trackingStart));
    }

    public void onDetailedTracking(String sessionId, TrackingContainer trackingContainer) {
        sessionTrackingContainers.get(sessionId).get(trackingContainer.getGameSessionUuid()).addTrackingContainer(trackingContainer);
    }

    public List<ServerTrackingContainer> getServerTrackingContainers(String sessionId) {
        List<ServerTrackingContainer> serverTrackingContainers = new ArrayList<>();
        if (sessionTrackingContainers.containsKey(sessionId)) {
            serverTrackingContainers.addAll(sessionTrackingContainers.get(sessionId).values());
        }
        serverTrackingContainers.sort(Comparator.comparing(ServerTrackingContainer::getTime));
        return serverTrackingContainers;
    }

    public ServerTrackingContainer getServerTrackingContainer(GameUiControlInput gameUiControlInput) {
        Map<String, ServerTrackingContainer> serverTrackerContainers = sessionTrackingContainers.get(gameUiControlInput.getPlaybackSessionUuid());
        if (serverTrackerContainers == null) {
            throw new IllegalArgumentException("No ServerTrackingContainer found. SessionId: " + gameUiControlInput.getPlaybackSessionUuid());
        }
        ServerTrackingContainer serverTrackingContainer = serverTrackerContainers.get(gameUiControlInput.getPlaybackGameSessionUuid());
        if (serverTrackingContainer == null) {
            throw new IllegalArgumentException("No ServerTrackingContainer found. SessionId: " + gameUiControlInput.getPlaybackSessionUuid() + " GameSessionId: " + gameUiControlInput.getPlaybackGameSessionUuid());
        }
        return serverTrackingContainer;
    }
}
