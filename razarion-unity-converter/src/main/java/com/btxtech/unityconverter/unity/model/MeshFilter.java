package com.btxtech.unityconverter.unity.model;

public class MeshFilter extends Component {
    private Reference m_Mesh;

    public Reference getM_Mesh() {
        return m_Mesh;
    }

    public void setM_Mesh(Reference m_Mesh) {
        this.m_Mesh = m_Mesh;
    }

    @Override
    public String toString() {
        return "MeshFilter{" +
                "m_Mesh=" + m_Mesh +
                '}';
    }
}
