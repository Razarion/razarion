package com.btxtech.server.clienthelper;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.core.Cookie;

/**
 * Created by Beat
 * on 11.02.2018.
 */
public class TestSessionContext {
    private String acceptLanguage;
    private Cookie sessionCookie;
    private Cookie loginTokenCookie;
    private ResteasyWebTarget target;

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public TestSessionContext setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    public Cookie getSessionCookie() {
        return sessionCookie;
    }

    public String getSessionId() {
        return sessionCookie.getValue().substring(0, sessionCookie.getValue().indexOf("."));
    }

    public void setSessionCookie(Cookie sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public Cookie getLoginTokenCookie() {
        return loginTokenCookie;
    }

    public void setLoginTokenCookie(Cookie loginTokenCookie) {
        this.loginTokenCookie = loginTokenCookie;
    }

    public void setTarget(ResteasyWebTarget target) {
        this.target = target;
    }

    public <T> T proxy(Class<T> proxyInterface) {
        return target.proxy(proxyInterface);
    }

    @Override
    public String toString() {
        return "TestSessionContext{" +
                "sessionCookie=" + sessionCookie +
                '}';
    }
}
