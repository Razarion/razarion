package com.btxtech.server.rest;

import com.btxtech.server.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Start over as a new player in this browser - for testing inside an app's browser (Facebook,
 * Instagram), which has no developer tools to clear what the game keeps: the anonymous player's
 * cookie (http-only, out of reach of the page), the login token and the rest of localStorage.
 * <p>
 * GET shows a page with one button; only the button clears. It clears through the browser's own
 * Clear-Site-Data, and on top of that expires the player cookie, ends the http session and empties
 * the page's storage by script - whichever the browser honours. A saved player is not touched: his
 * link brings him back.
 * <p>
 * The button's POST is accepted only from this page (Sec-Fetch-Site same-origin): a form on
 * another site could otherwise wipe a visitor's unsaved game. A plain link to this address only
 * shows the page.
 */
@RestController
@RequestMapping("/rest/fresh-start")
public class FreshStartController {
    private static final Set<String> OWN_SITE = Set.of("same-origin", "none");
    private final Logger logger = LoggerFactory.getLogger(FreshStartController.class);
    private final UserService userService;

    public FreshStartController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> page() {
        return html(HttpStatus.OK, """
                <h1>Start over</h1>
                <p>Start Razarion in this browser as a brand-new player?</p>
                <p class="dim">The game played in this browser is forgotten here, and a sign-in is signed out.
                A game you saved by email stays saved: its link brings it back.</p>
                <form method="post" action="/rest/fresh-start">
                  <button type="submit">Start as a new player</button>
                </form>
                <p><a href="/game">Cancel, back to the game</a></p>
                """, false, false);
    }

    @PostMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> clear(HttpServletRequest request) {
        String site = request.getHeader("Sec-Fetch-Site");
        // No header at all: an old browser - let it through, a forged cross-site form carries one.
        if (site != null && !OWN_SITE.contains(site)) {
            logger.warn("Fresh start refused, request came from another site ({})", site);
            return html(HttpStatus.FORBIDDEN, "<h1>Not from here</h1><p><a href=\"/rest/fresh-start\">Open the page</a> and press the button there.</p>", false, false);
        }
        userService.forgetBrowser(request);
        logger.info("A browser started over as a new player (fresh start)");
        return html(HttpStatus.OK, """
                <h1>Done</h1>
                <p>This browser is a new player now. Starting the game ...</p>
                <p><a href="/game">Start the game</a></p>
                <script>
                  try { localStorage.clear(); } catch (e) {}
                  try { sessionStorage.clear(); } catch (e) {}
                  setTimeout(function () { location.replace('/game'); }, 1200);
                </script>
                """, true, request.isSecure());
    }

    private static ResponseEntity<String> html(HttpStatus status, String body, boolean clear, boolean secure) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status)
                .contentType(new MediaType(MediaType.TEXT_HTML, java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CACHE_CONTROL, "no-store");
        if (clear) {
            builder.header("Clear-Site-Data", "\"cookies\", \"storage\"")
                    .header(HttpHeaders.SET_COOKIE, ResponseCookie.from(UserService.ANONYMOUS_COOKIE, "")
                            .path("/").maxAge(0).httpOnly(true).secure(secure).sameSite("Lax").build().toString())
                    .header(HttpHeaders.SET_COOKIE, ResponseCookie.from("JSESSIONID", "")
                            .path("/").maxAge(0).httpOnly(true).build().toString());
        }
        return builder.body("""
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta name="robots" content="noindex">
                <title>Razarion - start over</title>
                <style>
                  body { margin: 0; padding: 24px 16px; background: #10151c; color: #e5e7eb; font: 16px/1.5 sans-serif; }
                  main { max-width: 420px; margin: 0 auto; }
                  h1 { font-size: 22px; margin: 0 0 12px; }
                  .dim { color: #9ca3af; font-size: 14px; }
                  button { width: 100%; padding: 14px; margin: 12px 0; border: 0; border-radius: 10px;
                           background: #34d399; color: #062018; font: 700 16px sans-serif; }
                  a { color: #93c5fd; }
                </style></head><body><main>
                """ + body + "</main></body></html>");
    }
}
