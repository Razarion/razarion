package com.btxtech.server.service;

import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * The welcome bonuses paid (CheerService#helperBonus), in Mongo so that a restart - every deploy -
 * pays no pair twice and starts no helper's day afresh. One document per helper and newcomer, its
 * id the pair: a second insert fails on the id, which is the "already paid" answer.
 * <p>
 * Kept for good: at most ten a day per helper, and "once per newcomer" is meant for ever.
 */
@Service
public class CheerBonusStore {
    static final String COLLECTION = "cheer_bonus";
    private final Logger logger = LoggerFactory.getLogger(CheerBonusStore.class);
    private final MongoTemplate mongoTemplate;

    public CheerBonusStore(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void ensureIndexes() {
        try {
            mongoTemplate.indexOps(COLLECTION).ensureIndex(new Index()
                    .on("helperUserId", Sort.Direction.ASC)
                    .on("time", Sort.Direction.DESC));
        } catch (Exception e) {
            logger.warn("Could not ensure the helperUserId/time index on {}: {}", COLLECTION, e.getMessage());
        }
    }

    /**
     * Notes the bonus for this pair and answers true, or false when the pair was paid before, the
     * helper had {@code maxPerDay} today (UTC) or Mongo could not be asked - no bonus then.
     */
    public boolean claim(String helperUserId, String newcomerUserId, int maxPerDay) {
        try {
            Date midnight = Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant());
            long today = mongoTemplate.count(new Query(Criteria.where("helperUserId").is(helperUserId)
                    .and("time").gte(midnight)), COLLECTION);
            if (today >= maxPerDay) {
                return false;
            }
            mongoTemplate.insert(new Document("_id", helperUserId + ">" + newcomerUserId)
                    .append("helperUserId", helperUserId)
                    .append("newcomerUserId", newcomerUserId)
                    .append("time", new Date()), COLLECTION);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        } catch (Exception e) {
            logger.warn("Welcome bonus {} > {} not paid: {}", helperUserId, newcomerUserId, e.getMessage());
            return false;
        }
    }
}
