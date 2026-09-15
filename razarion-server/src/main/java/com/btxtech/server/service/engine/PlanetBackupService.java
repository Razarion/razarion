package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BackupPlanetOverview;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.index.Index;
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
    private final Logger logger = LoggerFactory.getLogger(PlanetBackupService.class);

    public PlanetBackupService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * What {@link #loadLastBackup} asks for on every start, and it had no index at all.
     * <p>
     * Deliberately <b>no</b> expiry, unlike every tracking collection. A TTL here is a rule that
     * deletes the world: it cannot say "keep the newest per planet", only "delete everything past
     * an age", and a server that has been down longer than that age would come back to an empty
     * planet. Four backups a day at 17 kB each is 29 MB a year - far too little to be worth that
     * risk. Old backups are pruned by hand from the backend's restore list.
     */
    @PostConstruct
    public void ensureIndexes() {
        try {
            mongoTemplate.indexOps(PLANET_BACKUP)
                    .ensureIndex(new Index().on("planetId", Sort.Direction.ASC).on("date", Sort.Direction.DESC));
        } catch (Exception e) {
            logger.warn("Could not ensure the planetId/date index on {}: {}", PLANET_BACKUP, e.getMessage());
        }
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