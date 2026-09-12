package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageRequestService {
    public static final String PAGE_REQUEST = "page_request";
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(PageRequestService.class);

    public PageRequestService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * serverTime for the range reads, httpSessionId for the per-session lookups that resolve a
     * visitor's click id. Neither had an index; both scanned the whole collection.
     */
    @PostConstruct
    public void ensureIndexes() {
        TrackingIndexes.ensureServerTimeIndex(mongoTemplate, logger, PAGE_REQUEST);
        try {
            mongoTemplate.indexOps(PAGE_REQUEST).ensureIndex(new Index().on("httpSessionId", Sort.Direction.ASC));
        } catch (Exception e) {
            logger.warn("Could not ensure the httpSessionId index on {}: {}", PAGE_REQUEST, e.getMessage());
        }
    }

    public void onHome(PageRequest pageRequest) {
        save(pageRequest, PageRequestType.HOME);
    }

    /**
     * The landing page was requested. Recorded for the referer it carries; see
     * {@link PageRequestType#LANDING}.
     */
    public void onLanding(PageRequest pageRequest) {
        save(pageRequest, PageRequestType.LANDING);
    }

    /**
     * Every signal the landing page reports arrives through the same pixel URL, so which of them
     * it is has already been decided by the caller.
     */
    public void onHomeEvent(PageRequest pageRequest, PageRequestType pageRequestType) {
        save(pageRequest, pageRequestType);
    }

    public void onGame(PageRequest pageRequest) {
        save(pageRequest, PageRequestType.GAME);
    }

    private void save(PageRequest pageRequest, PageRequestType pageRequestType) {
        try {
            pageRequest
                    .pageRequestType(pageRequestType)
                    .serverTime(new Date());
            logger.info("Page request {} tracked: utmCampaign={} utmSource={} utmMedium={} twclid={} rdtCid={} fbclid={} session={} query='{}'",
                    pageRequestType,
                    pageRequest.getUtmCampaign(),
                    pageRequest.getUtmSource(),
                    pageRequest.getUtmMedium(),
                    pageRequest.getTwclid(),
                    pageRequest.getRdtCid(),
                    pageRequest.getFbclid(),
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

    /**
     * The whole visit rather than only its click id, because Meta wants the browser with the
     * event and this row is the last place that still knows it - by the time a base is built
     * there is no request left to read a user agent from.
     */
    public PageRequest findFbclidPageRequest(String httpSessionId) {
        if (httpSessionId == null) {
            return null;
        }
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("httpSessionId").is(httpSessionId)
                    .and("fbclid").ne(null));
            query.with(Sort.by(Sort.Direction.DESC, "serverTime"));
            query.limit(1);
            return mongoTemplate.findOne(query, PageRequest.class, PAGE_REQUEST);
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

    /**
     * The same window as {@link #loadPageRequests}, reduced to one record per http session.
     * <p>
     * The history view never wants a page request; it wants what the session behind it says about
     * the visitor, and it asked that question by scanning the whole list once per row. Measured on
     * a 24-hour window: 4,522 documents and 5.71 MB to produce an 88 kB response, where 75 % of
     * every document is the query string, the referrer, the user agent and the click id - the same
     * strings again on each of a session's requests. Grouped here instead, 4,522 documents become
     * about 1,250 records of a few hundred bytes.
     * <p>
     * Sorted before grouping so "the earliest click id in the session" means that, rather than
     * whatever order the storage engine happened to return - which is what the scan it replaces was
     * reading, since the query it used carried no sort at all.
     * <p>
     * {@code $addToSet} rather than {@code $push} for the strings: within one session the referrer
     * is usually the identical value on every request, and the caller only tests them for
     * foreignness, so duplicates would be bytes for nothing.
     */
    public Map<String, SessionAttribution> loadSessionAttribution(Date fromDate, Date toDate) {
        return collect(sessionAttributionPipeline(fromDate, toDate));
    }

    /**
     * Built apart from being run, so that a test can build it without a database.
     * <p>
     * The version of this that shipped put a null into a {@code List.of} - which rejects one, and
     * the null is the whole point of it, since {@code $$REMOVE} is ignored inside {@code $push}. So
     * every call threw a NullPointerException before the database saw a single stage, and the
     * history tab answered 500. The stages had been verified against production data by running
     * them from a script; the Java that assembles them had never been executed once.
     */
    List<Document> sessionAttributionPipeline(Date fromDate, Date toDate) {
        return List.of(
                new Document("$match", matchWindow(fromDate, toDate)
                        .append("httpSessionId", new Document("$ne", null))),
                new Document("$sort", new Document("serverTime", 1)),
                new Document("$group", new Document("_id", "$httpSessionId")
                        .append("userAgents", TrackingAttribution.addToSetNonNull("$userAgent"))
                        .append("referers", TrackingAttribution.addToSetNonNull("$referer"))
                        .append("utmSources", TrackingAttribution.addToSetNonNull("$utmSource"))
                        .append("landingReferers", new Document("$addToSet", new Document("$cond",
                                List.of(new Document("$eq", List.of("$pageRequestType", PageRequestType.LANDING.name())),
                                        "$referer", "$$REMOVE"))))
                        // The click ids travel as a triple: one request may carry the fbclid and
                        // another the twclid, and ofClickIds() reads them together.
                        //
                        // null and a $filter below rather than $$REMOVE, which does not work inside
                        // $push - measured, not assumed: a session whose requests carry no click id
                        // came back with a [{}] instead of an []. That happens to be harmless once
                        // the fields are read off it, but it would not be for a session that landed
                        // without a click id and picked one up later: the empty first element would
                        // hide the real one.
                        //
                        // $ifNull around each field for a second reason found the same way: an
                        // absent field resolves to "missing", and {$ne: [missing, null]} is TRUE in
                        // an aggregation expression, unlike the $match semantics one expects. Every
                        // request without click ids therefore took the "has one" branch. $ifNull is
                        // what makes missing and null the same thing again.
                        //
                        // Shared with the startup side rather than spelled out twice: the copy that
                        // used to stand here built its $cond with List.of, which rejects null - and
                        // the null is the whole point of it. Every call threw a
                        // NullPointerException before it reached the database.
                        .append("clickIds", TrackingAttribution.clickIds())
                        .append("firstGameTime", new Document("$min", new Document("$cond",
                                List.of(new Document("$eq", List.of("$pageRequestType", PageRequestType.GAME.name())),
                                        "$serverTime", "$$REMOVE"))))),
                TrackingAttribution.filterNulls("clickIds"));
    }

    private Map<String, SessionAttribution> collect(List<Document> pipeline) {
        Map<String, SessionAttribution> perSession = new HashMap<>();
        for (Document document : mongoTemplate.getCollection(PAGE_REQUEST).aggregate(pipeline)) {
            String httpSessionId = document.getString("_id");
            if (httpSessionId == null) {
                continue;
            }
            List<Document> clickIds = document.getList("clickIds", Document.class, List.of());
            Document firstClickId = clickIds.isEmpty() ? new Document() : clickIds.get(0);
            perSession.put(httpSessionId, new SessionAttribution(
                    TrackingAttribution.strings(document, "userAgents"),
                    TrackingAttribution.strings(document, "landingReferers"),
                    TrackingAttribution.strings(document, "referers"),
                    TrackingAttribution.strings(document, "utmSources"),
                    firstClickId.getString("rdtCid"),
                    firstClickId.getString("twclid"),
                    firstClickId.getString("fbclid"),
                    document.getDate("firstGameTime")));
        }
        return perSession;
    }

    private Document matchWindow(Date fromDate, Date toDate) {
        Document serverTime = new Document();
        if (fromDate != null) {
            serverTime.append("$gte", fromDate);
        }
        if (toDate != null) {
            serverTime.append("$lte", toDate);
        }
        return serverTime.isEmpty() ? new Document() : new Document("serverTime", serverTime);
    }

}
