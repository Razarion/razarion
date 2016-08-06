package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 05.08.2016.
 */
public interface ShapeTransform {
    Matrix4 setupMatrix();

    Matrix4 setupNormMatrix();
}