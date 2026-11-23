package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.shared.datatypes.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Date;
import java.util.Deque;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * "Save your progress" with a link by mail instead of a password.
 * <p>
 * An unregistered player is his cookie ({@link UserService#ANONYMOUS_COOKIE}): lose it - another
 * browser, another phone, the Facebook app's browser that keeps nothing - and the game is gone. This
 * ties the player to an address without a password and without keeping the address: the row stores a
 * fingerprint of it ({@link UserEntity#getEmailHash()}), which finds the player again and can do
 * nothing else. Nobody can be written to from it.
 * <p>
 * One request does both jobs. The player types an address; if no saved player has it yet, the link
 * saves the asking player under it; if one has, the link continues that player on the device that
 * opens it. Either way the answer to the page is the same, so the form says nothing about which
 * addresses are in use. Opening the link points the browser's cookie at the player
 * ({@link UserService#adoptPlayer}) - a saved player then stays unregistered in every other respect,
 * he is only never deleted and his cookie lives a year.
 * <p>
 * Requested by the user on 2026-11-17: no address and no name kept, nobody is written to.
 */
@Service
public class ProgressSaveService {
    public static final int LINK_VALID_MINUTES = 30;
    /** Salt of the fingerprint. Not a secret - it only keeps the hashes apart from plain SHA-256 tables. */
    private static final String EMAIL_HASH_SALT = "razarion-progress-v1:";
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MAX_EMAIL_LENGTH = 190;
    private static final long TEN_MINUTES = 10 * 60_000L;
    private static final long HOUR = 60 * 60_000L;
    private static final long DAY = 24 * HOUR;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final Logger logger = LoggerFactory.getLogger(ProgressSaveService.class);
    private final UserRepository userRepository;
    private final UserService userService;
    private final RegisterService registerService;
    private final UserActivityService userActivityService;
    private final String publicUrl;
    /** All mails together, whoever asks for them - see {@link #allowed}. */
    static final int MAX_OVERALL_PER_HOUR = 20;
    static final int MAX_OVERALL_PER_DAY = 60;
    static final int MAX_PER_PLAYER_PER_HOUR = 5;
    /** Mails asked for per address fingerprint, per client address and per player - see {@link #allowed}. */
    private final Map<String, Deque<Long>> requestsByEmail = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> requestsByIp = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> requestsByUser = new ConcurrentHashMap<>();
    private final Deque<Long> requestsOverall = new ArrayDeque<>();
    private long overallLimitLoggedAt;

    public enum RequestResult {
        SENT, INVALID_EMAIL, TOO_MANY, REGISTERED, FAILED,
        /** No running game behind the request: no player, or none with an open connection. */
        NOT_IN_GAME,
        /** The cap over all players is reached - see {@link #allowed}. */
        BUSY
    }

    public enum LinkResult {
        SAVED, SIGNED_IN, EXPIRED
    }

    public ProgressSaveService(UserRepository userRepository,
                               UserService userService,
                               RegisterService registerService,
                               UserActivityService userActivityService,
                               @Value("${razarion.public-url:https://www.razarion.com}") String publicUrl) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.registerService = registerService;
        this.userActivityService = userActivityService;
        this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
    }

    public static String hashEmail(String email) {
        return sha256Hex(EMAIL_HASH_SALT + normalize(email));
    }

    static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Mails a link to the address: to save the asking player, or to continue the player already
     * saved under it. Not one transaction - the mail is a slow SMTP round trip, see
     * {@link UserService#registerByEmail}; the link lands in one targeted update before it goes out.
     */
    public RequestResult requestLink(String email) {
        String normalized = normalize(email);
        if (normalized.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(normalized).matches()) {
            return RequestResult.INVALID_EMAIL;
        }
        // Only a player in a running game - the dialog is part of the game. Without this the request
        // created a player of its own and anyone could mail any address without ever playing.
        String askingUserId = userService.getUserIdFromContextOrNull();
        if (askingUserId == null || !userService.hasOpenSystemConnection(askingUserId)) {
            return RequestResult.NOT_IN_GAME;
        }
        UserEntity asking = userRepository.findByUserId(askingUserId).orElse(null);
        if (asking == null) {
            return RequestResult.NOT_IN_GAME;
        }
        String emailHash = hashEmail(normalized);
        Limit limit = allowed(emailHash, clientIp(), askingUserId);
        if (limit == Limit.OVERALL) {
            return RequestResult.BUSY;
        }
        if (limit != Limit.NONE) {
            return RequestResult.TOO_MANY;
        }
        if (asking.createRegisterState() != UserContext.RegisterState.UNREGISTERED) {
            // A password account is already safe, and its login is its email.
            return RequestResult.REGISTERED;
        }
        Optional<UserEntity> saved = userRepository.findByEmailHash(emailHash);
        boolean signIn = saved.isPresent() && !saved.get().getUserId().equals(askingUserId);
        String targetUserId = saved.map(UserEntity::getUserId).orElse(askingUserId);

        String token = newToken();
        userRepository.applyLoginLink(targetUserId,
                sha256Hex(token),
                new Date(System.currentTimeMillis() + LINK_VALID_MINUTES * 60_000L),
                saved.isPresent() ? null : emailHash);
        try {
            registerService.sendProgressLink(normalized, publicUrl + "/rest/progress/link/" + token, signIn);
        } catch (Exception e) {
            logger.error("Could not send the progress link for user {}", askingUserId, e);
            return RequestResult.FAILED;
        }
        userActivityService.onProgressLinkSent(askingUserId, signIn ? "SIGN_IN" : (saved.isPresent() ? "RESAVE" : "SAVE"));
        return RequestResult.SENT;
    }

    /**
     * Opens a link: a save link stores its fingerprint for good, and either kind makes this browser
     * the player the link belongs to.
     */
    public LinkResult useLink(String token) {
        if (token == null || token.isBlank() || token.length() > 100) {
            return LinkResult.EXPIRED;
        }
        UserEntity userEntity = userRepository.findByLoginLinkHash(sha256Hex(token)).orElse(null);
        if (userEntity == null || userEntity.getLoginLinkExpiry() == null
                || userEntity.getLoginLinkExpiry().getTime() < System.currentTimeMillis()) {
            return LinkResult.EXPIRED;
        }
        String userId = userEntity.getUserId();
        String pending = userEntity.getPendingEmailHash();
        boolean save = pending != null;
        String emailHash = userEntity.getEmailHash();
        if (save) {
            // The address may have been saved by somebody else since the mail went out.
            Optional<UserEntity> other = userRepository.findByEmailHash(pending);
            if (other.isPresent() && !other.get().getUserId().equals(userId)) {
                userRepository.applyLinkUsed(userId, emailHash, userEntity.getProgressSavedDate());
                return LinkResult.EXPIRED;
            }
            emailHash = pending;
        }
        Date savedDate = userEntity.getProgressSavedDate() != null ? userEntity.getProgressSavedDate() : new Date();
        userRepository.applyLinkUsed(userId, emailHash, savedDate);
        userService.adoptPlayer(userId);
        if (save) {
            userActivityService.onProgressSaved(userId, "EMAIL");
            logger.info("User {} saved his progress", userId);
            return LinkResult.SAVED;
        }
        userActivityService.onProgressSignIn(userId, "EMAIL");
        logger.info("User {} continues his saved progress on another browser", userId);
        return LinkResult.SIGNED_IN;
    }

    public boolean isSaved() {
        String userId = userService.getUserIdFromContextOrNull();
        return userId != null && userRepository.findByUserId(userId).map(u -> u.getProgressSavedDate() != null).orElse(false);
    }

    public void onOffered(String detail) {
        String userId = userService.getUserIdFromContextOrNull();
        if (userId != null) {
            userActivityService.onProgressSaveOffered(userId, detail);
        }
    }

    enum Limit {
        NONE, ADDRESS, IP, PLAYER, OVERALL
    }

    /**
     * Three mails per address in ten minutes and ten a day; twenty per client address and five per
     * player an hour; and {@link #MAX_OVERALL_PER_HOUR} / {@link #MAX_OVERALL_PER_DAY} over everybody.
     * In memory, which is enough for one pod.
     * <p>
     * The per-address limit keeps the form from flooding one inbox. The overall cap is what keeps
     * it from flooding many: every mail goes out from razarion.com through Zoho, and a run of mails
     * to strangers or made-up addresses brings bounces and spam reports - Zoho suspends a sender
     * with too many, and then the mails of real players stop as well. Real use is two to four a day
     * (11/2026), so the cap is never felt; and the client address alone does not hold, the
     * forwarded header is the client's to write and many addresses are cheap.
     */
    Limit allowed(String emailHash, String ip, String userId) {
        long now = System.currentTimeMillis();
        synchronized (this) {
            Deque<Long> byEmail = requestsByEmail.computeIfAbsent(emailHash, k -> new ArrayDeque<>());
            Deque<Long> byIp = ip != null ? requestsByIp.computeIfAbsent(ip, k -> new ArrayDeque<>()) : new ArrayDeque<>();
            Deque<Long> byUser = userId != null ? requestsByUser.computeIfAbsent(userId, k -> new ArrayDeque<>()) : new ArrayDeque<>();
            prune(byEmail, now - DAY);
            prune(byIp, now - HOUR);
            prune(byUser, now - HOUR);
            prune(requestsOverall, now - DAY);
            Limit limit = Limit.NONE;
            long overallLastHour = requestsOverall.stream().filter(t -> t > now - HOUR).count();
            if (overallLastHour >= MAX_OVERALL_PER_HOUR || requestsOverall.size() >= MAX_OVERALL_PER_DAY) {
                limit = Limit.OVERALL;
            } else if (byEmail.stream().filter(t -> t > now - TEN_MINUTES).count() >= 3 || byEmail.size() >= 10) {
                limit = Limit.ADDRESS;
            } else if (byIp.size() >= 20) {
                limit = Limit.IP;
            } else if (byUser.size() >= MAX_PER_PLAYER_PER_HOUR) {
                limit = Limit.PLAYER;
            }
            if (limit == Limit.NONE) {
                byEmail.addLast(now);
                byIp.addLast(now);
                byUser.addLast(now);
                requestsOverall.addLast(now);
            } else if (limit == Limit.OVERALL && now - overallLimitLoggedAt > HOUR) {
                // Once an hour: a flood would otherwise write a line per request.
                overallLimitLoggedAt = now;
                logger.warn("Save-progress mails are capped: {} in the last hour, {} in the last day - somebody may be using the form to send mails",
                        overallLastHour, requestsOverall.size());
            }
            requestsByEmail.values().removeIf(Deque::isEmpty);
            requestsByIp.values().removeIf(Deque::isEmpty);
            requestsByUser.values().removeIf(Deque::isEmpty);
            return limit;
        }
    }

    private static void prune(Deque<Long> times, long cutoff) {
        while (!times.isEmpty() && times.peekFirst() < cutoff) {
            times.pollFirst();
        }
    }

    private static String clientIp() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            return null;
        }
        HttpServletRequest request = attr.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
