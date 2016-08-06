package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
@Portable
public class ModelMatrixAnimation {
    public enum Axis {
        X, Y, Z
    }
    private String id;
    private Element3D element3D;
    private TransformationModification modification;
    private Axis axis;
    private List<TimeValueSample> timeValueSamples;
    private ItemState itemState;

    public String getId() {
        return id;
    }

    public ModelMatrixAnimation setId(String id) {
        this.id = id;
        return this;
    }

    public Element3D getElement3D() {
        return element3D;
    }

    public ModelMatrixAnimation setElement3D(Element3D element3D) {
        this.element3D = element3D;
        return this;
    }

    public TransformationModification getModification() {
        return modification;
    }

    public ModelMatrixAnimation setModification(TransformationModification modification) {
        this.modification = modification;
        return this;
    }

    public List<TimeValueSample> getTimeValueSamples() {
        return timeValueSamples;
    }

    public Axis getAxis() {
        return axis;
    }

    public ModelMatrixAnimation setAxis(Axis axis) {
        this.axis = axis;
        return this;
    }

    public ModelMatrixAnimation setTimeValueSamples(List<TimeValueSample> timeValueSamples) {
        this.timeValueSamples = timeValueSamples;
        return this;
    }

    public ItemState getItemState() {
        return itemState;
    }

    public ModelMatrixAnimation setItemState(ItemState itemState) {
        this.itemState = itemState;
        return this;
    }

    @Override
    public String toString() {
        return "ModelMatrixAnimation{" +
                "id=" + id +
                ", modification=" + modification +
                ", axis=" + axis +
                ", timeValueSamples=" + timeValueSamples +
                ", element3D=" + element3D +
                ", itemState=" + itemState +
                '}';
    }
}
