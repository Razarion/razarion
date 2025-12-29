package com.btxtech.server.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
//    private final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
//        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//        String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
//        String method = request.getMethod();
//        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
//        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
//
//        logger.warn("handleError: {} {} '{}' {} '{}'",
//                status,
//                method,
//                uri,
//                throwable != null ? throwable.getMessage() : "",
//                message,
//                throwable);

        return "error";
    }
}
