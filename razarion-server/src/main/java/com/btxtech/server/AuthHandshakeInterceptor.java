package com.btxtech.server;

import com.btxtech.server.user.UserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    public static final String JWT_BEARER_TOKEN = "SPRING_SECURITY_AUTH";
    public static final String ANONYMOUS_TOKEN = "RAZARION_ANONYMOUS_TOKEN";
    private final JwtDecoder jwtDecoder;

    public AuthHandshakeInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        var servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        var bearerToken = servletRequest.getParameter("token");
        if (bearerToken != null && !bearerToken.trim().isEmpty()) {
            Jwt jwt = jwtDecoder.decode(bearerToken);
            attributes.put(JWT_BEARER_TOKEN, jwt);
        }
        var anonymousToken = UserService.readAnonymousCookie(servletRequest);
        if (anonymousToken != null) {
            attributes.put(ANONYMOUS_TOKEN, anonymousToken);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
