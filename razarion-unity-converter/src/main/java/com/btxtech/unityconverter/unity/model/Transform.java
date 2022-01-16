package com.btxtech.unityconverter.unity.model;

import java.util.List;

public class Transform extends Component {
    private List<Reference> m_Children;
    private Reference m_CorrespondingSourceObject;
    private Reference m_PrefabInstance;
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

    public static Transform copyTransforms(Transform source) {
        Transform result = new Transform();
        result.setM_LocalPosition(new UnityVector()
                .x(source.m_LocalPosition.getX())
                .y(source.m_LocalPosition.getY())
                .z(source.m_LocalPosition.getZ()));

        result.setM_LocalRotation(new UnityVector()
                .x(source.m_LocalRotation.getX())
                .y(source.m_LocalRotation.getY())
                .z(source.m_LocalRotation.getZ())
                .w(source.m_LocalRotation.getW()));

        result.setM_LocalScale(new UnityVector()
                .x(source.m_LocalScale.getX())
                .y(source.m_LocalScale.getY())
                .z(source.m_LocalScale.getZ()));

        return result;
    }

    public static Transform sumTransforms(Transform t1, Transform t2) {
        Transform result = new Transform();
        result.setM_LocalPosition(new UnityVector()
                .x(t1.m_LocalPosition.getX() + t2.m_LocalPosition.getX())
                .y(t1.m_LocalPosition.getY() + t2.m_LocalPosition.getY())
                .z(t1.m_LocalPosition.getZ() + t2.m_LocalPosition.getZ()));

        UnityVector baseQuaternion = new UnityVector()
                .x(t1.m_LocalRotation.getX())
                .y(t1.m_LocalRotation.getY())
                .z(t1.m_LocalRotation.getZ())
                .w(t1.m_LocalRotation.getW());
        baseQuaternion.quaternionMultiply(t2.m_LocalRotation);
        result.setM_LocalRotation(baseQuaternion);

        result.setM_LocalScale(new UnityVector()
                .x(t1.m_LocalScale.getX() * t2.m_LocalScale.getX())
                .y(t1.m_LocalScale.getY() * t2.m_LocalScale.getY())
                .z(t1.m_LocalScale.getZ() * t2.m_LocalScale.getZ()));

        return result;
    }

    @Override
    public String toString() {
        return "Transform{" +
                "m_Children=" + m_Children +
                ", m_CorrespondingSourceObject=" + m_CorrespondingSourceObject +
                ", m_PrefabInstance=" + m_PrefabInstance +
                ", m_LocalEulerAnglesHint=" + m_LocalEulerAnglesHint +
                ", m_LocalPosition=" + m_LocalPosition +
                ", m_LocalScale=" + m_LocalScale +
                ", m_LocalRotation=" + m_LocalRotation +
                '}';
    }
}
