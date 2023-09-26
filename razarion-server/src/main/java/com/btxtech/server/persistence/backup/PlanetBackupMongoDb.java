package com.btxtech.server.persistence.backup;

import com.btxtech.server.persistence.MongoDbService;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


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
        mongoDbService.storeObject(backupPlanetInfo, BackupPlanetInfo.class, MongoDbService.CollectionName.PLANET_BACKUP);
    }

    public List<BackupPlanetOverview> loadAllBackupBaseOverviews() {
        MongoCollection<BackupPlanetOverview> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.PLANET_BACKUP, BackupPlanetOverview.class);
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
        dbCollection.aggregate(pipeline).forEach((Consumer<BackupPlanetOverview>) backupPlanetOverviews::add);
        return backupPlanetOverviews;
    }

    public BackupPlanetInfo loadBackup(BackupPlanetOverview backupPlanetOverview) {
        BackupPlanetInfo backupPlanetInfo = mongoDbService.getCollection(MongoDbService.CollectionName.PLANET_BACKUP, BackupPlanetInfo.class).find(setupBackupFilter(backupPlanetOverview)).first();
        if (backupPlanetInfo == null) {
            throw new IllegalStateException("No entries found. Date: " + backupPlanetOverview.getDate() + " planet id: " + backupPlanetOverview.getPlanetId());
        }
        return backupPlanetInfo;
    }

    public BackupPlanetInfo loadLastBackup(int planetId) {
        try {
            MongoCollection<BackupPlanetInfo> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.PLANET_BACKUP, BackupPlanetInfo.class);
            return dbCollection.find(Filters.eq("planetId", planetId)).sort(Sorts.descending("date")).first();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        return null;
    }

    public void deleteBackup(BackupPlanetOverview backupPlanetOverview) {
        MongoCollection<BackupPlanetInfo> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.PLANET_BACKUP, BackupPlanetInfo.class);
        dbCollection.deleteOne(setupBackupFilter(backupPlanetOverview));
    }

    private Bson setupBackupFilter(BackupPlanetOverview backupPlanetOverview) {
        return Filters.and(Filters.eq("date", backupPlanetOverview.getDate()), Filters.eq("planetId", backupPlanetOverview.getPlanetId()));
    }
}
