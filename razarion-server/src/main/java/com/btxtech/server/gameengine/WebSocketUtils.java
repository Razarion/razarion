package com.btxtech.server.gameengine;

import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.socket.WebSocketSession;

import java.io.EOFException;
import java.io.IOException;

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

    /**
     * A player who simply leaves - tab closed, network gone, phone asleep - kills the TCP connection
     * without a close frame, which surfaces here as an EOFException or a broken pipe on the next read.
     * There is nothing to act on: the connection is gone either way, and the client reconnects on its
     * own. Logged as a one liner so the WARN level stays useful for transport errors that are real.
     */
    public static void logTransportError(Logger logger, WebSocketSession session, Throwable exception) {
        if (isClientGone(exception)) {
            logger.info("Websocket transport closed by peer. Session: {} Cause: {}", session.getId(), exception.toString());
        } else {
            logger.warn("handleTransportError. Session: {}", session, exception);
        }
    }

    private static boolean isClientGone(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof EOFException) {
                return true;
            }
            if (current instanceof IOException) {
                String message = current.getMessage();
                if (message != null && (message.contains("Broken pipe") || message.contains("Connection reset"))) {
                    return true;
                }
            }
            current = current.getCause() != current ? current.getCause() : null;
        }
        return false;
    }
}
