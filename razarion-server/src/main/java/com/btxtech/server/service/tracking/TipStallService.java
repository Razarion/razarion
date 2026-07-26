package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.TipStallJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Records quest tips that stopped making progress.
 * <p>
 * The quest funnel showed the losses but never the cause: a tip task that cannot go on simply
 * retries forever and writes a console warning nobody reads. One document per stall closes that
 * hole - it names the quest, the task and what it was waiting for, and whether the player ever
 * got past it.
 */
@Service
public class TipStallService {
    public static final String TIP_STALL_COLLECTION = "tip_stall";
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(TipStallService.class);

    public TipStallService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Stores a stall, or marks an already stored one resolved. Never throws: this is telemetry
     * from the running game and must not turn into an error the player sees.
     */
    public void onTipStall(TipStallJson tipStallJson, String userId) {
        try {
            tipStallJson.setUserId(userId);
            if (tipStallJson.isResolved()) {
                markResolved(tipStallJson);
            } else {
                tipStallJson.setServerTime(new Date());
                mongoTemplate.save(tipStallJson, TIP_STALL_COLLECTION);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * The resolution carries the same stallUuid as the stall it belongs to. If the stall itself
     * never made it to the server the resolution is stored on its own rather than dropped - a
     * lone resolved document is still true, and losing it would count as a dead end.
     */
    private void markResolved(TipStallJson tipStallJson) {
        Query query = new Query(Criteria.where("stallUuid").is(tipStallJson.getStallUuid()));
        Update update = new Update()
                .set("resolved", true)
                .set("resolvedMillis", tipStallJson.getResolvedMillis());
        var result = mongoTemplate.updateFirst(query, update, TIP_STALL_COLLECTION);
        if (result.getMatchedCount() == 0) {
            tipStallJson.setServerTime(new Date());
            mongoTemplate.save(tipStallJson, TIP_STALL_COLLECTION);
        }
    }

    public List<TipStallJson> loadTipStalls(Date fromDate, Date toDate) {
        Query query = new Query();
        if (fromDate != null && toDate != null) {
            query.addCriteria(Criteria.where("serverTime").gte(fromDate).lte(toDate));
        } else if (fromDate != null) {
            query.addCriteria(Criteria.where("serverTime").gte(fromDate));
        } else if (toDate != null) {
            query.addCriteria(Criteria.where("serverTime").lte(toDate));
        }
        return mongoTemplate.find(query, TipStallJson.class, TIP_STALL_COLLECTION);
    }
}
