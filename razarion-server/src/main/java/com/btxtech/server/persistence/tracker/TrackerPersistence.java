package com.btxtech.server.persistence.tracker;

import com.btxtech.server.marketing.facebook.FbFacade;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.web.SessionHolder;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 28.02.2017.
 */
@ApplicationScoped
public class TrackerPersistence {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private PlanetPersistence planetPersistence;
    @PersistenceContext
    private EntityManager entityManager;
    private TrackingContainerStore trackingContainerStore = new TrackingContainerStore();

    @Transactional
    public void onNewSession(HttpServletRequest request) {
        SessionTrackerEntity sessionTrackerEntity = new SessionTrackerEntity();
        sessionTrackerEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        sessionTrackerEntity.setUserAgent(request.getHeader("user-agent"));
        sessionTrackerEntity.setRemoteAddr(request.getRemoteAddr());
        sessionTrackerEntity.setReferer(request.getHeader("Referer"));
        sessionTrackerEntity.setLanguage(request.getHeader("Accept-Language"));
        sessionTrackerEntity.setTimeStamp(new Date());
        try {
            InetAddress inetAddress = InetAddress.getByName(request.getRemoteAddr());
            sessionTrackerEntity.setRemoteHost(inetAddress.getHostName());
        } catch (UnknownHostException e) {
            exceptionHandler.handleException(e);
        }
        entityManager.persist(sessionTrackerEntity);
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
    public void onPerformanceTracker(PerfmonStatistic perfmonStatistic) {
        PerfmonStatisticEntity fromPerfmonStatistic = new PerfmonStatisticEntity();
        fromPerfmonStatistic.fromPerfmonStatistic(sessionHolder.getPlayerSession().getHttpSessionId(), new Date(), perfmonStatistic);
        entityManager.persist(fromPerfmonStatistic);
    }

    public void onTrackingStart(String httpSessionId, TrackingStart trackingStart) {
        trackingContainerStore.onTrackingStart(httpSessionId, trackingStart);
    }

    public void onDetailedTracking(String sessionId, TrackingContainer trackingContainer) {
        trackingContainerStore.onDetailedTracking(sessionId, trackingContainer);
    }

    @Transactional
    @SecurityCheck
    public List<SessionTracker> readSessionTracking(SearchConfig searchConfig) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SessionTrackerEntity> query = criteriaBuilder.createQuery(SessionTrackerEntity.class);
        Root<SessionTrackerEntity> root = query.from(SessionTrackerEntity.class);
        CriteriaQuery<SessionTrackerEntity> userSelect = query.select(root);
        if (searchConfig.getFromDate() != null) {
            query.where(criteriaBuilder.greaterThanOrEqualTo(root.get(SessionTrackerEntity_.timeStamp), searchConfig.getFromDate()));
        }
        query.orderBy(criteriaBuilder.desc(root.get(SessionTrackerEntity_.timeStamp)));
        List<SessionTracker> sessionTrackers = new ArrayList<>();
        for (SessionTrackerEntity sessionTrackerEntity : entityManager.createQuery(userSelect).getResultList()) {
            sessionTrackers.add(sessionTrackerEntity.toSessionTracker().setFbAdRazTrack(getFbAdRazTrack(sessionTrackerEntity.getSessionId())));
        }
        return sessionTrackers;
    }

    @Transactional
    @SecurityCheck
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

    @Transactional
    @SecurityCheck
    public SessionDetail readSessionDetail(String sessionId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SessionTrackerEntity> query = criteriaBuilder.createQuery(SessionTrackerEntity.class);
        Root<SessionTrackerEntity> root = query.from(SessionTrackerEntity.class);
        CriteriaQuery<SessionTrackerEntity> userSelect = query.select(root);
        query.where(criteriaBuilder.equal(root.get(SessionTrackerEntity_.sessionId), sessionId));
        SessionTrackerEntity sessionTrackerEntity = entityManager.createQuery(userSelect).getSingleResult();

        SessionDetail sessionDetail = new SessionDetail().setId(sessionTrackerEntity.getSessionId()).setTime(sessionTrackerEntity.getTimeStamp()).setUserAgent(sessionTrackerEntity.getUserAgent());
        sessionDetail.setFbAdRazTrack(getFbAdRazTrack(sessionId));
        List<GameSessionDetail> gameSessionDetails = new ArrayList<>();
        for (ServerTrackingContainer serverTrackingContainer : trackingContainerStore.getServerTrackingContainers(sessionId)) {
            gameSessionDetails.add(new GameSessionDetail().setId(serverTrackingContainer.getGameSessionUuid()).setSessionId(sessionId).setTime(serverTrackingContainer.getTime()));
        }
        sessionDetail.setGameSessionDetails(gameSessionDetails);
        return sessionDetail;
    }

    @Transactional
    @SecurityCheck
    public WarmGameUiControlConfig setupWarmGameUiControlConfig(GameUiControlInput gameUiControlInput) {
        ServerTrackingContainer serverTrackingContainer = trackingContainerStore.getServerTrackingContainer(gameUiControlInput);
        PlanetEntity planetEntity = planetPersistence.loadPlanet(serverTrackingContainer.getPlanetId());

        WarmGameUiControlConfig warmGameUiControlConfig = new WarmGameUiControlConfig().setGameEngineMode(GameEngineMode.PLAYBACK);
        warmGameUiControlConfig.setPlanetConfig(planetEntity.toPlanetConfig()).setPlanetVisualConfig(planetEntity.toPlanetVisualConfig());

        PlaybackGameUiControlConfig playbackGameUiControlConfig = new PlaybackGameUiControlConfig();
        playbackGameUiControlConfig.setTrackingStart(serverTrackingContainer.getTrackingStart()).setTrackingContainer(serverTrackingContainer.generateTrackingContainer());
        warmGameUiControlConfig.setPlaybackGameUiControlConfig(playbackGameUiControlConfig);
        return warmGameUiControlConfig;
    }
}
