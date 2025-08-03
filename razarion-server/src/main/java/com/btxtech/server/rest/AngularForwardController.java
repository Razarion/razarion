package com.btxtech.server.rest;

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
}
