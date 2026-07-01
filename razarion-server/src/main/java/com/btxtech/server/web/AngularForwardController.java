package com.btxtech.server.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AngularForwardController {

    @GetMapping(value = {"/game/", "/game"}, produces = "text/html")
    public String forwardGame(HttpServletRequest request) {
        return "forward:/game/index.html";
    }

    @GetMapping(value = {"/verify-email/{verificationId}"}, produces = "text/html")
    public String forwardGameVerifyEmail(HttpServletRequest request, @PathVariable("verificationId") String verificationId) {
        return "forward:/game/index.html";
    }

    @GetMapping(value = {"/game/backend", "/game/backend/"}, produces = "text/html")
    public String forwardGameBackend(HttpServletRequest request) {
        return "forward:/game/index.html";
    }

    // Director mode (film the live world). Deep-link fallback so /game/director
    // serves the SPA shell, which then routes client-side to the director view.
    @GetMapping(value = {"/game/director", "/game/director/"}, produces = "text/html")
    public String forwardGameDirector(HttpServletRequest request) {
        return "forward:/game/index.html";
    }

    @GetMapping(value = {"/studio/", "/studio"}, produces = "text/html")
    public String forwardStudio(HttpServletRequest request) {
        return "forward:/studio/index.html";
    }
}
