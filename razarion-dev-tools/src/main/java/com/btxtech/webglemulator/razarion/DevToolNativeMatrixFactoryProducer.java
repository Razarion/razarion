package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.uiservice.nativejs.NativeMatrix;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Created by Beat
 * 28.03.2017.
 */
@ApplicationScoped
public class DevToolNativeMatrixFactoryProducer {
    private NativeMatrixFactory nativeMatrixFactory = new NativeMatrixFactory() {
        @Override
        public NativeMatrix createFromColumnMajorArray(double[] array) {
            return new DevToolNativeMatrix(Matrix4.fromColumnMajorOrder(array));
        }

        @Override
        public NativeMatrix createTranslation(double x, double y, double z) {
            return new DevToolNativeMatrix(Matrix4.createTranslation(x, y, z));
        }

        @Override
        public NativeMatrix createScale(double x, double y, double z) {
            return new DevToolNativeMatrix(Matrix4.createScale(x, y, z));
        }

        @Override
        public NativeMatrix createXRotation(double rad) {
            return new DevToolNativeMatrix(Matrix4.createXRotation(rad));
        }

        @Override
        public NativeMatrix createYRotation(double rad) {
            return new DevToolNativeMatrix(Matrix4.createYRotation(rad));
        }

        @Override
        public NativeMatrix createZRotation(double rad) {
            return new DevToolNativeMatrix(Matrix4.createZRotation(rad));
        }
    };

    @Produces
    public NativeMatrixFactory getNativeMatrixFactory() {
        return nativeMatrixFactory;
    }

    public static Matrix4 getMatrix(NativeMatrix nativeMatrix) {
        return ((DevToolNativeMatrix) nativeMatrix).getMatrix4();
    }

    private class DevToolNativeMatrix extends NativeMatrix {
        private Matrix4 matrix4;

        private DevToolNativeMatrix(Matrix4 matrix4) {
            this.matrix4 = matrix4;
        }

        @Override
        public DevToolNativeMatrix multiply(NativeMatrix other) {
            return new DevToolNativeMatrix(matrix4.multiply(((DevToolNativeMatrix) other).matrix4));
        }

        @Override
        public double[] toColumnMajorArray() {
            return matrix4.toWebGlArray();
        }

        @Override
        public DevToolNativeMatrix invert() {
            return new DevToolNativeMatrix(matrix4.invert());
        }

        @Override
        public DevToolNativeMatrix transpose() {
            return new DevToolNativeMatrix(matrix4.transpose());
        }

        @Override
        public NativeMatrixFactory getNativeMatrixFactory() {
            return nativeMatrixFactory;
        }

        public Matrix4 getMatrix4() {
            return matrix4;
        }

        @Override
        public String toString() {
            return matrix4.toString();
        }
    }
}
