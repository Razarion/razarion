package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.datatypes.InGameItemVisualization;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: beat
 * Date: 21.08.12
 * Time: 22:51
 */
public abstract class InGameTipVisualization implements InGameItemVisualization, ViewService.ViewFieldListener {
    private static final int READY_CHECK_DELAY = 500;
    private List<Vertex> cornerVertices;
    private final double moveDistance;
    private final long duration;
    private Color cornerColor;
    private Integer shape3DId;
    private Integer outOfViewShape3DId;
    private boolean inViewFiled;
    private ViewField viewField;
    private long lastReadyCheck;
    private boolean ready;
    private NativeMatrixFactory nativeMatrixFactory;

    public InGameTipVisualization(double cornerLength, double moveDistance, long duration, Color cornerColor, Integer shape3DId, Integer outOfViewShape3DId, NativeMatrixFactory nativeMatrixFactory) {
        this.moveDistance = moveDistance;
        this.duration = duration;
        this.cornerColor = cornerColor;
        this.shape3DId = shape3DId;
        this.outOfViewShape3DId = outOfViewShape3DId;
        this.nativeMatrixFactory = nativeMatrixFactory;
        cornerVertices = setupCornerVertices(cornerLength);
    }

    abstract Vertex getPosition3D();

    abstract boolean hasPositionChanged();

    abstract boolean checkReady();

    abstract DecimalPosition getPosition2D();

    public void cleanup() {
        // Override in subclasses
    }

    public void preRender() {
        if (!ready) {
            if (lastReadyCheck + READY_CHECK_DELAY > System.currentTimeMillis()) {
                return;
            }
            if (checkReady()) {
                ready = true;
                checkInView();
            } else {
                lastReadyCheck = System.currentTimeMillis();
                return;
            }
        }

        ready = true;

        if (hasPositionChanged()) {
            checkInView();
        }
    }

    public List<ModelMatrices> provideCornerModelMatrices(long timeStamp) {
        if (!ready) {
            return null;
        }

        if (inViewFiled) {
            return createCornerModelMatrices(getPosition3D(), timeStamp);
        } else {
            return null;
        }
    }

    public List<ModelMatrices> provideShape3DModelMatrices() {
        if (!ready) {
            return null;
        }

        if (inViewFiled) {
            return Collections.singletonList(ModelMatrices.createFromPosition(getPosition3D(), nativeMatrixFactory));
        } else {
            return null;
        }
    }

    public List<ModelMatrices> provideOutOfViewShape3DModelMatrices() {
        if (!ready) {
            return null;
        }

        if (inViewFiled) {
            return null;
        } else {
            DecimalPosition center = viewField.calculateCenter();
            double angle = center.getAngle(getPosition2D());
            return Collections.singletonList(ModelMatrices.createFromPositionAndZRotation(center.getX(), center.getY(), 0, angle, nativeMatrixFactory));
        }
    }

    private List<ModelMatrices> createCornerModelMatrices(Vertex position, long timeStamp) {
        double distance = moveDistance - moveDistance * (double) (timeStamp % duration) / (double) duration;
        NativeMatrix positionMatrix = nativeMatrixFactory.createTranslation(position.getX(), position.getY(), position.getZ());
        NativeMatrix animationMatrix = nativeMatrixFactory.createTranslation(distance, -distance, 0);
        NativeMatrix matrix1 = positionMatrix.multiply(animationMatrix);
        NativeMatrix matrix2 = positionMatrix.multiply(nativeMatrixFactory.createZRotation(MathHelper.QUARTER_RADIANT).multiply(animationMatrix));
        NativeMatrix matrix3 = positionMatrix.multiply(nativeMatrixFactory.createZRotation(MathHelper.HALF_RADIANT).multiply(animationMatrix));
        NativeMatrix matrix4 = positionMatrix.multiply(nativeMatrixFactory.createZRotation(MathHelper.THREE_QUARTER_RADIANT).multiply(animationMatrix));

        List<ModelMatrices> result = new ArrayList<>();
        result.add(new ModelMatrices(matrix1));
        result.add(new ModelMatrices(matrix2));
        result.add(new ModelMatrices(matrix3));
        result.add(new ModelMatrices(matrix4));

        return result;
    }

    public Color getCornerColor() {
        return cornerColor;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    @Override
    public boolean hasShape3DId() {
        return true;
    }

    public Integer getOutOfViewShape3DId() {
        return outOfViewShape3DId;
    }

    public List<Vertex> getCornerVertices() {
        return cornerVertices;
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        this.viewField = viewField;
        checkInView();
    }

    private void checkInView() {
        if (ready) {
            inViewFiled = viewField.isInside(getPosition2D());
        }
    }
}
