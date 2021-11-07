package com.btxtech.unityconverter.unity.model;

import java.util.List;

public class ModificationContainer {
    private List<Modification> m_Modifications;

    public List<Modification> getM_Modifications() {
        return m_Modifications;
    }

    public void setM_Modifications(List<Modification> m_Modifications) {
        this.m_Modifications = m_Modifications;
    }

    @Override
    public String toString() {
        return "ModificationContainer{" +
                "m_Modification=" + m_Modifications +
                '}';
    }
}
