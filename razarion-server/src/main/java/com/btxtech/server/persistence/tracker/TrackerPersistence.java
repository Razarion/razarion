package com.btxtech.server.persistence.tracker;

import com.btxtech.server.persistence.MongoDbService;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.PlaybackGameUiControlConfig;
import com.btxtech.shared.dto.SceneTrackerInfo;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 28.02.2017.
 */
@Singleton
public class TrackerPersistence {
    // private final Logger logger = Logger.getLogger(TrackerPersistence.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private TrackingContainerMongoDb trackingContainerMongoDb;
    @Inject
    private HistoryPersistence historyPersistence;
    @Inject
    private MongoDbService mongoDbService;

    @Transactional
    public void onNewSession(HttpServletRequest request) {
        try {
            SessionTrackerEntity sessionTrackerEntity = new SessionTrackerEntity();
            sessionTrackerEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
            sessionTrackerEntity.setUserAgent(request.getHeader("user-agent"));
            sessionTrackerEntity.setRemoteAddr(request.getRemoteAddr());
            sessionTrackerEntity.setReferer(request.getHeader("Referer"));
            sessionTrackerEntity.setLanguage(request.getLocale().toString());
            sessionTrackerEntity.setAcceptLanguage(request.getHeader("Accept-Language"));
            sessionTrackerEntity.setTimeStamp(new Date());
            // TODO slows down
            // TODO try {
            // TODO     InetAddress inetAddress = InetAddress.getByName(request.getRemoteAddr());
            // TODO     sessionTrackerEntity.setRemoteHost(inetAddress.getHostName());
            // TODO } catch (UnknownHostException e) {
            // TODO     exceptionHandler.handleException(e);
            // TODO }
            if (request.getCookies() != null) {
                sessionTrackerEntity.setRazarionCookie(Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(CommonUrl.RAZARION_COOKIE_NAME)).map(Cookie::getValue).findFirst().orElse(null));
            }
            // TODO entityManager.persist(sessionTrackerEntity);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Transactional
    public void onPage(String page, HttpServletRequest request) {
        PageTrackerEntity pageTrackerEntity = new PageTrackerEntity();
        pageTrackerEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        pageTrackerEntity.setPage(page);
        pageTrackerEntity.setTimeStamp(new Date());
        pageTrackerEntity.setUri(request.getRequestURI());
        StringBuilder params = new StringBuilder();
        for (Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String[]> entry = iterator.next();
            params.append(entry.getKey());
            params.append("=");
            System.out.print(entry.getKey() + "=");
            String[] value = entry.getValue();
            for (int i = 0; i < value.length; i++) {
                params.append(value[i]);
                if (i + 1 < value.length) {
                    params.append("|");
                }
            }
            if (iterator.hasNext()) {
                params.append("||");
            }
        }
        pageTrackerEntity.setParams(params.toString());
        // TODO entityManager.persist(pageTrackerEntity);
    }

    public void onStartupTask(StartupTaskJson startupTaskJson) {
        startupTaskJson.setHttpSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        startupTaskJson.setServerTime(new Date());
        mongoDbService.storeObject(startupTaskJson, StartupTaskJson.class, MongoDbService.CollectionName.STARTUP_TRACKING);
    }

    public void onStartupTerminated(StartupTerminatedJson startupTerminatedJson) {
        startupTerminatedJson.setHttpSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        startupTerminatedJson.setServerTime(new Date());
        mongoDbService.storeObject(startupTerminatedJson, StartupTerminatedJson.class, MongoDbService.CollectionName.STARTUP_TRACKING);
    }


    public List<StartupTerminatedJson> loadStartupTerminatedJson() {
        List<StartupTerminatedJson> startupTerminatedJsons = new ArrayList<>();
        List<GroupedStartupTerminatedJson> groupedStartupTerminatedJsons = loadGroupedByGameSessionUuid();
        groupedStartupTerminatedJsons.forEach(groupedStartupTerminatedJson -> {

            StartupTerminatedJson startupTerminatedJson = findStartupTerminated(groupedStartupTerminatedJson.getDocuments());
            if (startupTerminatedJson == null) {
                startupTerminatedJson = new StartupTerminatedJson()
                        .gameSessionUuid(groupedStartupTerminatedJson.getGameSessionUuid())
                        .successful(false);
            }

            startupTerminatedJsons.add(startupTerminatedJson);
        });
        return startupTerminatedJsons;
    }

    private StartupTerminatedJson findStartupTerminated(List<Document> documents) {
        return documents.stream()
                .filter(document -> document.containsKey("successful"))
                .map(document -> new StartupTerminatedJson()
                        .gameSessionUuid((String)document.get("gameSessionUuid"))
                        .httpSessionId((String)document.get("httpSessionId"))
                        .serverTime((Date) document.get("serverTime"))
                        .successful((Boolean) document.get("successful"))
                        .totalTime((Integer) document.get("totalTime")))
                .findFirst()
                .orElse(null);
    }

    public List<StartupTaskJson> loadStartupTaskJson(String gameSessionUuid) {
        MongoCollection<StartupTaskJson> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.STARTUP_TRACKING, StartupTaskJson.class);
        List<StartupTaskJson> startupTaskJsons = new ArrayList<>();
        Document query = new Document("taskEnum", new Document("$exists", true))
                .append("gameSessionUuid", gameSessionUuid);
        dbCollection.find(query)
                .forEach((Consumer<? super StartupTaskJson>) startupTaskJsons::add);
        return startupTaskJsons;
    }

