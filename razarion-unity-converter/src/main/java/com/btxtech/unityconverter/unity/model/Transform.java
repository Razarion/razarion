package com.btxtech.unityconverter.unity.model;

import com.btxtech.shared.datatypes.shape.ShapeTransform;

import java.util.List;

public class Transform extends Component {
    private List<Reference> m_Children;
    private Reference m_CorrespondingSourceObject;
    private Reference m_PrefabInstance;
    private Reference m_Father;
    private UnityVector m_LocalEulerAnglesHint;
    private UnityVector m_LocalPosition;
    private UnityVector m_LocalScale;
    private UnityVector m_LocalRotation;

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

    public Reference getM_PrefabInstance() {
        return m_PrefabInstance;
    }

    public void setM_PrefabInstance(Reference m_PrefabInstance) {
        this.m_PrefabInstance = m_PrefabInstance;
    }

    public Reference getM_Father() {
        return m_Father;
    }

    public UnityVector getM_LocalEulerAnglesHint() {
        return m_LocalEulerAnglesHint;
    }

    public void setM_LocalEulerAnglesHint(UnityVector m_LocalEulerAnglesHint) {
        this.m_LocalEulerAnglesHint = m_LocalEulerAnglesHint;
    }

    public UnityVector getM_LocalPosition() {
        return m_LocalPosition;
    }

    public void setM_LocalPosition(UnityVector m_LocalPosition) {
        this.m_LocalPosition = m_LocalPosition;
    }

    public UnityVector getM_LocalScale() {
        return m_LocalScale;
    }

    public void setM_LocalScale(UnityVector m_LocalScale) {
        this.m_LocalScale = m_LocalScale;
    }

    public UnityVector getM_LocalRotation() {
        return m_LocalRotation;
    }

    public void setM_LocalRotation(UnityVector m_LocalRotation) {
        this.m_LocalRotation = m_LocalRotation;
    }

    public void setM_Father(Reference m_Father) {
        this.m_Father = m_Father;
    }

    public static ShapeTransform createShapeTransform(Transform source) {
        return new ShapeTransform()
                .translateX(source.m_LocalPosition.getX())
                .translateY(source.m_LocalPosition.getY())
                .translateZ(source.m_LocalPosition.getZ())
                .rotateX(source.m_LocalRotation.getX())
                .rotateY(source.m_LocalRotation.getY())
                .rotateZ(source.m_LocalRotation.getZ())
                .rotateW(source.m_LocalRotation.getW())
                .scaleX(source.m_LocalScale.getX())
                .scaleY(source.m_LocalScale.getY())
                .scaleZ(source.m_LocalScale.getZ());
    }

    @Override
    public String toString() {
        return "Transform{" +
                "m_Children=" + m_Children +
                ", m_CorrespondingSourceObject=" + m_CorrespondingSourceObject +
                ", m_PrefabInstance=" + m_PrefabInstance +
                ", m_Father=" + m_Father +
                ", m_LocalEulerAnglesHint=" + m_LocalEulerAnglesHint +
                ", m_LocalPosition=" + m_LocalPosition +
                ", m_LocalScale=" + m_LocalScale +
                ", m_LocalRotation=" + m_LocalRotation +
                '}';
    }
}
