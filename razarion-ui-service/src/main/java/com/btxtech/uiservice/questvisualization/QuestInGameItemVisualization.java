package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.datatypes.InGameItemVisualization;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.item.AbstractSyncItemSetPositionMonitor;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.ViewService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 12.09.2017.
 */
@Dependent
public class QuestInGameItemVisualization implements InGameItemVisualization {
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private ViewService viewService;
    private List<Vertex> cornerVertices;
    private AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor;
    private Color color;
    private InGameQuestVisualConfig inGameQuestVisualConfig;

    public void init(Color color, InGameQuestVisualConfig inGameQuestVisualConfig, AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor) {
        this.color = color;
        this.inGameQuestVisualConfig = inGameQuestVisualConfig;
        cornerVertices = setupCornerVertices(inGameQuestVisualConfig.getCornerLength());
        this.syncItemSetPositionMonitor = syncItemSetPositionMonitor;
    }

    @Override
    public List<Vertex> getCornerVertices() {
        return cornerVertices;
    }

    @Override
    public Integer getOutOfViewShape3DId() {
        return inGameQuestVisualConfig.getOutOfViewShape3DId();
    }

    @Override
    public List<ModelMatrices> provideCornerModelMatrices(long timeStamp) {
        if (!syncItemSetPositionMonitor.hasInViewPositions()) {
            return null;
        }
        double distance = inGameQuestVisualConfig.getMoveDistance() * (1.0 - (double) (timeStamp % inGameQuestVisualConfig.getDuration()) / (double) inGameQuestVisualConfig.getDuration());
        List<ModelMatrices> result = new ArrayList<>();
        for (Vertex inViewPosition : syncItemSetPositionMonitor.getInViewPosition3d()) {
            NativeMatrix positionMatrix = nativeMatrixFactory.createTranslation(inViewPosition.getX(), inViewPosition.getY(), inViewPosition.getZ());
            NativeMatrix animationMatrix = nativeMatrixFactory.createTranslation(distance, -distance, 0);
            NativeMatrix matrix1 = positionMatrix.multiply(animationMatrix);
            NativeMatrix matrix2 = positionMatrix.multiply(nativeMatrixFactory.createZRotation(MathHelper.QUARTER_RADIANT).multiply(animationMatrix));
            NativeMatrix matrix3 = positionMatrix.multiply(nativeMatrixFactory.createZRotation(MathHelper.HALF_RADIANT).multiply(animationMatrix));
            NativeMatrix matrix4 = positionMatrix.multiply(nativeMatrixFactory.createZRotation(MathHelper.THREE_QUARTER_RADIANT).multiply(animationMatrix));

            result.add(new ModelMatrices(matrix1));
            result.add(new ModelMatrices(matrix2));
            result.add(new ModelMatrices(matrix3));
            result.add(new ModelMatrices(matrix4));
        }
        return result;
    }

    @Override
    public List<ModelMatrices> provideOutOfViewShape3DModelMatrices() {
        if (syncItemSetPositionMonitor.hasInViewPositions()) {
            return null;
        }
        DecimalPosition outOfViewPosition = syncItemSetPositionMonitor.getNearestOutOfViewPosition2d();
        if (outOfViewPosition == null) {
            return null;
        }
        DecimalPosition center = viewService.getCurrentViewField().calculateCenter();
        double angle = center.getAngle(outOfViewPosition);
        return Collections.singletonList(ModelMatrices.createFromPositionAndZRotation(center.getX(), center.getY(), 0, angle, nativeMatrixFactory));
    }

    @Override
    public Color getCornerColor() {
        return color;
    }

    public void releaseMonitor() {
        if (syncItemSetPositionMonitor != null) {
            syncItemSetPositionMonitor.release();
            syncItemSetPositionMonitor = null;
        }
    }

    public AbstractSyncItemSetPositionMonitor getSyncItemSetPositionMonitor() {
        return syncItemSetPositionMonitor;
    }
}
