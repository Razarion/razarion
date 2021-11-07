package com.btxtech.unityconverter.unity.model;

public class PrefabInstance extends Component {
    private ModificationContainer m_Modification;
    private Reference m_SourcePrefab;

    public ModificationContainer getM_Modification() {
        return m_Modification;
    }

    public void setM_Modification(ModificationContainer m_Modification) {
        this.m_Modification = m_Modification;
    }

    public Reference getM_SourcePrefab() {
        return m_SourcePrefab;
    }

    public void setM_SourcePrefab(Reference m_SourcePrefab) {
        this.m_SourcePrefab = m_SourcePrefab;
    }

    @Override
    public String toString() {
        return "PrefabInstance{" +
                "m_Modification=" + m_Modification +
                ", m_SourcePrefab=" + m_SourcePrefab +
                '}';
    }
}
