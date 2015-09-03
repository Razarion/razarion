package com.btxtech.client.dialogs;

import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Matrix4Builder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Matrix4BuilderView extends Composite {
    private static Matrix4ViewUiBinder uiBinder = GWT.create(Matrix4ViewUiBinder.class);
    @UiField
    InputNumber c1r1;
    @UiField
    InputNumber c2r1;
    @UiField
    InputNumber c3r1;
    @UiField
    InputNumber c4r1;
    @UiField
    InputNumber c1r2;
    @UiField
    InputNumber c2r2;
    @UiField
    InputNumber c3r2;
    @UiField
    InputNumber c4r2;
    @UiField
    InputNumber c1r3;
    @UiField
    InputNumber c2r3;
    @UiField
    InputNumber c3r3;
    @UiField
    InputNumber c4r3;
    @UiField
    InputNumber c1r4;
    @UiField
    InputNumber c2r4;
    @UiField
    InputNumber c3r4;
    @UiField
    InputNumber c4r4;
    @UiField
    InputNumber step;

    private Matrix4Builder matrix4Builder;
    private ChangeListener changeListener;

    interface Matrix4ViewUiBinder extends UiBinder<Widget, Matrix4BuilderView> {
    }

    public interface ChangeListener {
        void onMatrixChanged(Matrix4 matrix4);
    }

    public Matrix4BuilderView(Matrix4 matrix4, ChangeListener changeListener) {
        this.changeListener = changeListener;
        initWidget(uiBinder.createAndBindUi(this));
        setStep(step.getValue());
        setMatrix(matrix4);
    }

    public void setMatrix(Matrix4 matrix4) {
        matrix4Builder = new Matrix4Builder(matrix4);
        // Row 1
        c1r1.setValue(matrix4Builder.getNumber(0, 0));
        c2r1.setValue(matrix4Builder.getNumber(1, 0));
        c3r1.setValue(matrix4Builder.getNumber(2, 0));
        c4r1.setValue(matrix4Builder.getNumber(3, 0));
        // Row 2
        c1r2.setValue(matrix4Builder.getNumber(0, 1));
        c2r2.setValue(matrix4Builder.getNumber(1, 1));
        c3r2.setValue(matrix4Builder.getNumber(2, 1));
        c4r2.setValue(matrix4Builder.getNumber(3, 1));
        // Row 3
        c1r3.setValue(matrix4Builder.getNumber(0, 2));
        c2r3.setValue(matrix4Builder.getNumber(1, 2));
        c3r3.setValue(matrix4Builder.getNumber(2, 2));
        c4r3.setValue(matrix4Builder.getNumber(3, 2));
        // Row 4
        c1r4.setValue(matrix4Builder.getNumber(0, 3));
        c2r4.setValue(matrix4Builder.getNumber(1, 3));
        c3r4.setValue(matrix4Builder.getNumber(2, 3));
        c4r4.setValue(matrix4Builder.getNumber(3, 3));
    }

    private void fireMatrixChanged() {
        if (changeListener != null) {
            changeListener.onMatrixChanged(matrix4Builder.toMatrix4());
        }
    }

    @UiHandler("c1r1")
    void onFieldChangedC1r1(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(0, 0, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c2r1")
    void onFieldChangedC2r1(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(1, 0, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c3r1")
    void onFieldChangedC3r1(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(2, 0, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c4r1")
    void onFieldChangedC4r1(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(3, 0, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c1r2")
    void onFieldChangedC1r2(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(0, 1, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c2r2")
    void onFieldChangedC2r2(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(1, 1, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c3r2")
    void onFieldChangedC3r2(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(2, 1, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c4r2")
    void onFieldChangedC4r2(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(3, 1, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c1r3")
    void onFieldChangedC1r3(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(0, 2, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c2r3")
    void onFieldChangedC2r3(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(1, 2, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c3r3")
    void onFieldChangedC3r3(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(2, 2, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c4r3")
    void onFieldChangedC4r3(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(3, 2, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c1r4")
    void onFieldChangedC1r4(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(0, 3, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c2r4")
    void onFieldChangedC2r4(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(1, 3, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c3r4")
    void onFieldChangedC3r4(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(2, 3, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("c4r4")
    void onFieldChangedC4r4(ValueChangeEvent<Double> valueChangeEvent) {
        matrix4Builder.setNumber(3, 3, valueChangeEvent.getValue());
        fireMatrixChanged();
    }

    @UiHandler("step")
    void onStepFieldChanged(ValueChangeEvent<Double> valueChangeEvent) {
        double aDouble = valueChangeEvent.getValue();
        setStep(aDouble);

    }

    private void setStep(double aDouble) {
        // Row 1
        c1r1.setStep(aDouble);
        c2r1.setStep(aDouble);
        c3r1.setStep(aDouble);
        c4r1.setStep(aDouble);
        // Row 2
        c1r2.setStep(aDouble);
        c2r2.setStep(aDouble);
        c3r2.setStep(aDouble);
        c4r2.setStep(aDouble);
        // Row 3
        c1r3.setStep(aDouble);
        c2r3.setStep(aDouble);
        c3r3.setStep(aDouble);
        c4r3.setStep(aDouble);
        // Row 4
        c1r4.setStep(aDouble);
        c2r4.setStep(aDouble);
        c3r4.setStep(aDouble);
        c4r4.setStep(aDouble);
    }
}
