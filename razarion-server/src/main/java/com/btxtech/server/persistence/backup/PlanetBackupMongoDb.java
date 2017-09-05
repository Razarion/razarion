package com.btxtech.server.persistence.backup;

import com.btxtech.server.persistence.MongoDbService;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Created by Beat
 * on 01.09.2017.
 */
@Singleton

public class PlanetBackupMongoDb {
    @Inject
    private MongoDbService mongoDbService;
    @Inject
    private ExceptionHandler exceptionHandler;

    public void saveBackup(BackupPlanetInfo backupPlanetInfo) throws JsonProcessingException {
        mongoDbService.storeObject(backupPlanetInfo, MongoDbService.CollectionName.PLANET_BACKUP);
    }

    public void removeBackup(BackupPlanetOverview backupPlanetOverview) throws JsonProcessingException {
        // TODO
    }

    public List<BackupPlanetOverview> loadAllBackupBaseOverviews() {
        MongoCollection<Document> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.PLANET_BACKUP);
        ObjectMapper objectMapper = mongoDbService.setupObjectMapper();
        List<BackupPlanetOverview> backupPlanetOverviews = new ArrayList<>();
        List<Document> pipeline = Collections.singletonList(Document.parse("{\n" +
                "        \"$project\": {\n" +
                "            \"date\": 1,\n" +
                "            \"planetId\": 1,\n" +
                "            \"bases\": { \"$size\": {\"$ifNull\": [\"$playerBaseInfos\" , [] ]}},\n" +
                "            \"items\": { \"$size\": {\"$ifNull\": [\"$syncBaseItemInfos\" , [] ]}},\n" +
                "            \"quests\": { \"$size\": {\"$ifNull\": [\"$backupComparisionInfos\", [] ]}}\n" +
                "        }\n" +
                "    }"));
        dbCollection.aggregate(pipeline).forEach((Block<Document>) document -> {
            try {
                backupPlanetOverviews.add(objectMapper.readValue(document.toJson(), BackupPlanetOverview.class));
            } catch (IOException e) {
                exceptionHandler.handleException(e);
            }
        });
        return backupPlanetOverviews;
    }

    public BackupPlanetInfo loadBackup(BackupPlanetOverview backupPlanetOverview) {
        SingleHolder<BackupPlanetInfo> holder = new SingleHolder<>();
        ObjectMapper objectMapper = mongoDbService.setupObjectMapper();
        mongoDbService.getCollection(MongoDbService.CollectionName.PLANET_BACKUP).find(Filters.and(Filters.eq("date", DateUtil.tpoJsonTimeString(backupPlanetOverview.getDate())), Filters.eq("planetId", backupPlanetOverview.getPlanetId()))).forEach((Block<Document>) document -> {
            try {
                if (!holder.isEmpty()) {
                    throw new IllegalStateException("More the one entry found. Date: " + backupPlanetOverview.getDate() + " planet id: " + backupPlanetOverview.getPlanetId());
                }
                holder.setO(objectMapper.readValue(document.toJson(), BackupPlanetInfo.class));
            } catch (IOException e) {
                exceptionHandler.handleException(e);
            }
        });
        if (holder.isEmpty()) {
            throw new IllegalStateException("No entries found. Date: " + backupPlanetOverview.getDate() + " planet id: " + backupPlanetOverview.getPlanetId());
        }
        return holder.getO();
    }

    public BackupPlanetInfo loadLastBackup(int planetId) {
        MongoCollection<Document> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.PLANET_BACKUP);
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(Document.parse("{\n" +
                "       $match : { planetId : " + planetId + " }\n" +
                "   }"
        ));
        pipeline.add(Document.parse("{\n" +
                "       $group:\n" +
                "         {\n" +
                "           _id: \"$planetId\",\n" +
                "           date: { $max: \"$date\" }\n" +
                "         }\n" +
                "     }\n"));
        SingleHolder<Date> holder = new SingleHolder<>();
        dbCollection.aggregate(pipeline).forEach((Block<Document>) document -> holder.setO(DateUtil.fromJsonTimeString(document.get("date").toString())));
        if (holder.isEmpty()) {
            return null;
        } else {
            return loadBackup(new BackupPlanetOverview().setDate(holder.getO()).setPlanetId(planetId));
        }
    }
}
