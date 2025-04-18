package com.btxtech.server.user;

// TODO import javax.interceptor.InterceptorBinding;

import java.lang.annotation.*;

/**
 * Created by Beat
 * 21.02.2017.
 */
@Inherited
// TODO @InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SecurityCheck {
}
