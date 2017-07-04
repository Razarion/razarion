package com.btxtech.uiservice;

import java.lang.reflect.Field;

/**
 * Created by Beat
 * on 04.07.2017.
 */
public interface UiTestHelper {
    static void injectService(String fieldName, Object service, Object serviceToInject) {
        injectService(fieldName, service, service.getClass(), serviceToInject);
    }

    static void injectService(String fieldName, Object service, Class theClazz, Object serviceToInject) {
        try {
            Field field = theClazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, serviceToInject);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
