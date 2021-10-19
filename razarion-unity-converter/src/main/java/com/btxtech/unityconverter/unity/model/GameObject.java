package com.btxtech.unityconverter.unity.model;


import java.util.List;

public class GameObject extends UnityObject {
    private String m_Name;
    private List<ComponentReference> m_Component;

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

    @Override
    public String toString() {
        return "GameObject{" +
                "m_Name=" + m_Name +
                ", m_Component=" + m_Component +
                '}';
    }
}