    public List<GroupedStartupTerminatedJson> loadGroupedByGameSessionUuid() {
        MongoCollection<Document> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.STARTUP_TRACKING, Document.class);
        List<GroupedStartupTerminatedJson> groupedResults = new ArrayList<>();

        // Definiere die Aggregationspipeline
        List<Bson> pipeline = Arrays.asList(
                Aggregates.group(
                        "$gameSessionUuid",
                        Accumulators.sum("count", 1),  // Zählt die Anzahl der Dokumente in jeder Gruppe
                        Accumulators.push("documents", "$$ROOT")  // Fügt die Dokumente der Gruppe hinzu
                )
        );

        // Führe die Aggregation aus und iteriere über die Ergebnisse
        try (MongoCursor<Document> cursor = dbCollection.aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String gameSessionUuid = doc.getString("_id");
                int count = doc.getInteger("count");
                List<Document> documents = (List<Document>) doc.get("documents");

                // Hier kannst du die Daten in dein Zielobjekt GroupedStartupTerminatedJson umwandeln
                GroupedStartupTerminatedJson groupedResult = new GroupedStartupTerminatedJson(gameSessionUuid, count, documents);
                groupedResults.add(groupedResult);
            }
        }

        return groupedResults;
    }

    class GroupedStartupTerminatedJson {
        private String gameSessionUuid;
        private int count;
        private List<Document> documents;

        public GroupedStartupTerminatedJson(String gameSessionUuid, int count, List<Document> documents) {
            this.gameSessionUuid = gameSessionUuid;
            this.count = count;
            this.documents = documents;
        }

        // Getter und Setter
        public String getGameSessionUuid() {
            return gameSessionUuid;
        }

        public void setGameSessionUuid(String gameSessionUuid) {
            this.gameSessionUuid = gameSessionUuid;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<Document> getDocuments() {
            return documents;
        }

        public void setDocuments(List<Document> documents) {
            this.documents = documents;
        }
    }

    @Transactional
    public void onGameUiControlTrackerInfo(GameUiControlTrackerInfo gameUiControlTrackerInfo) {
        GameUiControlTrackerEntity gameUiControlTrackerEntity = new GameUiControlTrackerEntity();
        gameUiControlTrackerEntity.setTimeStamp(new Date());
        gameUiControlTrackerEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        gameUiControlTrackerEntity.setClientStartTime(gameUiControlTrackerInfo.getStartTime());
        gameUiControlTrackerEntity.setGameSessionUuid(gameUiControlTrackerInfo.getGameSessionUuid());
        gameUiControlTrackerEntity.setDuration(gameUiControlTrackerInfo.getDuration());
        // TODO entityManager.persist(gameUiControlTrackerEntity);
    }

    @Transactional
    public void onSceneTrackerInfo(SceneTrackerInfo sceneTrackerInfo) {
        SceneTrackerEntity sceneTrackerEntity = new SceneTrackerEntity();
        sceneTrackerEntity.setTimeStamp(new Date());
        sceneTrackerEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        sceneTrackerEntity.setClientStartTime(sceneTrackerInfo.getStartTime());
        sceneTrackerEntity.setGameSessionUuid(sceneTrackerInfo.getGameSessionUuid());
        sceneTrackerEntity.setInternalName(sceneTrackerInfo.getInternalName());
        sceneTrackerEntity.setDuration(sceneTrackerInfo.getDuration());
        // TODO entityManager.persist(sceneTrackerEntity);
    }

    @Transactional
    public void onPerformanceTracker(List<PerfmonStatistic> perfmonStatistics) {
        perfmonStatistics.forEach(perfmonStatistic -> {
            PerfmonStatisticEntity fromPerfmonStatistic = new PerfmonStatisticEntity();
            fromPerfmonStatistic.fromPerfmonStatistic(sessionHolder.getPlayerSession().getHttpSessionId(), new Date(), perfmonStatistic);
            // TODO entityManager.persist(fromPerfmonStatistic);
        });
    }

    @Transactional
    public void onTerrainTileStatisticsTracker(List<TerrainTileStatistic> terrainTileStatistics) {
        for (TerrainTileStatistic terrainTileStatistic : terrainTileStatistics) {
            TerrainTileStatisticEntity terrainTileStatisticEntity = new TerrainTileStatisticEntity();
            terrainTileStatisticEntity.setTimeStamp(new Date());
            terrainTileStatisticEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
            terrainTileStatisticEntity.fromTerrainTileStatistic(terrainTileStatistic);
            // TODO entityManager.persist(terrainTileStatisticEntity);
        }
    }

    @Transactional
    public void onFrontendNavigation(String url, String sessionId) {
        FrontendNavigationEntity frontendNavigationEntity = new FrontendNavigationEntity();
        frontendNavigationEntity.setSessionId(sessionId);
        frontendNavigationEntity.setTimeStamp(new Date());
        frontendNavigationEntity.setUrl(url);
        // TODO entityManager.persist(frontendNavigationEntity);
    }

    @Transactional
    public void onWindowClose(String url, String clientTime, String eventString, String sessionId) {
        WindowCloseTrackerEntity closeTrackerEntity = new WindowCloseTrackerEntity();
        closeTrackerEntity.setSessionId(sessionId);
        closeTrackerEntity.setServerTime(new Date());
        closeTrackerEntity.setUrl(url);
        closeTrackerEntity.setClientTime(clientTime);
        closeTrackerEntity.setEventString(eventString);
        // TODO entityManager.persist(closeTrackerEntity);
    }

    public void onTrackingStart(String httpSessionId, TrackingStart trackingStart) throws JsonProcessingException {
        trackingContainerMongoDb.storeTrackingStart(httpSessionId, trackingStart);
    }

    public void onDetailedTracking(String sessionId, TrackingContainer trackingContainer) throws JsonProcessingException {
        trackingContainerMongoDb.storeDetailedTracking(sessionId, trackingContainer);
    }

    @Transactional
    @SecurityCheck
    public List<SessionTracker> readSessionTracking(SearchConfig searchConfig) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SessionTrackerEntity> query = criteriaBuilder.createQuery(SessionTrackerEntity.class);
        Root<SessionTrackerEntity> root = query.from(SessionTrackerEntity.class);
        CriteriaQuery<SessionTrackerEntity> userSelect = query.select(root);

        Predicate predicate = null;
        if (searchConfig.getFromDate() != null) {
            // TODO predicate = criteriaBuilder.greaterThanOrEqualTo(root.get(SessionTrackerEntity_.timeStamp), searchConfig.getFromDate());
        }
        if (searchConfig.isBotFilter()) {
//      TODO      for (String userAgent : BotFilterConstants.userAgentBotStrings()) {
//                Predicate userAgentBot = criteriaBuilder.notLike(root.get(SessionTrackerEntity_.userAgent), userAgent);
//                if (predicate == null) {
//                    predicate = userAgentBot;
//                } else {
//                    predicate = criteriaBuilder.and(predicate, userAgentBot);
//                }
//            }
//            for (String remoteHost : BotFilterConstants.remoteHostBotStrings()) {
//                Predicate userAgentBot = criteriaBuilder.notLike(root.get(SessionTrackerEntity_.remoteHost), remoteHost);
//                if (predicate == null) {
//                    predicate = userAgentBot;
//                } else {
//                    predicate = criteriaBuilder.and(predicate, userAgentBot);
//                }
//            }
        }
        if (predicate != null) {
            query.where(predicate);
        }

        // TODO query.orderBy(criteriaBuilder.desc(root.get(SessionTrackerEntity_.timeStamp)));
        List<SessionTracker> sessionTrackers = new ArrayList<>();
        for (SessionTrackerEntity sessionTrackerEntity : entityManager.createQuery(userSelect).getResultList()) {
            SessionTracker sessionTracker = sessionTrackerEntity.toSessionTracker();
            sessionTracker.setGameAttempts(readStartupTaskCount(sessionTrackerEntity.getSessionId())).setSuccessGameAttempts(readSuccessStartupTerminatedCount(sessionTrackerEntity.getSessionId()));
            sessionTracker.setCreatedHumanPlayerId(readCreatedHumanPlayerId(sessionTrackerEntity.getSessionId()));
            sessionTracker.setUserFromHistory(historyPersistence.readUserFromHistory(sessionTrackerEntity.getSessionId()));
            sessionTracker.setPageHits(getPageHits(sessionTrackerEntity.getSessionId()));
            sessionTrackers.add(sessionTracker);
        }
        return sessionTrackers;
    }

    private int readStartupTaskCount(String sessionId) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
