package com.btxtech.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {
    @GetMapping("/")
    public String index(
            @RequestParam(required = false) String utm_campaign,
            @RequestParam(required = false) String utm_source,
            Model model) {
        StringBuilder qs = new StringBuilder();
        if (utm_campaign != null) {
            qs.append("utm_campaign=").append(utm_campaign);
        }
        if (utm_source != null) {
            if (!qs.isEmpty()) {
                qs.append("&");
            }
            qs.append("utm_source=").append(utm_source);
        }

        if (!qs.isEmpty()) {
            qs.insert(0, "?"); // führendes Fragezeichen
        }

        model.addAttribute("qs", qs.toString());
        return "index";
    }
}
