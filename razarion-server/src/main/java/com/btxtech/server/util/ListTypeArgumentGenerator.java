package com.btxtech.server.util;

import com.btxtech.shared.rest.CrudController;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ListTypeArgumentGenerator {
    private Map<String, Map<String, String>> listTypeArguments = new HashMap<>();
    private Set<Class> alreadyAnalyzed = new HashSet<>();

    public Map<String, Map<String, String>> generate() {
        Reflections reflection = new Reflections("com.btxtech");
        Set<Class<? extends CrudController>> crudControllers = reflection.getSubTypesOf(CrudController.class);

        crudControllers.forEach(controllerClass -> {
            Type[] genericSuperclass = controllerClass.getGenericInterfaces();
            if (genericSuperclass[0] instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass[0];
                Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                if (typeArgument instanceof Class) {
                    scanType((Class) typeArgument);
                }
            }
        });
        return listTypeArguments;
    }

    private void scanType(Class type) {
        if (alreadyAnalyzed.contains(type)) {
            return;
        }
        if (type.isPrimitive() || type.getPackage() == null || !type.getPackage().getName().startsWith("com.btxtech")) {
            return;
        }
        alreadyAnalyzed.add(type);
        List<Method> getters = Arrays.stream(type.getMethods())
                .filter(method -> method.getName().length() > 3)
                .filter(method -> method.getName().startsWith("get"))
                .filter(method -> Character.isUpperCase(method.getName().charAt(3)))
                .filter(method -> method.getReturnType() != Void.class)
                .collect(Collectors.toList());


        // http://tutorials.jenkov.com/java-reflection/generics.html
        getters.forEach(method -> {
            if (method.getReturnType().isAssignableFrom(List.class)) {
                String propertyName = method.getName().substring(3);
                propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    ParameterizedType genericType = (ParameterizedType) returnType;
                    Type[] typeArguments = genericType.getActualTypeArguments();
                    if (typeArguments == null) {
                        throw new IllegalArgumentException("List property '" + propertyName + "' in '" + type + "' has no generic type in List<???>.");
                    }
                    if (typeArguments.length != 1) {
                        throw new IllegalArgumentException("List property '" + propertyName + "' in '" + type + "' has an unexpected count (" + typeArguments.length + ")  of generic types in List<???>. ");
                    }
                    Class typeArgument = (Class) typeArguments[0];
                    add(type, propertyName, typeArgument);
                    scanType(typeArgument);
                } else {
                    throw new IllegalArgumentException("List property '" + propertyName + "' in '" + type + "' has no generic type in List<???>.");
                }
            } else {
                scanType(method.getReturnType());
            }
        });

    }

    private void add(Class type, String propertyName, Class typeArgument) {
        Map<String, String> listProperties = listTypeArguments.computeIfAbsent(type.getName(), className -> new HashMap<>());
        listProperties.put(propertyName, typeArgument.getName());
    }

}
