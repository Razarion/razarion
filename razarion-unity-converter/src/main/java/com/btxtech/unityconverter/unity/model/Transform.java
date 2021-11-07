package com.btxtech.unityconverter.unity.model;

import java.util.List;

public class Transform extends Component {
    private List<Reference> m_Children;
    private Reference m_CorrespondingSourceObject;

    public List<Reference> getM_Children() {
        return m_Children;
    }

    public void setM_Children(List<Reference> m_Children) {
        this.m_Children = m_Children;
    }

    public Reference getM_CorrespondingSourceObject() {
        return m_CorrespondingSourceObject;
    }

    public void setM_CorrespondingSourceObject(Reference m_CorrespondingSourceObject) {
        this.m_CorrespondingSourceObject = m_CorrespondingSourceObject;
    }

    @Override
    public String toString() {
        return "Transform{" +
                "m_Children=" + m_Children +
                ", m_CorrespondingSourceObject=" + m_CorrespondingSourceObject +
                '}';
    }
}
