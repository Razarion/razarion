package com.btxtech.unityconverter.unity.model;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameObject extends UnityObject {
    private String name;
    private List<ComponentReference> componentReferences;
    private List<Component> components;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ComponentReference> getComponentReferences() {
        return componentReferences;
    }

    public void setComponentReferences(List<ComponentReference> componentReferences) {
        this.componentReferences = componentReferences;
    }

    public void resolveComponents(Map<String, UnityObject> unityObjects) {
        components = componentReferences.stream()
                .map(ref -> unityObjects.get(ref.getComponent().getFileID()))
                .map(unityObject -> (Component) unityObject)
                .collect(Collectors.toList());
    }

    public List<Component> getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return "GameObject{" +
                "name=" + name +
                ", componentReferences=" + componentReferences +
                ", components=" + components +
                '}';
    }
}
