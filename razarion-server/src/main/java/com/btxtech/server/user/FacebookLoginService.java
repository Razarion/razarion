package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.shared.datatypes.UserContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;

/**
 * "Continue with Facebook" in the "Save progress" dialog: the OAuth 2 code flow, server side.
 * <p>
 * Most players come from a Facebook ad and sit in Facebook's own browser, logged in - for them this
 * is one tap, no address to type. Only {@code public_profile} is asked for and only the app-scoped id
 * is read from it (no name, no address: the user's wish of 2026-11-17), and even that is kept as a
 * fingerprint ({@link UserEntity#getFacebookIdHash()}). Like the mail link it either saves the player
 * of this browser or, when the Facebook account saved somebody already, continues that player here -
 * see {@link ProgressSaveService} for the rest of the picture.
 * <p>
 * A redirect rather than Facebook's JavaScript SDK: popups are unreliable in the in-app browsers this
 * is mostly for, and the SDK would be one more script on the game page. The game is left for a moment;
 * nothing is lost, the progress is on the server.
 * <p>
 * Off until {@code razarion.facebook.app-id} and {@code .app-secret} are set - the dialog then simply
 * shows no button.
 */
@Service
public class FacebookLoginService {
    private static final String STATE_ATTRIBUTE = "raz.facebookLoginState";
    private static final long STATE_VALID_MILLIS = 15 * 60_000L;
    private static final String ID_HASH_SALT = "razarion-facebook-v1:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final Logger logger = LoggerFactory.getLogger(FacebookLoginService.class);
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserActivityService userActivityService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String appId;
    private final String appSecret;
    private final String apiVersion;
    private final String dialogUrl;
    private final String graphUrl;
    private final String publicUrl;

    public enum CallbackResult {
        SAVED, SIGNED_IN, CANCELLED, FAILED
    }

    /** Asks Facebook who logged in: the app-scoped id for an authorisation code. A seam for tests. */
    public interface Graph {
        String appScopedUserId(String code, String redirectUri) throws Exception;
    }

    private Graph graph = this::appScopedUserIdFromFacebook;

