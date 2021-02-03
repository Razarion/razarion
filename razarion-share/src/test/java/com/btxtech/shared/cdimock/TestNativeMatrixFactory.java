package com.btxtech.shared.cdimock;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.mocks.TestFloat32Array;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixDto;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.nativejs.NativeVertexDto;

import javax.inject.Singleton;

/**
 * Created by Beat
 * on 15.01.2018.
 */
@Singleton
public class TestNativeMatrixFactory extends NativeMatrixFactory {
    private static TestNativeMatrixFactory testNativeMatrixFactory;

    public TestNativeMatrixFactory() {
        testNativeMatrixFactory = this;
    }

    @Override
    public NativeMatrix createFromColumnMajorArray(double[] array) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public NativeMatrix createTranslation(double x, double y, double z) {
        return new TestNativeMatrix(Matrix4.createTranslation(x, y, z));
    }

    @Override
    public NativeMatrix createScale(double x, double y, double z) {
        return new TestNativeMatrix(Matrix4.createScale(x, y, z));
    }

    @Override
    public NativeMatrix createXRotation(double x) {
        return new TestNativeMatrix(Matrix4.createXRotation(x));
    }

    @Override
    public NativeMatrix createYRotation(double y) {
        return new TestNativeMatrix(Matrix4.createYRotation(y));
    }

    @Override
    public NativeMatrix createZRotation(double z) {
        return new TestNativeMatrix(Matrix4.createZRotation(z));
    }

    @Override
    public NativeMatrix createFromNativeMatrixDto(NativeMatrixDto nativeMatrixDto) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public NativeMatrixDto createNativeMatrixDtoColumnMajorArray(double[] array) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public int[] intArrayConverter(int[] ints) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    public static class TestNativeMatrix extends NativeMatrix {
        private Matrix4 matrix4;

        public TestNativeMatrix(Matrix4 matrix4) {
            this.matrix4 = matrix4;
        }

        @Override
        public NativeMatrix multiply(NativeMatrix other) {
            return new TestNativeMatrix(matrix4.multiply(((TestNativeMatrix) other).matrix4));
        }

        @Override
        public NativeVertexDto multiplyVertex(NativeVertexDto other, double w) {
            return NativeUtil.toNativeVertex(matrix4.multiply(NativeUtil.toVertex(other), w));
        }

        @Override
        public Float32ArrayEmu getColumnMajorFloat32Array() {
            return new TestFloat32Array().doubles(matrix4.toWebGlArray());
        }

        @Override
        public NativeMatrix invert() {
            return new TestNativeMatrix(matrix4.invert());
        }

        @Override
        public NativeMatrix transpose() {
            return new TestNativeMatrix(matrix4.transpose());
        }

        @Override
        public NativeMatrixFactory getNativeMatrixFactory() {
            return TestNativeMatrixFactory.testNativeMatrixFactory;
        }

        @Override
        public String toString() {
            return matrix4.toString();
        }

    }
}
