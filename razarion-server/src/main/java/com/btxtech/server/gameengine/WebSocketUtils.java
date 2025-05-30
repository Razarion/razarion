package com.btxtech.server.gameengine;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.socket.WebSocketSession;

import static com.btxtech.server.AuthHandshakeInterceptor.JWT_BEARER_TOKEN;
import static com.btxtech.server.user.UserService.removeAnonymousAuthentication;

public class WebSocketUtils {
    public static Authentication getJwtFromWsSession(WebSocketSession wsSession, Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) {
        Authentication auth = null;
        Jwt jwt = (Jwt) wsSession.getAttributes().get(JWT_BEARER_TOKEN);
        if (jwt != null) {
            Authentication authentication = removeAnonymousAuthentication(jwtAuthenticationConverter.convert(jwt));
            if (authentication != null && authentication.isAuthenticated()) {
                auth = authentication;
            }
        }
        return auth;
    }

}
