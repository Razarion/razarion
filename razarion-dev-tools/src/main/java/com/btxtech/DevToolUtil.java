package com.btxtech;

import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

import java.lang.reflect.Field;

/**
 * Created by Beat
 * 31.03.2017.
 */
public interface DevToolUtil {
    static Object readServiceFiled(String fieldName, Object service) {
        try {
            if (ProxyObject.class.isInstance(service)) {
                TargetInstanceProxy targetInstanceProxy = (TargetInstanceProxy) service;
                service = targetInstanceProxy.getTargetInstance();
            }
            Field field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object filedValue = field.get(service);
            field.setAccessible(false);
            return filedValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
