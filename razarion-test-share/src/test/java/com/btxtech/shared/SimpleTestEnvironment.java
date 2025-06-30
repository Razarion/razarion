package com.btxtech.shared;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Beat
 * 23.09.2016.
 */
public class SimpleTestEnvironment {
    public static void injectService(String fieldName, Object service, Class theClazz, Object serviceToInject) {
        try {
            Field field = theClazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, serviceToInject);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object readField(String fieldName, Object bean) {
        try {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object object = field.get(bean);
            field.setAccessible(false);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object callPrivateMethod(String methodName, Object bean, Class[] paramClasses, Object[] params) {
        try {
            Method method = bean.getClass().getDeclaredMethod(methodName, paramClasses);
            method.setAccessible(true);
            Object r = method.invoke(bean, params);
            method.setAccessible(false);
            return r;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