//        Root<StartupTaskEntity> root = cq.from(StartupTaskEntity.class);
//        // TODO cq.where(criteriaBuilder.equal(root.get(StartupTaskEntity_.sessionId), sessionId));
//        // TODO cq.select(criteriaBuilder.countDistinct(root.get(StartupTaskEntity_.gameSessionUuid)));
//        return entityManager.createQuery(cq).getSingleResult().intValue();
        return -999;
    }

    private int readSuccessStartupTerminatedCount(String sessionId) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
//        Root<StartupTerminatedEntity> root = cq.from(StartupTerminatedEntity.class);
//        // TODO cq.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(StartupTerminatedEntity_.sessionId), sessionId), criteriaBuilder.equal(root.get(StartupTerminatedEntity_.successful), true)));
//        cq.select(criteriaBuilder.count(root));
//        return entityManager.createQuery(cq).getSingleResult().intValue();
        return -999;
    }

    @Deprecated
    private Integer readCreatedHumanPlayerId(String sessionId) {
        throw new UnsupportedOperationException("...No longer supported...");
    }

    private int getPageHits(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
        Root<PageTrackerEntity> root = cq.from(PageTrackerEntity.class);
        // TODO cq.where(criteriaBuilder.equal(root.get(PageTrackerEntity_.sessionId), sessionId));
        cq.select(criteriaBuilder.count(root));
        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    @Transactional
    @SecurityCheck
    public SessionDetail readSessionDetail(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SessionTrackerEntity> query = criteriaBuilder.createQuery(SessionTrackerEntity.class);
        Root<SessionTrackerEntity> root = query.from(SessionTrackerEntity.class);
        CriteriaQuery<SessionTrackerEntity> userSelect = query.select(root);
        // TODO query.where(criteriaBuilder.equal(root.get(SessionTrackerEntity_.sessionId), sessionId));
        SessionTrackerEntity sessionTrackerEntity = entityManager.createQuery(userSelect).getSingleResult();

        SessionDetail sessionDetail = new SessionDetail().setId(sessionTrackerEntity.getSessionId()).setTime(sessionTrackerEntity.getTimeStamp()).setUserAgent(sessionTrackerEntity.getUserAgent()).setReferer(sessionTrackerEntity.getReferer());
        sessionDetail.setRemoteAddr(sessionTrackerEntity.getRemoteAddr()).setRemoteHost(sessionTrackerEntity.getRemoteHost());
        sessionDetail.setLanguage(sessionTrackerEntity.getLanguage());
        sessionDetail.setAcceptLanguage(sessionTrackerEntity.getAcceptLanguage());
        sessionDetail.setGameSessionDetails(readGameSessionDetails(sessionId));
        List<PageDetail> pageDetails = new ArrayList<>();
        pageDetails.addAll(readPageDetails(sessionId));
        pageDetails.addAll(readFrontendNavigationDetails(sessionId));
        pageDetails.addAll(readWindowCloseTrackerDetails(sessionId));
        pageDetails.sort(Comparator.comparing(PageDetail::getTime));
        sessionDetail.setPageDetails(pageDetails);
        return sessionDetail;
    }

    private List<PageDetail> readPageDetails(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PageTrackerEntity> query = criteriaBuilder.createQuery(PageTrackerEntity.class);
        Root<PageTrackerEntity> root = query.from(PageTrackerEntity.class);
        // TODO query.where(criteriaBuilder.equal(root.get(PageTrackerEntity_.sessionId), sessionId));
        CriteriaQuery<PageTrackerEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getResultList().stream().map(PageTrackerEntity::toPageDetail).collect(Collectors.toList());
    }

    private List<PageDetail> readFrontendNavigationDetails(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FrontendNavigationEntity> query = criteriaBuilder.createQuery(FrontendNavigationEntity.class);
        Root<FrontendNavigationEntity> root = query.from(FrontendNavigationEntity.class);
        // TODO query.where(criteriaBuilder.equal(root.get(FrontendNavigationEntity_.sessionId), sessionId));
        CriteriaQuery<FrontendNavigationEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getResultList().stream().map(FrontendNavigationEntity::toPageDetail).collect(Collectors.toList());
    }

    private List<PageDetail> readWindowCloseTrackerDetails(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<WindowCloseTrackerEntity> query = criteriaBuilder.createQuery(WindowCloseTrackerEntity.class);
        Root<WindowCloseTrackerEntity> root = query.from(WindowCloseTrackerEntity.class);
        // TODO query.where(criteriaBuilder.equal(root.get(WindowCloseTrackerEntity_.sessionId), sessionId));
        CriteriaQuery<WindowCloseTrackerEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getResultList().stream().map(WindowCloseTrackerEntity::toPageDetail).collect(Collectors.toList());
    }

    private List<GameSessionDetail> readGameSessionDetails(String sessionId) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
//        Root<StartupTaskEntity> root = cq.from(StartupTaskEntity.class);
//        // TODO cq.where(criteriaBuilder.equal(root.get(StartupTaskEntity_.sessionId), sessionId));
//        // TODO cq.multiselect(root.get(StartupTaskEntity_.gameSessionUuid), root.get(StartupTaskEntity_.startTime), root.get(StartupTaskEntity_.clientStartTime));
//        // TODO cq.groupBy(root.get(StartupTaskEntity_.gameSessionUuid));
//        // TODO cq.orderBy(criteriaBuilder.asc(root.get(StartupTaskEntity_.clientStartTime)));
//        return entityManager.createQuery(cq).getResultList().stream().map(tuple -> readGameSessionDetail(sessionId, (String) tuple.get(0), (Date) tuple.get(1), (Date) tuple.get(2))).collect(Collectors.toList());
        return null;
    }

    private GameSessionDetail readGameSessionDetail(String sessionId, String gameSessionUuid, Date time, Date clientTime) {
        GameSessionDetail gameSessionDetail = new GameSessionDetail();
        gameSessionDetail.setSessionId(sessionId).setId(gameSessionUuid).setTime(time).setClientTime(clientTime);
        gameSessionDetail.setStartupTaskDetails(readStartupTaskDetails(sessionId, gameSessionUuid));
        gameSessionDetail.setStartupTerminatedDetail(readStartupTerminatedDetail(sessionId, gameSessionUuid));
        gameSessionDetail.setInGameTracking(trackingContainerMongoDb.hasServerTrackerStarts(sessionId, gameSessionUuid));
        gameSessionDetail.setSceneTrackerDetails(readSceneTrackerDetails(sessionId, gameSessionUuid));
        gameSessionDetail.setPerfmonTrackerDetails(readPerfmonTrackerDetails(sessionId, gameSessionUuid));
        gameSessionDetail.setPerfmonTerrainTileDetails(readPerfmonTerrainTileDetails(sessionId, gameSessionUuid));
        return gameSessionDetail;
    }

    private List<StartupTaskDetail> readStartupTaskDetails(String sessionId, String gameSessionUuid) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StartupTaskEntity> query = criteriaBuilder.createQuery(StartupTaskEntity.class);
