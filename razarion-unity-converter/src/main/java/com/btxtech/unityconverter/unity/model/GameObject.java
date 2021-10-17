package com.btxtech.unityconverter.unity.model;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameObject extends UnityObject {
    private String m_Name;
    private List<ComponentReference> m_Component;
    private List<Component> components;

    public String getM_Name() {
        return m_Name;
    }

    public void setM_Name(String m_Name) {
        this.m_Name = m_Name;
    }

    public List<ComponentReference> getM_Component() {
        return m_Component;
    }

    public void setM_Component(List<ComponentReference> m_Component) {
        this.m_Component = m_Component;
    }

    public void resolveComponents(Map<String, UnityObject> unityObjects) {
        components = m_Component.stream()
                .map(ref -> unityObjects.get(ref))
                .map(unityObject -> (Component) unityObject)
                .collect(Collectors.toList());
    }

    public List<Component> getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return "GameObject{" +
                "m_Name=" + m_Name +
                ", m_Component=" + m_Component +
                ", components=" + components +
                '}';
    }
}
