package com.btxtech.shared;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 23.09.2016.
 */
public class SimpleTestEnvironment {
    private final static ExceptionHandler exceptionHandler = new ExceptionHandler() {
        @Override
        protected void handleExceptionInternal(String message, Throwable t) {
            System.out.println("ExceptionHandler from com.btxtech.share.SimpleTestEnvironment: " + message);
            if (t != null) {
                t.printStackTrace();
            }

        }
    };

    public static void injectExceptionHandler(Object service) {
        try {
            Field field = service.getClass().getDeclaredField("exceptionHandler");
            field.setAccessible(true);
            field.set(service, exceptionHandler);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void injectJsInteropObjectFactory(String fieldName, Object service, JsInteropObjectFactory jsInteropObjectFactory) {
        try {
            Field field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, jsInteropObjectFactory);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void injectService(String fieldName, Object service, Object serviceToInject) {
        injectService(fieldName, service, service.getClass(), serviceToInject);
    }

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

    public static void injectInstance(String fieldName, Object object, Supplier getSupplier) {
        Instance instance = new Instance() {
            @Override
            public Instance select(Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instance select(Class subtype, Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isUnsatisfied() {
                return false;
            }

            @Override
            public boolean isAmbiguous() {
                return false;
            }

            @Override
            public void destroy(Object instance) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object get() {
                return getSupplier.get();
            }
        };

        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, instance);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void injectInstance(String fieldName, Object object, Map<Class, Supplier> selectorSupplier) {
        Instance instance = new Instance() {
            @Override
            public Instance select(Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instance select(Class subtype, Annotation... qualifiers) {
                Supplier getSupplier = selectorSupplier.get(subtype);
                if (getSupplier == null) {
                    throw new IllegalArgumentException("No supplier for: " + subtype);
                }
                return new Instance() {
                    @Override
                    public Instance select(Annotation... qualifiers) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Instance select(Class subtype, Annotation... qualifiers) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isUnsatisfied() {
                        return false;
                    }

                    @Override
                    public boolean isAmbiguous() {
                        return false;
                    }

                    @Override
                    public void destroy(Object instance) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Iterator iterator() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Object get() {
                        return getSupplier.get();
                    }
                };
            }

            @Override
            public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isUnsatisfied() {
                return false;
            }

            @Override
            public boolean isAmbiguous() {
                return false;
            }

            @Override
            public void destroy(Object instance) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object get() {
                throw new UnsupportedOperationException();
            }
        };

        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, instance);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
