package com.btxtech.client.editor.fractal;

import com.btxtech.shared.dto.FractalFieldConfig;

/**
 * Created by Beat
 * on 19.01.2019.
 */
public class FractalDialogDto {
    private FractalFieldConfig fractalFieldConfig;
    private double[][] fractalField;

    public FractalFieldConfig getFractalFieldConfig() {
        return fractalFieldConfig;
    }

    public void setFractalFieldConfig(FractalFieldConfig fractalFieldConfig) {
        this.fractalFieldConfig = fractalFieldConfig;
    }

    public double[][] getFractalField() {
        return fractalField;
    }

    public void setFractalField(double[][] fractalField) {
        this.fractalField = fractalField;
    }
}
