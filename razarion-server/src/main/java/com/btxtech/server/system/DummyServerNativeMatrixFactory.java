package com.btxtech.server.system;

import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixDto;
import com.btxtech.shared.nativejs.NativeMatrixFactory;

import javax.inject.Singleton;

/**
 * Created by Beat
 * on 13.01.2018.
 */
@Singleton
public class DummyServerNativeMatrixFactory extends NativeMatrixFactory {
    @Override
    public NativeMatrix createFromColumnMajorArray(double[] array) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public NativeMatrix createTranslation(double x, double y, double z) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public NativeMatrix createScale(double x, double y, double z) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public NativeMatrix createXRotation(double rad) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public NativeMatrix createYRotation(double rad) {
        throw new UnsupportedOperationException("Only works between worker and client");
    }

    @Override
    public NativeMatrix createZRotation(double rad) {
        throw new UnsupportedOperationException("Only works between worker and client");
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
}
