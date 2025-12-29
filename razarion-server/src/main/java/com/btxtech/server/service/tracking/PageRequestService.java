package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PageRequestService {
    public static final String PAGE_REQUEST = "page_request";
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(PageRequestService.class);

    public PageRequestService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void onHome(String sessionId, String utmCampaign, String utmSource, String rdtCid) {
        try {
            var pageRequest = new PageRequest()
                    .pageRequestType(PageRequestType.HOME)
                    .serverTime(new Date())
                    .httpSessionId(sessionId)
                    .utmCampaign(utmCampaign)
                    .utmSource(utmSource)
                    .rdtCid(rdtCid);
            mongoTemplate.save(pageRequest, PAGE_REQUEST);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void onGame(String sessionId, String utmCampaign, String utmSource, String rdtCid) {
        try {
            var pageRequest = new PageRequest()
                    .pageRequestType(PageRequestType.GAME)
                    .serverTime(new Date())
                    .httpSessionId(sessionId)
                    .utmCampaign(utmCampaign)
                    .utmSource(utmSource)
                    .rdtCid(rdtCid);
            mongoTemplate.save(pageRequest, PAGE_REQUEST);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public List<PageRequest> loadPageRequests(Date fromDate, Date toDate) {
        Query query = new Query();
        if (fromDate != null && toDate != null) {
            query.addCriteria(
                    Criteria.where("serverTime").gte(fromDate).lte(toDate)
            );
        } else if (fromDate != null) {
            query.addCriteria(
                    Criteria.where("serverTime").gte(fromDate)
            );
        } else if (toDate != null) {
            query.addCriteria(
                    Criteria.where("serverTime").lte(toDate)
            );
        }

        return mongoTemplate.find(query, PageRequest.class, PAGE_REQUEST);
    }
}
