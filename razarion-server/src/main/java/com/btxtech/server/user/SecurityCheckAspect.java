package com.btxtech.server.user;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

// TODO if remove -> remove also @EnableAspectJAutoProxy from com.btxtech.server.RazarionServerApplication
@Aspect
@Component
public class SecurityCheckAspect {
    @Before("@within(com.btxtech.server.user.SecurityCheck) || @annotation(com.btxtech.server.user.SecurityCheck)")
    public void checkSecurity() {
        throw new RuntimeException("Access denied");
    }

}