    public FacebookLoginService(UserRepository userRepository,
                                UserService userService,
                                UserActivityService userActivityService,
                                @Value("${razarion.facebook.app-id:}") String appId,
                                @Value("${razarion.facebook.app-secret:}") String appSecret,
                                @Value("${razarion.facebook.api-version:v21.0}") String apiVersion,
                                @Value("${razarion.facebook.dialog-url:https://www.facebook.com}") String dialogUrl,
                                @Value("${razarion.facebook.graph-url:https://graph.facebook.com}") String graphUrl,
                                @Value("${razarion.public-url:https://www.razarion.com}") String publicUrl) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userActivityService = userActivityService;
        this.appId = appId == null ? "" : appId.trim();
        this.appSecret = appSecret == null ? "" : appSecret.trim();
        this.apiVersion = apiVersion;
        this.dialogUrl = stripSlash(dialogUrl);
        this.graphUrl = stripSlash(graphUrl);
        this.publicUrl = stripSlash(publicUrl);
    }

    void setGraph(Graph graph) {
        this.graph = graph;
    }

    public boolean isEnabled() {
        return !appId.isEmpty() && !appSecret.isEmpty();
    }

    public String redirectUri() {
        return publicUrl + "/rest/progress/facebook/callback";
    }

    /** Where the browser goes to log in: Facebook's dialog, with a state bound to this http session. */
    public String startUrl() {
        if (!isEnabled()) {
            return null;
        }
        String state = newToken();
        HttpSession session = currentSession();
        session.setAttribute(STATE_ATTRIBUTE, state + ":" + System.currentTimeMillis());
        String userId = userService.getOrCreateUserIdFromContext();
        userActivityService.onProgressFacebookStarted(userId);
        return dialogUrl + "/" + apiVersion + "/dialog/oauth"
                + "?client_id=" + encode(appId)
                + "&redirect_uri=" + encode(redirectUri())
                + "&state=" + encode(state)
                + "&response_type=code"
                + "&scope=public_profile";
    }

    public CallbackResult callback(String code, String state, String error) {
        HttpSession session = currentSession();
        Object expected = session.getAttribute(STATE_ATTRIBUTE);
        session.removeAttribute(STATE_ATTRIBUTE);
        if (error != null || code == null) {
            return CallbackResult.CANCELLED;
        }
        if (!isEnabled() || state == null || !(expected instanceof String expectedState) || !stateMatches(expectedState, state)) {
            logger.warn("Facebook login: state missing or wrong");
            return CallbackResult.FAILED;
        }
        String facebookId;
        try {
            facebookId = graph.appScopedUserId(code, redirectUri());
        } catch (Exception e) {
            logger.warn("Facebook login: could not read the user id", e);
            return CallbackResult.FAILED;
        }
        if (facebookId == null || facebookId.isBlank()) {
            return CallbackResult.FAILED;
        }
        String idHash = hashFacebookId(facebookId);
        String askingUserId = userService.getOrCreateUserIdFromContext();
        Optional<UserEntity> saved = userRepository.findByFacebookIdHash(idHash);
        if (saved.isPresent() && !saved.get().getUserId().equals(askingUserId)) {
            userService.adoptPlayer(saved.get().getUserId());
            userActivityService.onProgressSignIn(saved.get().getUserId(), "FACEBOOK");
            logger.info("User {} continues his saved progress through Facebook", saved.get().getUserId());
            return CallbackResult.SIGNED_IN;
        }
        UserEntity asking = userRepository.findByUserId(askingUserId).orElseThrow();
        if (asking.createRegisterState() != UserContext.RegisterState.UNREGISTERED) {
            return CallbackResult.FAILED;
        }
        Date savedDate = asking.getProgressSavedDate() != null ? asking.getProgressSavedDate() : new Date();
        userRepository.applyFacebookLink(askingUserId, idHash, savedDate);
        userService.adoptPlayer(askingUserId);
        userActivityService.onProgressSaved(askingUserId, "FACEBOOK");
        logger.info("User {} saved his progress through Facebook", askingUserId);
        return CallbackResult.SAVED;
    }

    /**
     * Facebook's data deletion callback (required for Facebook Login): a signed request naming the
     * app-scoped id. The only thing kept from Facebook is the fingerprint of that id, so the link to
     * the player is removed - the player and his game stay, reachable by cookie or mail link.
     *
     * @return a confirmation code, or {@code null} for a request that is not Facebook's
     */
    public String deleteData(String signedRequest) {
        String facebookId = verifiedUserId(signedRequest);
        if (facebookId == null) {
            return null;
        }
        int removed = userRepository.removeFacebookLink(hashFacebookId(facebookId));
        logger.info("Facebook data deletion: {} link(s) removed", removed);
        return newToken().substring(0, 16);
    }

    String verifiedUserId(String signedRequest) {
        if (!isEnabled() || signedRequest == null || !signedRequest.contains(".")) {
            return null;
        }
        try {
            String[] parts = signedRequest.split("\\.", 2);
            byte[] signature = Base64.getUrlDecoder().decode(pad(parts[0]));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(parts[1].getBytes(StandardCharsets.UTF_8));
            if (!MessageDigest.isEqual(signature, expected)) {
                return null;
            }
            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(pad(parts[1])));
            JsonNode userId = payload.get("user_id");
            return userId != null ? userId.asText() : null;
        } catch (Exception e) {
            logger.warn("Facebook data deletion: unreadable signed request", e);
            return null;
        }
    }

    String hashFacebookId(String facebookId) {
        return ProgressSaveService.sha256Hex(ID_HASH_SALT + appId + ":" + facebookId);
    }

    private String appScopedUserIdFromFacebook(String code, String redirectUri) throws Exception {
        JsonNode token = getJson(graphUrl + "/" + apiVersion + "/oauth/access_token"
                + "?client_id=" + encode(appId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&client_secret=" + encode(appSecret)
                + "&code=" + encode(code));
        String accessToken = token.path("access_token").asText(null);
        if (accessToken == null) {
            throw new IllegalStateException("No access token from Facebook");
        }
        JsonNode me = getJson(graphUrl + "/" + apiVersion + "/me?fields=id"
                + "&access_token=" + encode(accessToken)
                + "&appsecret_proof=" + encode(hmacHex(accessToken)));
        return me.path("id").asText(null);
    }

    private JsonNode getJson(String url) throws Exception {
        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            // The body names the problem; the URL would carry the secret, so it stays out of the log.
            throw new IllegalStateException("Facebook answered " + response.statusCode() + ": " + response.body());
        }
        return objectMapper.readTree(response.body());
    }

    private String hmacHex(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static boolean stateMatches(String stored, String state) {
        int colon = stored.lastIndexOf(':');
        if (colon < 0) {
            return false;
        }
        long issued;
        try {
            issued = Long.parseLong(stored.substring(colon + 1));
        } catch (NumberFormatException e) {
            return false;
        }
        return System.currentTimeMillis() - issued <= STATE_VALID_MILLIS
                && MessageDigest.isEqual(stored.substring(0, colon).getBytes(StandardCharsets.UTF_8), state.getBytes(StandardCharsets.UTF_8));
    }

    private static HttpSession currentSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true);
    }

    private static String newToken() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String pad(String base64) {
        int rest = base64.length() % 4;
        return rest == 0 ? base64 : base64 + "====".substring(rest);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String stripSlash(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
