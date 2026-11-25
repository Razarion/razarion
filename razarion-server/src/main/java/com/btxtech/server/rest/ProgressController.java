package com.btxtech.server.rest;

import com.btxtech.server.user.FacebookLoginService;
import com.btxtech.server.user.ProgressSaveService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * "Save your progress" for players without an account - see {@link ProgressSaveService}. Open to
 * everybody: the asking player is whoever the session and cookie say.
 */
@RestController
@RequestMapping("/rest/progress")
public class ProgressController {
    private final ProgressSaveService progressSaveService;
    private final FacebookLoginService facebookLoginService;
    private final String publicUrl;
    /**
     * Whether players are offered "Continue with Facebook" at all. Off by default, also with the
     * secret set: Meta grants public_profile to people outside the app's own roles only after a
     * business verification, which this project cannot pass (2026-11-22). Until then every player
     * who pressed the button landed on an error in Facebook's dialog and never came back - 25
     * attempts on PROD, not one saved. The rest of the Facebook code stays, including the data
     * deletion callback, for the day this is switched back on.
     */
    private final boolean facebookOffered;

    public ProgressController(ProgressSaveService progressSaveService,
                              FacebookLoginService facebookLoginService,
                              @org.springframework.beans.factory.annotation.Value("${razarion.public-url:https://www.razarion.com}") String publicUrl,
                              @org.springframework.beans.factory.annotation.Value("${razarion.facebook.login-offered:false}") boolean facebookOffered) {
        this.progressSaveService = progressSaveService;
        this.facebookLoginService = facebookLoginService;
        this.facebookOffered = facebookOffered;
        this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
    }

    /** Body {"email": "..."}; answers {"result": SENT | INVALID_EMAIL | TOO_MANY | REGISTERED | FAILED}. */
    @PostMapping("request-link")
    public Map<String, String> requestLink(@RequestBody Map<String, String> body) {
        return Map.of("result", progressSaveService.requestLink(body != null ? body.get("email") : null).name());
    }

    @GetMapping("state")
    public Map<String, Boolean> state() {
        return Map.of("saved", progressSaveService.isSaved(), "facebook", facebookOffered && facebookLoginService.isEnabled());
    }

    @PostMapping("offered")
    public void offered(@RequestParam(value = "where", required = false) String where) {
        progressSaveService.onOffered(where != null && where.length() <= 32 ? where : null);
    }

    /** The link from the mail. Lands on the game, which tells the player how it went. */
    @GetMapping("link/{token}")
    public ResponseEntity<Void> link(@PathVariable("token") String token) {
        ProgressSaveService.LinkUse use = progressSaveService.useLink(token);
        if (use.result() == ProgressSaveService.LinkResult.ACCOUNT) {
            // The login token rides in the fragment: the browser keeps it to itself, so it shows up
            // in no request, no server log and no referrer. The page stores it before it starts - see
            // account-link.ts in the frontend.
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/game?progress=account#account=" + use.appToken())
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .build();
        }
        String outcome = switch (use.result()) {
            case SAVED -> "saved";
            case SIGNED_IN -> "welcome";
            case EXPIRED, ACCOUNT -> "expired";
        };
        return toGame(outcome);
    }

    /** "Continue with Facebook": off to Facebook's login dialog. */
    @GetMapping("facebook/start")
    public ResponseEntity<Void> facebookStart() {
        String url = facebookOffered ? facebookLoginService.startUrl() : null;
        if (url == null) {
            return toGame("failed");
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    /** Back from Facebook, with a code or with an error when the player said no. */
    @GetMapping("facebook/callback")
    public ResponseEntity<Void> facebookCallback(@RequestParam(value = "code", required = false) String code,
                                                 @RequestParam(value = "state", required = false) String state,
                                                 @RequestParam(value = "error", required = false) String error) {
        String outcome = switch (facebookLoginService.callback(code, state, error)) {
            case SAVED -> "saved";
            case SIGNED_IN -> "welcome";
            case CANCELLED -> "cancelled";
            case FAILED -> "failed";
        };
        return toGame(outcome);
    }

    /**
     * Facebook's data deletion callback, entered in the app settings as
     * {@code https://www.razarion.com/rest/progress/facebook/data-deletion}.
     */
    @PostMapping(value = "facebook/data-deletion", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, String>> facebookDataDeletion(@RequestParam("signed_request") String signedRequest) {
        String code = facebookLoginService.deleteData(signedRequest);
        if (code == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(Map.of(
                "url", publicUrl + "/rest/progress/facebook/deletion-status?code=" + code,
                "confirmation_code", code));
    }

    @GetMapping(value = "facebook/deletion-status", produces = MediaType.TEXT_PLAIN_VALUE)
    public String facebookDeletionStatus(@RequestParam(value = "code", required = false) String code) {
        return "Razarion keeps no data from Facebook except a fingerprint of the app-scoped id that linked a game "
                + "to the account. That link has been removed (confirmation " + (code != null ? code.replaceAll("[^A-Za-z0-9_-]", "") : "-")
                + "). The game itself stays playable in the browser it was played in.";
    }

    private static ResponseEntity<Void> toGame(String outcome) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/game?progress=" + outcome)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }
}
