package com.btxtech.server.persistence.tracker;

import com.btxtech.server.marketing.facebook.FbFacade;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.user.HumanPlayerIdEntity;
import com.btxtech.server.user.HumanPlayerIdEntity_;
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
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 28.02.2017.
 */
@ApplicationScoped
public class TrackerPersistence {
    private Logger logger = Logger.getLogger(TrackerPersistence.class.getName());
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
            try {
                InetAddress inetAddress = InetAddress.getByName(request.getRemoteAddr());
                sessionTrackerEntity.setRemoteHost(inetAddress.getHostName());
            } catch (UnknownHostException e) {
                exceptionHandler.handleException(e);
            }
            if (request.getCookies() != null) {
                sessionTrackerEntity.setRazarionCookie(Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(CommonUrl.RAZARION_COOKIE_NAME)).map(Cookie::getValue).findFirst().orElse(null));
            }
            entityManager.persist(sessionTrackerEntity);
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
        entityManager.persist(pageTrackerEntity);
    }

    @Transactional
    public void onStartupTask(StartupTaskJson startupTaskJson) {
        StartupTaskEntity startupTaskEntity = new StartupTaskEntity();
        startupTaskEntity.setStartTime(new Date());
        startupTaskEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        startupTaskEntity.setGameSessionUuid(startupTaskJson.getGameSessionUuid());
        startupTaskEntity.setClientStartTime(startupTaskJson.getStartTime());
        startupTaskEntity.setDuration(startupTaskJson.getDuration());
        startupTaskEntity.setTaskEnum(startupTaskJson.getTaskEnum());
        startupTaskEntity.setError(startupTaskJson.getError());
        entityManager.persist(startupTaskEntity);
    }

    @Transactional
    public void onStartupTerminated(StartupTerminatedJson startupTerminatedJson) {
        StartupTerminatedEntity startupTerminatedEntity = new StartupTerminatedEntity();
        startupTerminatedEntity.setTimeStamp(new Date());
        startupTerminatedEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        startupTerminatedEntity.setGameSessionUuid(startupTerminatedJson.getGameSessionUuid());
        startupTerminatedEntity.setTotalTime(startupTerminatedJson.getTotalTime());
        startupTerminatedEntity.setSuccessful(startupTerminatedJson.isSuccessful());
        entityManager.persist(startupTerminatedEntity);
    }

    @Transactional
    public void onGameUiControlTrackerInfo(GameUiControlTrackerInfo gameUiControlTrackerInfo) {
        GameUiControlTrackerEntity gameUiControlTrackerEntity = new GameUiControlTrackerEntity();
        gameUiControlTrackerEntity.setTimeStamp(new Date());
        gameUiControlTrackerEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        gameUiControlTrackerEntity.setClientStartTime(gameUiControlTrackerInfo.getStartTime());
        gameUiControlTrackerEntity.setGameSessionUuid(gameUiControlTrackerInfo.getGameSessionUuid());
        gameUiControlTrackerEntity.setDuration(gameUiControlTrackerInfo.getDuration());
        entityManager.persist(gameUiControlTrackerEntity);
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
        entityManager.persist(sceneTrackerEntity);
    }

    @Transactional
    public void onPerformanceTracker(List<PerfmonStatistic> perfmonStatistics) {
        perfmonStatistics.forEach(perfmonStatistic -> {
            PerfmonStatisticEntity fromPerfmonStatistic = new PerfmonStatisticEntity();
            fromPerfmonStatistic.fromPerfmonStatistic(sessionHolder.getPlayerSession().getHttpSessionId(), new Date(), perfmonStatistic);
            entityManager.persist(fromPerfmonStatistic);
        });
    }

    @Transactional
    public void onTerrainTileStatisticsTracker(List<TerrainTileStatistic> terrainTileStatistics) {
        for (TerrainTileStatistic terrainTileStatistic : terrainTileStatistics) {
            TerrainTileStatisticEntity terrainTileStatisticEntity = new TerrainTileStatisticEntity();
            terrainTileStatisticEntity.setTimeStamp(new Date());
            terrainTileStatisticEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
            terrainTileStatisticEntity.fromTerrainTileStatistic(terrainTileStatistic);
            entityManager.persist(terrainTileStatisticEntity);
        }
    }

    @Transactional
    public void onFrontendNavigation(String url, String sessionId) {
        FrontendNavigationEntity frontendNavigationEntity = new FrontendNavigationEntity();
        frontendNavigationEntity.setSessionId(sessionId);
        frontendNavigationEntity.setTimeStamp(new Date());
        frontendNavigationEntity.setUrl(url);
        entityManager.persist(frontendNavigationEntity);
    }

    @Transactional
    public void onWindowClose(String url, String clientTime, String eventString, String sessionId) {
        WindowCloseTrackerEntity closeTrackerEntity = new WindowCloseTrackerEntity();
        closeTrackerEntity.setSessionId(sessionId);
        closeTrackerEntity.setServerTime(new Date());
        closeTrackerEntity.setUrl(url);
        closeTrackerEntity.setClientTime(clientTime);
        closeTrackerEntity.setEventString(eventString);
        entityManager.persist(closeTrackerEntity);
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
            predicate = criteriaBuilder.greaterThanOrEqualTo(root.get(SessionTrackerEntity_.timeStamp), searchConfig.getFromDate());
        }
        if (searchConfig.isBotFilter()) {
            for (String userAgent : BotFilterConstants.userAgentBotStrings()) {
                Predicate userAgentBot = criteriaBuilder.notLike(root.get(SessionTrackerEntity_.userAgent), userAgent);
                if (predicate == null) {
                    predicate = userAgentBot;
                } else {
                    predicate = criteriaBuilder.and(predicate, userAgentBot);
                }
            }
            for (String remoteHost : BotFilterConstants.remoteHostBotStrings()) {
                Predicate userAgentBot = criteriaBuilder.notLike(root.get(SessionTrackerEntity_.remoteHost), remoteHost);
                if (predicate == null) {
                    predicate = userAgentBot;
                } else {
                    predicate = criteriaBuilder.and(predicate, userAgentBot);
                }
            }
        }
        if (predicate != null) {
            query.where(predicate);
        }

        query.orderBy(criteriaBuilder.desc(root.get(SessionTrackerEntity_.timeStamp)));
        List<SessionTracker> sessionTrackers = new ArrayList<>();
        for (SessionTrackerEntity sessionTrackerEntity : entityManager.createQuery(userSelect).getResultList()) {
            SessionTracker sessionTracker = sessionTrackerEntity.toSessionTracker();
            sessionTracker.setGameAttempts(readStartupTaskCount(sessionTrackerEntity.getSessionId())).setSuccessGameAttempts(readSuccessStartupTerminatedCount(sessionTrackerEntity.getSessionId()));
            sessionTracker.setFbAdRazTrack(getFbAdRazTrack(sessionTrackerEntity.getSessionId()));
            sessionTracker.setCreatedHumanPlayerId(readCreatedHumanPlayerId(sessionTrackerEntity.getSessionId()));
            sessionTracker.setUserFromHistory(historyPersistence.readUserFromHistory(sessionTrackerEntity.getSessionId()));
            sessionTracker.setPageHits(getPageHits(sessionTrackerEntity.getSessionId()));
            sessionTrackers.add(sessionTracker);
        }
        return sessionTrackers;
    }

    private int readStartupTaskCount(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
        Root<StartupTaskEntity> root = cq.from(StartupTaskEntity.class);
        cq.where(criteriaBuilder.equal(root.get(StartupTaskEntity_.sessionId), sessionId));
        cq.select(criteriaBuilder.countDistinct(root.get(StartupTaskEntity_.gameSessionUuid)));
        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    private int readSuccessStartupTerminatedCount(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
        Root<StartupTerminatedEntity> root = cq.from(StartupTerminatedEntity.class);
        cq.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(StartupTerminatedEntity_.sessionId), sessionId), criteriaBuilder.equal(root.get(StartupTerminatedEntity_.successful), true)));
        cq.select(criteriaBuilder.count(root));
        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    private String getFbAdRazTrack(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PageTrackerEntity> query = criteriaBuilder.createQuery(PageTrackerEntity.class);
        Root<PageTrackerEntity> root = query.from(PageTrackerEntity.class);
        CriteriaQuery<PageTrackerEntity> userSelect = query.select(root);
        Predicate sessionPredicate = criteriaBuilder.equal(root.get(PageTrackerEntity_.sessionId), sessionId);
        Predicate fbAdRazTrackPredicate = criteriaBuilder.like(root.get(PageTrackerEntity_.params), "%" + FbFacade.URL_PARAM_TRACK_KEY + "%");
        query.where(criteriaBuilder.and(sessionPredicate, fbAdRazTrackPredicate));
        List<PageTrackerEntity> pageTrackerEntities = entityManager.createQuery(userSelect).setFirstResult(0).setMaxResults(1).getResultList();
        if (pageTrackerEntities.isEmpty()) {
            return null;
        }
        PageTrackerEntity pageTrackerEntity = pageTrackerEntities.get(0);
        int fromIndex = pageTrackerEntity.getParams().indexOf(FbFacade.URL_PARAM_TRACK_KEY);
        fromIndex = pageTrackerEntity.getParams().indexOf("=", fromIndex);
        int toIndex = pageTrackerEntity.getParams().indexOf("||", fromIndex);
        if (toIndex < 0) {
            toIndex = pageTrackerEntity.getParams().length();
        }
        return pageTrackerEntity.getParams().substring(fromIndex + 1, toIndex).trim();
    }

    private Integer readCreatedHumanPlayerId(String sessionId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HumanPlayerIdEntity> cq = criteriaBuilder.createQuery(HumanPlayerIdEntity.class);
        Root<HumanPlayerIdEntity> root = cq.from(HumanPlayerIdEntity.class);
        cq.where(criteriaBuilder.equal(root.get(HumanPlayerIdEntity_.sessionId), sessionId));
        List<HumanPlayerIdEntity> humanPlayerIdEntities = entityManager.createQuery(cq).getResultList();
        if (humanPlayerIdEntities.isEmpty()) {
            return null;
        }
        if (humanPlayerIdEntities.size() > 1) {
            logger.warning("More the one HumanPlayerIdEntity found for session id: " + sessionId + " HumanPlayerId: " + humanPlayerIdEntities);
        }
        return humanPlayerIdEntities.get(0).getId();
    }

    private int getPageHits(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
        Root<PageTrackerEntity> root = cq.from(PageTrackerEntity.class);
        cq.where(criteriaBuilder.equal(root.get(PageTrackerEntity_.sessionId), sessionId));
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
        query.where(criteriaBuilder.equal(root.get(SessionTrackerEntity_.sessionId), sessionId));
        SessionTrackerEntity sessionTrackerEntity = entityManager.createQuery(userSelect).getSingleResult();

        SessionDetail sessionDetail = new SessionDetail().setId(sessionTrackerEntity.getSessionId()).setTime(sessionTrackerEntity.getTimeStamp()).setUserAgent(sessionTrackerEntity.getUserAgent()).setReferer(sessionTrackerEntity.getReferer());
        sessionDetail.setFbAdRazTrack(getFbAdRazTrack(sessionId));
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
        query.where(criteriaBuilder.equal(root.get(PageTrackerEntity_.sessionId), sessionId));
        CriteriaQuery<PageTrackerEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getResultList().stream().map(PageTrackerEntity::toPageDetail).collect(Collectors.toList());
    }

    private List<PageDetail> readFrontendNavigationDetails(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FrontendNavigationEntity> query = criteriaBuilder.createQuery(FrontendNavigationEntity.class);
        Root<FrontendNavigationEntity> root = query.from(FrontendNavigationEntity.class);
        query.where(criteriaBuilder.equal(root.get(FrontendNavigationEntity_.sessionId), sessionId));
        CriteriaQuery<FrontendNavigationEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getResultList().stream().map(FrontendNavigationEntity::toPageDetail).collect(Collectors.toList());
    }

    private List<PageDetail> readWindowCloseTrackerDetails(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<WindowCloseTrackerEntity> query = criteriaBuilder.createQuery(WindowCloseTrackerEntity.class);
        Root<WindowCloseTrackerEntity> root = query.from(WindowCloseTrackerEntity.class);
        query.where(criteriaBuilder.equal(root.get(WindowCloseTrackerEntity_.sessionId), sessionId));
        CriteriaQuery<WindowCloseTrackerEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getResultList().stream().map(WindowCloseTrackerEntity::toPageDetail).collect(Collectors.toList());
    }

    private List<GameSessionDetail> readGameSessionDetails(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<StartupTaskEntity> root = cq.from(StartupTaskEntity.class);
        cq.where(criteriaBuilder.equal(root.get(StartupTaskEntity_.sessionId), sessionId));
        cq.multiselect(root.get(StartupTaskEntity_.gameSessionUuid), root.get(StartupTaskEntity_.startTime), root.get(StartupTaskEntity_.clientStartTime));
        cq.groupBy(root.get(StartupTaskEntity_.gameSessionUuid));
        cq.orderBy(criteriaBuilder.asc(root.get(StartupTaskEntity_.clientStartTime)));
        return entityManager.createQuery(cq).getResultList().stream().map(tuple -> readGameSessionDetail(sessionId, (String) tuple.get(0), (Date) tuple.get(1), (Date) tuple.get(2))).collect(Collectors.toList());
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
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StartupTaskEntity> query = criteriaBuilder.createQuery(StartupTaskEntity.class);
        Root<StartupTaskEntity> root = query.from(StartupTaskEntity.class);
        query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(StartupTaskEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(StartupTaskEntity_.gameSessionUuid), gameSessionUuid));
        query.orderBy(criteriaBuilder.asc(root.get(StartupTaskEntity_.clientStartTime)));

        return entityManager.createQuery(query).getResultList().stream().map(StartupTaskEntity::toStartupTaskDetail).collect(Collectors.toList());
    }

    private StartupTerminatedDetail readStartupTerminatedDetail(String sessionId, String gameSessionUuid) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StartupTerminatedEntity> query = criteriaBuilder.createQuery(StartupTerminatedEntity.class);
        Root<StartupTerminatedEntity> root = query.from(StartupTerminatedEntity.class);
        query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(StartupTerminatedEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(StartupTerminatedEntity_.gameSessionUuid), gameSessionUuid));

        List<StartupTerminatedEntity> startupTerminatedEntities = entityManager.createQuery(query).getResultList();
        if (startupTerminatedEntities.isEmpty()) {
            return null;
        }
        if (startupTerminatedEntities.size() > 1) {
            logger.warning("More then one entry found for StartupTerminatedEntity. sessionId: " + sessionId + " gameSessionUuid: " + gameSessionUuid);
        }
        return startupTerminatedEntities.get(0).toStartupTerminatedDetail();
    }

    private List<SceneTrackerDetail> readSceneTrackerDetails(String sessionId, String gameSessionUuid) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SceneTrackerEntity> query = criteriaBuilder.createQuery(SceneTrackerEntity.class);
        Root<SceneTrackerEntity> root = query.from(SceneTrackerEntity.class);
        query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(SceneTrackerEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(SceneTrackerEntity_.gameSessionUuid), gameSessionUuid));
        query.orderBy(criteriaBuilder.asc(root.get(SceneTrackerEntity_.clientStartTime)));

        return entityManager.createQuery(query).getResultList().stream().map(SceneTrackerEntity::toSceneTrackerDetail).collect(Collectors.toList());
    }

    private List<PerfmonTrackerDetail> readPerfmonTrackerDetails(String sessionId, String gameSessionUuid) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PerfmonStatisticEntity> query = criteriaBuilder.createQuery(PerfmonStatisticEntity.class);
        Root<PerfmonStatisticEntity> root = query.from(PerfmonStatisticEntity.class);
        query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(PerfmonStatisticEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(PerfmonStatisticEntity_.gameSessionUuid), gameSessionUuid));
        query.orderBy(criteriaBuilder.asc(root.get(PerfmonStatisticEntity_.clientTimeStamp)));

        List<PerfmonTrackerDetail> perfmonTrackerDetails = new ArrayList<>();
        entityManager.createQuery(query).getResultList().forEach(perfmonStatisticEntity -> perfmonTrackerDetails.addAll(perfmonStatisticEntity.toPerfmonTrackerDetails()));
        perfmonTrackerDetails.sort(Comparator.comparing(PerfmonTrackerDetail::getClientStartTime));
        return perfmonTrackerDetails;
    }

    private List<PerfmonTerrainTileDetail> readPerfmonTerrainTileDetails(String sessionId, String gameSessionUuid) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TerrainTileStatisticEntity> query = criteriaBuilder.createQuery(TerrainTileStatisticEntity.class);
        Root<TerrainTileStatisticEntity> root = query.from(TerrainTileStatisticEntity.class);
        query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(TerrainTileStatisticEntity_.sessionId), sessionId)), criteriaBuilder.equal(root.get(TerrainTileStatisticEntity_.gameSessionUuid), gameSessionUuid));
        query.orderBy(criteriaBuilder.asc(root.get(TerrainTileStatisticEntity_.clientTimeStamp)));
        return entityManager.createQuery(query).getResultList().stream().map(TerrainTileStatisticEntity::toPerfmonTerrainTileDetail).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public WarmGameUiControlConfig setupWarmGameUiControlConfig(GameUiControlInput gameUiControlInput) {
        ServerTrackerStart serverTrackerStart = trackingContainerMongoDb.findServerTrackerStart(gameUiControlInput);

        PlanetEntity planetEntity = planetCrudPersistence.loadPlanet(serverTrackerStart.getTrackingStart().getPlanetId());
        WarmGameUiControlConfig warmGameUiControlConfig = new WarmGameUiControlConfig().setGameUiControlConfigId(-2).setGameEngineMode(GameEngineMode.PLAYBACK);
        warmGameUiControlConfig.setPlanetConfig(planetEntity.toPlanetConfig()).setPlanetVisualConfig(planetEntity.toPlanetVisualConfig());

        PlaybackGameUiControlConfig playbackGameUiControlConfig = new PlaybackGameUiControlConfig();
        playbackGameUiControlConfig.setTrackingStart(serverTrackerStart.getTrackingStart()).setTrackingContainer(trackingContainerMongoDb.findServerTrackingContainer(gameUiControlInput));
        warmGameUiControlConfig.setPlaybackGameUiControlConfig(playbackGameUiControlConfig);
        return warmGameUiControlConfig;
    }
}
