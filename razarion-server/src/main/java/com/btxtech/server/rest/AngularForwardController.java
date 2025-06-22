package com.btxtech.server.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AngularForwardController {

    @GetMapping(value = {"/game/", "/game"}, produces = "text/html")
    public String forwardGame(HttpServletRequest request) {
        return "forward:/game/index.html";
    }
}
