package com.btxtech.server;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 23.09.2016.
 */
public class SimpleTestEnvironment {

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

//    public SyncBaseItem createSimpleSyncBaseItem(PlayerBase playerBase) {
//        SyncBaseItem syncBaseItem = new SyncBaseItem();
//        syncBaseItem.init(1, SIMPLE_MOVABLE_ITEM_TYPE, new SyncPhysicalMovable(syncBaseItem, new PhysicalAreaConfig().setAngularVelocity(Math.toDegrees(30)).setSpeed(80.0).setMinTurnSpeed(10.0).setAcceleration(100.0).setRadius(20), new Vertex(0, 0, 0), new Vertex(0, 0, 1), 0.0, new DecimalPosition(0, 0)));
//        syncBaseItem.setup(playerBase, ItemLifecycle.ALIVE);
//        return syncBaseItem;
//    }
}