//        Root<StartupTaskEntity> root = query.from(StartupTaskEntity.class);
//        // TODO query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(StartupTaskEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(StartupTaskEntity_.gameSessionUuid), gameSessionUuid));
//        // TODO query.orderBy(criteriaBuilder.asc(root.get(StartupTaskEntity_.clientStartTime)));
//
//        return entityManager.createQuery(query).getResultList().stream().map(StartupTaskEntity::toStartupTaskDetail).collect(Collectors.toList());
        return null;
    }

    private StartupTerminatedDetail readStartupTerminatedDetail(String sessionId, String gameSessionUuid) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StartupTerminatedEntity> query = criteriaBuilder.createQuery(StartupTerminatedEntity.class);
//        Root<StartupTerminatedEntity> root = query.from(StartupTerminatedEntity.class);
//        // TODO query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(StartupTerminatedEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(StartupTerminatedEntity_.gameSessionUuid), gameSessionUuid));
//
//        List<StartupTerminatedEntity> startupTerminatedEntities = entityManager.createQuery(query).getResultList();
//        if (startupTerminatedEntities.isEmpty()) {
//            return null;
//        }
//        if (startupTerminatedEntities.size() > 1) {
//            logger.warning("More then one entry found for StartupTerminatedEntity. sessionId: " + sessionId + " gameSessionUuid: " + gameSessionUuid);
//        }
//        return startupTerminatedEntities.get(0).toStartupTerminatedDetail();
        return null;
    }

    private List<SceneTrackerDetail> readSceneTrackerDetails(String sessionId, String gameSessionUuid) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SceneTrackerEntity> query = criteriaBuilder.createQuery(SceneTrackerEntity.class);
        Root<SceneTrackerEntity> root = query.from(SceneTrackerEntity.class);
        // TODO query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(SceneTrackerEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(SceneTrackerEntity_.gameSessionUuid), gameSessionUuid));
        // TODO query.orderBy(criteriaBuilder.asc(root.get(SceneTrackerEntity_.clientStartTime)));

        return entityManager.createQuery(query).getResultList().stream().map(SceneTrackerEntity::toSceneTrackerDetail).collect(Collectors.toList());
    }

    private List<PerfmonTrackerDetail> readPerfmonTrackerDetails(String sessionId, String gameSessionUuid) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PerfmonStatisticEntity> query = criteriaBuilder.createQuery(PerfmonStatisticEntity.class);
        Root<PerfmonStatisticEntity> root = query.from(PerfmonStatisticEntity.class);
        // TODO query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(PerfmonStatisticEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(PerfmonStatisticEntity_.gameSessionUuid), gameSessionUuid));
        // TODO query.orderBy(criteriaBuilder.asc(root.get(PerfmonStatisticEntity_.clientTimeStamp)));

        List<PerfmonTrackerDetail> perfmonTrackerDetails = new ArrayList<>();
        entityManager.createQuery(query).getResultList().forEach(perfmonStatisticEntity -> perfmonTrackerDetails.addAll(perfmonStatisticEntity.toPerfmonTrackerDetails()));
        perfmonTrackerDetails.sort(Comparator.comparing(PerfmonTrackerDetail::getClientStartTime));
        return perfmonTrackerDetails;
    }

    private List<PerfmonTerrainTileDetail> readPerfmonTerrainTileDetails(String sessionId, String gameSessionUuid) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TerrainTileStatisticEntity> query = criteriaBuilder.createQuery(TerrainTileStatisticEntity.class);
        Root<TerrainTileStatisticEntity> root = query.from(TerrainTileStatisticEntity.class);
        // TODO query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(TerrainTileStatisticEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(TerrainTileStatisticEntity_.gameSessionUuid), gameSessionUuid));
        // TODO query.orderBy(criteriaBuilder.asc(root.get(TerrainTileStatisticEntity_.clientTimeStamp)));
        return entityManager.createQuery(query).getResultList().stream().map(TerrainTileStatisticEntity::toPerfmonTerrainTileDetail).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public WarmGameUiContext setupWarmGameUiControlConfig(GameUiControlInput gameUiControlInput) {
        ServerTrackerStart serverTrackerStart = trackingContainerMongoDb.findServerTrackerStart(gameUiControlInput);

        PlanetEntity planetEntity = planetCrudPersistence.getEntity(serverTrackerStart.getTrackingStart().getPlanetId());
        WarmGameUiContext warmGameUiContext = new WarmGameUiContext().gameUiControlConfigId(-2).gameEngineMode(GameEngineMode.PLAYBACK);
        warmGameUiContext.planetConfig(planetEntity.toPlanetConfig());

        PlaybackGameUiControlConfig playbackGameUiControlConfig = new PlaybackGameUiControlConfig();
        playbackGameUiControlConfig.setTrackingStart(serverTrackerStart.getTrackingStart()).setTrackingContainer(trackingContainerMongoDb.findServerTrackingContainer(gameUiControlInput));
        warmGameUiContext.setPlaybackGameUiControlConfig(playbackGameUiControlConfig);
        return warmGameUiContext;
    }
}
