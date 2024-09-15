package com.btxtech.server;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 23.09.2016.
 */
public class SimpleTestEnvironment {

    public static Ejector injectService(String fieldName, Object service, Object serviceToInject) {
        return injectService(fieldName, service, service.getClass(), serviceToInject);
    }

    public static Ejector injectService(String fieldName, Object service, Class theClazz, Object serviceToInject) {
        try {
            Class clazz = theClazz;
            if (service instanceof TargetInstanceProxy) {
                // Weld deproxy unproxy
                // In weld-core maven dependency
                // Do not put weld-core in prodcode or integration test code. Do not add to Arquillan
                TargetInstanceProxy targetInstanceProxy = (TargetInstanceProxy) service;
                // ? object = targetInstanceProxy.getTargetInstance();
                clazz = targetInstanceProxy.getTargetClass();
            }
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object originalService = field.get(service);
            field.set(service, serviceToInject);
            field.setAccessible(false);
            return new Ejector(fieldName, service, theClazz, originalService);
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

    public static Ejector injectInstance(String fieldName, Object object, Supplier getSupplier) {
        Class clazz = object.getClass();
        if (object instanceof TargetInstanceProxy) {
            // Weld deproxy unproxy
            // In weld-core maven dependency
            // Do not put weld-core in prodcode or integration test code. Do not add to Arquillan
            TargetInstanceProxy targetInstanceProxy = (TargetInstanceProxy) object;
            // ? object = targetInstanceProxy.getTargetInstance();
            clazz = targetInstanceProxy.getTargetClass();
        }
//        Instance instance = new Instance() {
//            @Override
//            public Instance select(Annotation... qualifiers) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Instance select(Class subtype, Annotation... qualifiers) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public boolean isUnsatisfied() {
//                return false;
//            }
//
//            @Override
//            public boolean isAmbiguous() {
//                return false;
//            }
//
//            @Override
//            public void destroy(Object instance) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Iterator iterator() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Object get() {
//                return getSupplier.get();
//            }
//        };
//
//        try {
//            Field field = clazz.getDeclaredField(fieldName);
//            field.setAccessible(true);
//            Object original = field.get(object);
//            field.set(object, instance);
//            field.setAccessible(false);
//            return new Ejector(fieldName, object, clazz, original);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        return null;
    }

    public static void injectInstance(String fieldName, Object object, Map<Class, Supplier> selectorSupplier) {
//        Instance instance = new Instance() {
//            @Override
//            public Instance select(Annotation... qualifiers) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Instance select(Class subtype, Annotation... qualifiers) {
//                Supplier getSupplier = selectorSupplier.get(subtype);
//                if (getSupplier == null) {
//                    throw new IllegalArgumentException("No supplier for: " + subtype);
//                }
//                return new Instance() {
//                    @Override
//                    public Instance select(Annotation... qualifiers) {
//                        throw new UnsupportedOperationException();
//                    }
//
//                    @Override
//                    public Instance select(Class subtype, Annotation... qualifiers) {
//                        throw new UnsupportedOperationException();
//                    }
//
//                    @Override
//                    public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
//                        throw new UnsupportedOperationException();
//                    }
//
//                    @Override
//                    public boolean isUnsatisfied() {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean isAmbiguous() {
//                        return false;
//                    }
//
//                    @Override
//                    public void destroy(Object instance) {
//                        throw new UnsupportedOperationException();
//                    }
//
//                    @Override
//                    public Iterator iterator() {
//                        throw new UnsupportedOperationException();
//                    }
//
//                    @Override
//                    public Object get() {
//                        return getSupplier.get();
//                    }
//                };
//            }
//
//            @Override
//            public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public boolean isUnsatisfied() {
//                return false;
//            }
//
//            @Override
//            public boolean isAmbiguous() {
//                return false;
//            }
//
//            @Override
//            public void destroy(Object instance) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Iterator iterator() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Object get() {
//                throw new UnsupportedOperationException();
//            }
//        };
//
//        try {
//            Field field = object.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            field.set(object, instance);
//            field.setAccessible(false);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

//    public SyncBaseItem createSimpleSyncBaseItem(PlayerBase playerBase) {
//        SyncBaseItem syncBaseItem = new SyncBaseItem();
//        syncBaseItem.init(1, SIMPLE_MOVABLE_ITEM_TYPE, new SyncPhysicalMovable(syncBaseItem, new PhysicalAreaConfig().setAngularVelocity(Math.toDegrees(30)).setSpeed(80.0).setMinTurnSpeed(10.0).setAcceleration(100.0).radius(20), new Vertex(0, 0, 0), new Vertex(0, 0, 1), 0.0, new DecimalPosition(0, 0)));
//        syncBaseItem.setup(playerBase, ItemLifecycle.ALIVE);
//        return syncBaseItem;
//    }

    public static class Ejector {
        private final String fieldName;
        private final Object service;
        private final Class theClazz;
        private final Object originalService;

        public Ejector(String fieldName, Object service, Class theClazz, Object originalService) {
            this.fieldName = fieldName;
            this.service = service;
            this.theClazz = theClazz;
            this.originalService = originalService;
        }

        public void eject() {
            injectService(fieldName, service, theClazz, originalService);
        }
    }
}
