package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by Beat
 * 31.07.2016.
 */
public class Rotate extends ColladaXml {
    private Matrix4 matrix4;

    public Rotate(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 4) {
            throw new ColladaRuntimeException("Rotation length must be 4. Current length: " + doubleList.size());
        }

        if (!MathHelper.compareWithPrecision(CollectionUtils.sum(doubleList.subList(0, 3)), 1.0)) {
            throw new IllegalArgumentException();
        }

        double radians = Math.toRadians(doubleList.get(3));
        if (doubleList.get(0) > 0.0) {
            matrix4 = Matrix4.createXRotation(radians);
        } else if (doubleList.get(1) > 0.0) {
            matrix4 = Matrix4.createYRotation(radians);
        } else if (doubleList.get(2) > 0.0) {
            matrix4 = Matrix4.createZRotation(radians);
        } else {
            throw new IllegalStateException();
        }
    }

    public Matrix4 getMatrix4() {
        return matrix4;
    }
}
