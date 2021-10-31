package com.btxtech.unityconverter.unity.model;

import java.util.List;

public class MeshRenderer extends Component {
    private List<Reference> m_Materials;

    public List<Reference> getM_Materials() {
        return m_Materials;
    }

    public void setM_Materials(List<Reference> m_Materials) {
        this.m_Materials = m_Materials;
    }

    @Override
    public String toString() {
        return "MeshRenderer{" +
                "m_Materials=" + m_Materials +
                '}';
    }
}
