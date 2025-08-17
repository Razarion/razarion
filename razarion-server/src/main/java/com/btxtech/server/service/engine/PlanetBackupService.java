package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BackupPlanetOverview;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Service
public class PlanetBackupService {
    public static final String PLANET_BACKUP = "planet_backup";
    private final MongoTemplate mongoTemplate;

    public PlanetBackupService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public BackupPlanetInfo loadLastBackup(int planetId) {
        Query query = new Query()
                .addCriteria(Criteria.where("planetId").is(planetId))
                .with(Sort.by(Sort.Direction.DESC, "date"))
                .limit(1);

        return mongoTemplate.findOne(query, BackupPlanetInfo.class, PLANET_BACKUP);
    }

    public void saveBackup(BackupPlanetInfo backupPlanetInfo) {
        mongoTemplate.save(backupPlanetInfo, PLANET_BACKUP);
    }

    public BackupPlanetInfo loadBackup(BackupPlanetOverview backupPlanetOverview) {
        Query query = new Query()
                .addCriteria(Criteria.where("date").is(backupPlanetOverview.getDate())
                        .and("planetId").is(backupPlanetOverview.getPlanetId()));
        return mongoTemplate.findOne(query, BackupPlanetInfo.class, PLANET_BACKUP);
    }

    public List<BackupPlanetOverview> loadAllBackupBaseOverviews() {
        Aggregation aggregation = newAggregation(
                project("date", "planetId")
                        .andExpression("size(playerBaseInfos)").as("bases")
                        .andExpression("size(syncBaseItemInfos)").as("items")
                        .andExpression("size(backupComparisionInfos)").as("quests")
        );

        AggregationResults<BackupPlanetOverview> results =
                mongoTemplate.aggregate(aggregation, PLANET_BACKUP, BackupPlanetOverview.class);

        return results.getMappedResults();
    }

    public void deleteBackup(BackupPlanetOverview backupPlanetOverview) {
        Query query = new Query()
                .addCriteria(Criteria.where("date").is(backupPlanetOverview.getDate())
                        .and("planetId").is(backupPlanetOverview.getPlanetId()));

        mongoTemplate.remove(query, BackupPlanetInfo.class, PLANET_BACKUP);
    }

}