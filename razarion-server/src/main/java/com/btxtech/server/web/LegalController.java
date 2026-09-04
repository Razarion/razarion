package com.btxtech.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The two pages Google's OAuth consent screen points at. They are required to move the app out of
 * testing: without a reachable homepage and privacy policy URL the "publish" button stays disabled,
 * and the app verification that follows reads them as well.
 * <p>
 * Plain paths rather than files under /static, because these URLs are handed to a third party and
 * have to keep working across builds - a hashed or renamed asset would break the console entry.
 */
@Controller
public class LegalController {

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
}
