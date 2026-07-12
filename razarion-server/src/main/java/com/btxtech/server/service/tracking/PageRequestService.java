package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
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

    public void onHome(PageRequest pageRequest) {
        save(pageRequest, PageRequestType.HOME);
    }

    public void onGame(PageRequest pageRequest) {
        save(pageRequest, PageRequestType.GAME);
    }

    private void save(PageRequest pageRequest, PageRequestType pageRequestType) {
        try {
            pageRequest
                    .pageRequestType(pageRequestType)
                    .serverTime(new Date());
            logger.info("Page request {} tracked: utmCampaign={} utmSource={} utmMedium={} twclid={} rdtCid={} session={} query='{}'",
                    pageRequestType,
                    pageRequest.getUtmCampaign(),
                    pageRequest.getUtmSource(),
                    pageRequest.getUtmMedium(),
                    pageRequest.getTwclid(),
                    pageRequest.getRdtCid(),
                    pageRequest.getHttpSessionId(),
                    pageRequest.getRawQueryString());
            mongoTemplate.save(pageRequest, PAGE_REQUEST);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public String findRdtCidByHttpSessionId(String httpSessionId) {
        if (httpSessionId == null) {
            return null;
        }
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("httpSessionId").is(httpSessionId)
                    .and("rdtCid").ne(null));
            query.with(Sort.by(Sort.Direction.DESC, "serverTime"));
            query.limit(1);
            PageRequest pageRequest = mongoTemplate.findOne(query, PageRequest.class, PAGE_REQUEST);
            return pageRequest != null ? pageRequest.getRdtCid() : null;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }

    public String findTwclidByHttpSessionId(String httpSessionId) {
        if (httpSessionId == null) {
            return null;
        }
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("httpSessionId").is(httpSessionId)
                    .and("twclid").ne(null));
            query.with(Sort.by(Sort.Direction.DESC, "serverTime"));
            query.limit(1);
            PageRequest pageRequest = mongoTemplate.findOne(query, PageRequest.class, PAGE_REQUEST);
            return pageRequest != null ? pageRequest.getTwclid() : null;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
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
