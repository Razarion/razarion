package com.btxtech.shared.dto;

public class DoubleSplattingConfig extends SplattingConfig {
    private double scale2;

    public double getScale2() {
        return scale2;
    }

    public void setScale2(double scale2) {
        this.scale2 = scale2;
    }

    public DoubleSplattingConfig scale2(double scale2) {
        setScale2(scale2);
        return this;
    }
}
