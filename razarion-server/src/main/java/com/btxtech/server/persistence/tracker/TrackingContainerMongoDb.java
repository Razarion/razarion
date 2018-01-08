package com.btxtech.server.persistence.tracker;

import com.btxtech.server.persistence.MongoDbService;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 30.05.2017.
 */
@Singleton
public class TrackingContainerMongoDb {
    @Inject
    private MongoDbService mongoDbService;
    @Inject
    private ExceptionHandler exceptionHandler;

    public void storeTrackingStart(String sessionId, TrackingStart trackingStart) throws JsonProcessingException {
        ServerTrackerStart serverTrackerStart = new ServerTrackerStart();
        serverTrackerStart.setSessionId(sessionId);
        serverTrackerStart.setTimeStamp(new Date());
        serverTrackerStart.setTrackingStart(trackingStart);

        mongoDbService.storeObject(serverTrackerStart, MongoDbService.CollectionName.IN_GAME_TRACKING);
    }

    public void storeDetailedTracking(String sessionId, TrackingContainer trackingContainer) throws JsonProcessingException {
        ServerTrackingContainer serverTrackerStart = new ServerTrackingContainer();
        serverTrackerStart.setSessionId(sessionId);
        serverTrackerStart.setTimeStamp(new Date());
        serverTrackerStart.setTrackingContainer(trackingContainer);
        mongoDbService.storeObject(serverTrackerStart, MongoDbService.CollectionName.IN_GAME_TRACKING);
    }

    public boolean hasServerTrackerStarts(String sessionId, String gameSessionUuid) {
        return mongoDbService.getCollection(MongoDbService.CollectionName.IN_GAME_TRACKING).count(Filters.and(Filters.eq("sessionId", sessionId), Filters.eq("trackingStart.gameSessionUuid", gameSessionUuid))) > 0;
    }

    public ServerTrackerStart findServerTrackerStart(GameUiControlInput gameUiControlInput) {
        MongoCollection<Document> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.IN_GAME_TRACKING);
        ObjectMapper objectMapper =  mongoDbService.setupObjectMapper();
        SingleHolder<ServerTrackerStart> holder = new SingleHolder<>();
        dbCollection.find(Filters.and(Filters.eq("sessionId", gameUiControlInput.getPlaybackSessionUuid()), Filters.eq("trackingStart.gameSessionUuid", gameUiControlInput.getPlaybackGameSessionUuid()))).forEach((Block<Document>) document -> {
            try {
                if (!holder.isEmpty()) {
                    throw new IllegalStateException("More the one entry found. SessionId: " + gameUiControlInput.getPlaybackSessionUuid() + " gameSessionUuid: " + gameUiControlInput.getPlaybackGameSessionUuid());
                }
                holder.setO(objectMapper.readValue(document.toJson(), ServerTrackerStart.class));
            } catch (IOException e) {
                exceptionHandler.handleException(e);
            }
        });
        if (holder.isEmpty()) {
            throw new IllegalArgumentException("No entity found. SessionId: " + gameUiControlInput.getPlaybackSessionUuid() + " gameSessionUuid: " + gameUiControlInput.getPlaybackGameSessionUuid());
        }
        return holder.getO();
    }

    public TrackingContainer findServerTrackingContainer(GameUiControlInput gameUiControlInput) {
        MongoCollection<Document> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.IN_GAME_TRACKING);
        ObjectMapper objectMapper = mongoDbService.setupObjectMapper();
        List<TrackingContainer> trackingContainers = new ArrayList<>();
        dbCollection.find(Filters.and(Filters.eq("sessionId", gameUiControlInput.getPlaybackSessionUuid()), Filters.eq("trackingContainer.gameSessionUuid", gameUiControlInput.getPlaybackGameSessionUuid()))).forEach((Block<Document>) document -> {
            try {
                trackingContainers.add(objectMapper.readValue(document.toJson(), ServerTrackingContainer.class).getTrackingContainer());
            } catch (IOException e) {
                exceptionHandler.handleException(e);
            }
        });

        return generateTrackingContainer(trackingContainers);
    }

    private TrackingContainer generateTrackingContainer(List<TrackingContainer> trackingContainers) {
        TrackingContainer result = new TrackingContainer();
        for (TrackingContainer trackingContainer : trackingContainers) {
            if (trackingContainer.getCameraTrackings() != null) {
                result.getCameraTrackings().addAll(trackingContainer.getCameraTrackings());
            }
            if (trackingContainer.getBrowserWindowTrackings() != null) {
                result.getBrowserWindowTrackings().addAll(trackingContainer.getBrowserWindowTrackings());
            }
            if (trackingContainer.getSelectionTrackings() != null) {
                result.getSelectionTrackings().addAll(trackingContainer.getSelectionTrackings());
            }
            if (trackingContainer.getDialogTrackings() != null) {
                result.getDialogTrackings().addAll(trackingContainer.getDialogTrackings());
            }
            if (trackingContainer.getMouseMoveTrackings() != null) {
                result.getMouseMoveTrackings().addAll(trackingContainer.getMouseMoveTrackings());
            }
            if (trackingContainer.getMouseButtonTrackings() != null) {
                result.getMouseButtonTrackings().addAll(trackingContainer.getMouseButtonTrackings());
            }
            if (trackingContainer.getPlayerBaseTrackings() != null) {
                result.getPlayerBaseTrackings().addAll(trackingContainer.getPlayerBaseTrackings());
            }
            if (trackingContainer.getSyncItemDeletedTrackings() != null) {
                result.getSyncItemDeletedTrackings().addAll(trackingContainer.getSyncItemDeletedTrackings());
            }
            if (trackingContainer.getSyncBaseItemTrackings() != null) {
                result.getSyncBaseItemTrackings().addAll(trackingContainer.getSyncBaseItemTrackings());
            }
            if (trackingContainer.getSyncResourceItemTrackings() != null) {
                result.getSyncResourceItemTrackings().addAll(trackingContainer.getSyncResourceItemTrackings());
            }
            if (trackingContainer.getSyncBoxItemTrackings() != null) {
                result.getSyncBoxItemTrackings().addAll(trackingContainer.getSyncBoxItemTrackings());
            }
        }
        result.getCameraTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getBrowserWindowTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getSelectionTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getDialogTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getMouseMoveTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getMouseButtonTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getPlayerBaseTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getSyncItemDeletedTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getSyncBaseItemTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getSyncResourceItemTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getSyncBoxItemTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        return result;
    }

}
