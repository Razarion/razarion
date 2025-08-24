package com.btxtech.server.user;

import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.model.RegisterResult;
import com.btxtech.server.model.Roles;
import com.btxtech.server.model.SetNameError;
import com.btxtech.server.model.SetNameResult;
import com.btxtech.server.model.UserEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.LevelUnlockEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.engine.LevelCrudService;
import com.btxtech.server.service.engine.QuestConfigService;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.dto.UserBackendInfo;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.btxtech.server.service.PersistenceUtil.extractId;


@Service
public class UserService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final Duration checkIntervalRegisteredUser = Duration.ofMinutes(30);
    private final LevelCrudService levelCrudPersistence;
    private final Map<String, Instant> lastCheckedRegisteredUsers = new ConcurrentHashMap<>();
    private final Map<String, String> anonymousMap = new HashMap<>();
    private final UserRepository userRepository;
    private final ServerGameEngineService serverGameEngineCrudPersistence;
    private final QuestConfigService questConfigService;
    private final RegisterService registerService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    @Lazy
    private BaseItemService baseItemService;
    @Autowired
    @Lazy
    private ServerGameEngineControl serverGameEngineControl;
    @Autowired
    @Lazy
    private ClientGameConnectionService clientGameConnectionService;
    @Autowired
    @Lazy
    private ClientSystemConnectionService clientSystemConnectionService;

    public UserService(LevelCrudService levelCrudPersistence,
                       UserRepository userRepository,
                       ServerGameEngineService serverGameEngineCrudPersistence,
                       QuestConfigService questConfigService,
                       RegisterService registerService,
                       PasswordEncoder passwordEncoder) {
        this.levelCrudPersistence = levelCrudPersistence;
        this.userRepository = userRepository;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.questConfigService = questConfigService;
        this.registerService = registerService;
        this.passwordEncoder = passwordEncoder;
    }

    public static Authentication removeAnonymousAuthentication(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication;
    }

    private UserBackendInfo userEntity2UserBackendInfo(UserEntity userEntity) {
        var clientSystemConnection = clientSystemConnectionService.getClientSystemConnection(userEntity.getUserId());
        var clientGameConnection = clientGameConnectionService.getClientGameConnection(userEntity.getUserId());

        UserBackendInfo userBackendInfo = new UserBackendInfo()
                .name(userEntity.getName())
                .creationDate(userEntity.getCreationDate())
                .verificationStartedDate(userEntity.getVerificationStartedDate())
                .verificationDoneDate(userEntity.getVerificationDoneDate())
                .email(userEntity.getEmail())
                .userId(userEntity.getUserId())
                .levelId(extractId(userEntity.getLevel(), LevelEntity::getId))
                .levelNumber(extractId(userEntity.getLevel(), LevelEntity::getNumber))
                .xp(userEntity.getXp())
                .crystals(userEntity.getCrystals())
                .activeQuest(extractId(userEntity.getActiveQuest(), QuestConfigEntity::getId))
                .systemConnectionOpened(userEntity.getSystemConnectionOpened() != null ? Date.from(userEntity.getSystemConnectionOpened().atZone(ZoneId.systemDefault()).toInstant()) : null)
                .systemConnectionClosed(userEntity.getSystemConnectionClosed() != null ? Date.from(userEntity.getSystemConnectionClosed().atZone(ZoneId.systemDefault()).toInstant()) : null)
                .systemConnectionOpen(clientSystemConnection != null)
                .systemConnectionLastMessageSent(clientSystemConnection != null ? clientSystemConnection.getLastMessageSent() : null)
                .systemConnectionLastMessageReceived(clientSystemConnection != null ? clientSystemConnection.getLastMessageReceived() : null)
                .gameConnectionOpen(clientGameConnection != null)
                .gameConnectionLastMessageSent(clientGameConnection != null ? clientGameConnection.getLastMessageSent() : null)
                .gameConnectionLastMessageReceived(clientGameConnection != null ? clientGameConnection.getLastMessageReceived() : null);
        if (userEntity.getCompletedQuestIds() != null && !userEntity.getCompletedQuestIds().isEmpty()) {
            userBackendInfo.completedQuestIds(userEntity.getCompletedQuest().stream().map(QuestConfigEntity::getId).collect(Collectors.toList()));
        }
        if (userEntity.getLevelUnlockEntities() != null && !userEntity.getLevelUnlockEntities().isEmpty()) {
            userBackendInfo.unlockedIds(userEntity.getLevelUnlockEntities().stream().map(LevelUnlockEntity::getId).collect(Collectors.toList()));
        }
        return userBackendInfo;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userEntity = userRepository.findByEmail(username).orElseThrow();

        return new UserDetails() {

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                if (userEntity.isAdmin()) {
                    return List.of(new SimpleGrantedAuthority(Roles.toJwtRole(Roles.ADMIN)));
                } else {
                    return List.of();
                }
            }

            @Override
            public String getPassword() {
                return userEntity.getPasswordHash();
            }

            @Override
            public String getUsername() {
                return userEntity.getEmail();
            }
        };
    }

    @Transactional
    public String getUserIdByEmail(String email) {
        try {
            return userRepository.findByEmail(email)
                    .map(UserEntity::getUserId)
                    .orElseThrow(() -> new UsernameNotFoundException(email));
        } catch (UsernameNotFoundException e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public String getOrCreateUserIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication = removeAnonymousAuthentication(authentication);
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String httpSessionId = null;
        if (attr != null) {
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession(true);
            if (session != null) {
                httpSessionId = session.getId();
            }
        }
        if (authentication == null && httpSessionId == null) {
            throw new IllegalStateException("authentication and httpSessionId is null");
        }
        return getOrCreateUserId(authentication, httpSessionId);
    }

    @Transactional
    public String getOrCreateUserId(Authentication auth, String httpSessionId) {
        auth = removeAnonymousAuthentication(auth);
        if (auth != null) {
            return getUserIdByEmail(auth.getName());
        } else {
            String userId = anonymousMap.get(httpSessionId);
            if (userId != null) {
                return userId;
            }
            String anonymousUserId = createAnonymousUser();
            anonymousMap.put(httpSessionId, anonymousUserId);
            return anonymousUserId;
        }
    }

    private String createAnonymousUser() {
        var userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setLevel(levelCrudPersistence.getStarterLevel());
        userEntity.setCreationDate(new Date());
        return userRepository.save(userEntity).getUserId();
    }

    @Transactional
    public UserContext getUserContextFromContext() {
        var userId = getOrCreateUserIdFromContext();
        return getUserContext(userId);
    }

    public UserContext getUserContext(String userId) {
        return userRepository.findByUserId(userId).orElseThrow().toUserContext();
    }

    @Transactional
    public UserContext getUserContextTransactional(String userId) {
        return getUserContext(userId);
    }

    @Transactional
    public void persistLevel(String userId, LevelEntity newLevel) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setLevel(newLevel);
        userRepository.save(userEntity);
    }

    @Transactional
    public QuestConfig getAndSaveNewQuest(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        if (userEntity.getActiveQuest() == null) {
            QuestConfigEntity newQuest = serverGameEngineCrudPersistence.getQuest4LevelAndIgnoreCompleted(userEntity.getLevel(), userEntity.getCompletedQuestIds());
            userEntity.setActiveQuest(newQuest);
            userRepository.save(userEntity);
            if (newQuest != null) {
                return newQuest.toQuestConfig();
            }
        }
        return null;
    }

    @Transactional
    public void setActiveQuest(String userId, int questId) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setActiveQuest(questConfigService.getEntity(questId));
        userRepository.save(userEntity);
    }

    @Transactional
    public QuestConfig findActiveQuestConfig4CurrentUser(String userId) {
        return getActiveQuest(userId);
    }

    @Transactional
    public void persistXp(String userId, int xp) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setXp(xp);
        userRepository.save(userEntity);
    }

    @Transactional
    public void addCompletedServerQuest(String userId, QuestConfig questConfig) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.addCompletedQuest(questConfigService.getEntity(questConfig.getId()));
        userEntity.setActiveQuest(null);
        userRepository.save(userEntity);
    }

    @Transactional
    public List<Integer> findActivePassedQuestId(String userId) {
        List<Integer> ids = new ArrayList<>();
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        if (userEntity.getActiveQuest() != null) {
            ids.add(userEntity.getActiveQuest().getId());
        }
        List<Integer> completedIds = userEntity.getCompletedQuestIds();
        if (completedIds != null) {
            ids.addAll(completedIds);
        }
        return ids;
    }

    @Transactional
    public QuestConfig getActiveQuest(String userId) {
        QuestConfigEntity questConfigEntity = userRepository.findByUserId(userId).orElseThrow().getActiveQuest();
        if (questConfigEntity != null) {
            return questConfigEntity.toQuestConfig();
        }
        return null;
    }

    @Transactional
    public void clearActiveQuest(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setActiveQuest(null);
        userRepository.save(userEntity);
    }

    @Transactional
    public Collection<Integer> unlockedEntityIds(String userId) {
        var unlocks = userRepository.findByUserId(userId).orElseThrow().getLevelUnlockEntities();
        if (unlocks != null) {
            return unlocks.stream()
                    .map(LevelUnlockEntity::getId)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Transactional
    public void persistCrystals(String userId, int crystals) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setCrystals(crystals);
        userRepository.save(userEntity);
    }

    @Transactional
    public void setCompletedQuest(String userId, List<Integer> completedQuestIds) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setCompletedQuest(completedQuestIds.stream().map(questId -> {
            QuestConfigEntity questConfigEntity = questConfigService.getEntity(questId);
            if (questConfigEntity == null) {
                throw new IllegalArgumentException("No QuestConfigEntity for id: " + questId);
            }
            return questConfigEntity;
        }).collect(Collectors.toList()));
        userRepository.save(userEntity);
    }

    @Transactional
    public InventoryInfo readInventoryInfo(String userId) {
        return userRepository.findByUserId(userId).orElseThrow().toInventoryInfo();
    }

    @Transactional
    public int readCrystals(String userId) {
        return userRepository.findByUserId(userId).orElseThrow().getCrystals();
    }

    @Transactional
    public void persistUnlockViaCrystals(String userId, int levelUnlockEntityId) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        LevelUnlockEntity levelUnlockEntity = levelCrudPersistence.readLevelUnlockEntity(levelUnlockEntityId);
        if (levelUnlockEntity.getCrystalCost() > userEntity.getCrystals()) {
            throw new IllegalArgumentException("User does not have enough crystals to unlock LevelUnlockEntity. User id: " + userEntity.getId() + " LevelUnlockEntity id: " + levelUnlockEntity.getId());
        }
        userEntity.addLevelUnlockEntity(levelUnlockEntity);
        userEntity.removeCrystals(levelUnlockEntity.getCrystalCost());
    }

    @Transactional
    public List<UserBackendInfo> getUserBackendInfos() {
        return userRepository.findAll()
                .stream()
                .map(this::userEntity2UserBackendInfo)
                .collect(Collectors.toList());
    }

    public boolean shouldCheckRegisteredUser(String email) {
        return Duration.between(lastCheckedRegisteredUsers.getOrDefault(email, Instant.EPOCH), Instant.now())
                .compareTo(checkIntervalRegisteredUser) > 0;
    }

    public boolean registeredUserExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void updateLastCheckedRegisteredUser(String email) {
        lastCheckedRegisteredUsers.put(email, Instant.now());
    }

    @Transactional
    public void onClientSystemConnectionOpened(String userId) {
        var userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setSystemConnectionOpened(LocalDateTime.now());
        userEntity.setSystemConnectionClosed(null);
        userRepository.save(userEntity);
    }

    @Transactional
    public void onClientSystemConnectionClosed(String userId) {
        var userEntity = userRepository.findByUserId(userId).orElse(null);
        if (userEntity == null) {
            // User may be deleted
            return;
        }
        userEntity.setSystemConnectionClosed(LocalDateTime.now());
        userRepository.save(userEntity);
    }

    @Transactional
    public RegisterResult registerByEmail(String email, String password) {
        if (email == null || email.isEmpty()) {
            return RegisterResult.INVALID_EMAIL;
        }
        if (password == null || password.isEmpty()) {
            return RegisterResult.INVALID_PASSWORD;
        }
        var userId = getOrCreateUserIdFromContext();
        var userEntity = userRepository.findByUserId(userId).orElseThrow();
        if (userEntity.getEmail() != null) {
            logger.warn("User hase already an email. UserId {} email {}", userId, email);
        }
        SetNameError setNameError = verifyEmail(email);
        if (setNameError != null) {
            switch (setNameError) {
                case TO_SHORT:
                    return RegisterResult.INVALID_EMAIL;
                case ALREADY_USED:
                    return RegisterResult.EMAIL_ALREADY_USED;
                case UNKNOWN_ERROR:
                    return RegisterResult.UNKNOWN_ERROR;
                default:
                    logger.warn("verifyEmail(email): {}. Unknown result: {}", email, setNameError);
            }
        }
        // TODO historyPersistence.get().onUserLoggedIn(userEntity, sessionHolder.getPlayerSession().getHttpSessionId());
        userEntity.setEmail(email);
        userEntity.setPasswordHash(passwordEncoder.encode(password));
        registerService.startEmailVerifyingProcess(userEntity);
        userRepository.save(userEntity);
        return RegisterResult.OK;
    }

    @Transactional
    public SetNameError verifyEmail(String email) {
        if (email == null || email.isEmpty()) {
            return SetNameError.TO_SHORT;
        }
        return userRepository.findByEmail(email)
                .map(userEntity -> SetNameError.ALREADY_USED)
                .orElse(null);
    }

    @Transactional
    public void mgmtDeleteUnregisteredUser(String userId) {
        userRepository.findByUserId(userId).ifPresent(userEntity -> {
            if (userEntity.createRegisterState() == UserContext.RegisterState.UNREGISTERED) {
                userRepository.delete(userEntity);
            }
        });
    }

    @Transactional
    public void cleanupUnregisteredUsersStartup() {
        userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.createRegisterState() == UserContext.RegisterState.UNREGISTERED)
                .forEach(userEntity -> {
                    logger.info("Removing unregistered user startup: {}", userEntity);
                    userRepository.delete(userEntity);
                });
    }

    @Transactional
    public boolean verifyEmailVerificationId(String verificationId) {
        List<UserEntity> optionalUser = userRepository.findPendingVerificationUsers(verificationId);

        if (optionalUser.isEmpty()) {
            logger.warn("No user {} for email verification id", verificationId);
            return false;
        }

        if (optionalUser.size() > 1) {
            logger.warn("More then one User for email verification id {}. Using first user", verificationId);
        }

        UserEntity userEntity = optionalUser.get(0);

        userEntity.setVerifiedDone();
        userRepository.save(userEntity);
        return true;
    }

    @Transactional
    public void deleteUser() {
        var userId = getOrCreateUserIdFromContext();
        deleteBaseByUser(userId);
        userRepository.delete(userRepository.findByUserId(userId).orElseThrow());
        SecurityContextHolder.clearContext();
        logger.info("User deleted: {}", userId);
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cleanupDisconnectedUnregisteredUsers() {
        var cutoff = LocalDateTime.now().minusMinutes(240);
        userRepository.findInactiveSince(cutoff)
                .stream()
                .filter(userEntity -> userEntity.createRegisterState() == UserContext.RegisterState.UNREGISTERED)
                .forEach(userEntity -> {
                    logger.info("Removing user: {}", userEntity);
                    deleteBaseByUser(userEntity.getUserId());
                    userRepository.delete(userEntity);
                });
    }


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupUnverifiedUsersAfter24h() {
        var cutoff = LocalDateTime.now().minusHours(24);
        List<UserEntity> oldUnverified = userRepository.findUnverifiedUsersOlderThan(cutoff);

        for (UserEntity user : oldUnverified) {
            logger.info("Deleting unverified user older than 24h: {}", user);
            userRepository.delete(user);
        }
    }


    private void deleteBaseByUser(String userId) {
        var playerBase = baseItemService.getPlayerBase4UserId(userId);
        if (playerBase != null) {
            baseItemService.deleteBase(playerBase.getBaseId());
        }
    }

    @Transactional
    public SetNameResult setName(String name) {
        SetNameError setNameError = verifySetName(name);
        if (setNameError != null) {
            return new SetNameResult().errorResult(setNameError);
        }
        var userId = getOrCreateUserIdFromContext();
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow();
        userEntity.setName(name);
        userRepository.save(userEntity);
        serverGameEngineControl.updateUserName(userId, name);
        return new SetNameResult().userName(name);
    }

    @Transactional
    public SetNameError verifySetName(String name) {
        if (name == null || name.isEmpty()) {
            return SetNameError.TO_SHORT;
        }

        if (name.length() < 3) {
            return SetNameError.TO_SHORT;
        }
        UserContext userContext = getUserContextFromContext();
        if (!userContext.registered()) {
            throw new IllegalStateException("Only registered user chan set a name: " + userContext);
        }
        if (userContext.emailNotVerified()) {
            throw new IllegalStateException("Only email verified user can set a name: " + userContext);
        }
        if (userContext.checkName()) {
            throw new IllegalStateException("The name has already been set: " + userContext);
        }

        if (userRepository.existsByNameIgnoreCase(name)) {
            return SetNameError.ALREADY_USED;
        }

        return null;
    }
}
