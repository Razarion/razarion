package com.btxtech.client.terrain;

import com.btxtech.shared.GroundConfigEntity;
import com.btxtech.shared.GroundHeightEntry;
import com.btxtech.shared.GroundSkeletonEntity;
import com.btxtech.shared.GroundSplattingEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 03.05.2016.
 */
public class GroundSkeletonFactory {
    public static void sculpt(GroundConfigEntity groundConfigEntity) {
        FractalField heightField = FractalField.createSaveFractalField(groundConfigEntity.getHeightXCount(), groundConfigEntity.getHeightYCount(), groundConfigEntity.getHeightFractalRoughness(), -groundConfigEntity.getHeightFractalShift() / 2.0, groundConfigEntity.getHeightFractalShift());
        List<GroundHeightEntry> groundHeightEntries = new ArrayList<>();
        for (int x = 0; x < groundConfigEntity.getHeightXCount(); x++) {
            for (int y = 0; y < groundConfigEntity.getHeightYCount(); y++) {
                groundHeightEntries.add(new GroundHeightEntry(x, y, heightField.getValue(x, y)));
            }
        }
        FractalField splattingField = FractalField.createSaveFractalField(groundConfigEntity.getSplattingXCount(), groundConfigEntity.getSplattingYCount(), groundConfigEntity.getSplattingFractalRoughness(), groundConfigEntity.getSplattingFractalMin(), groundConfigEntity.getSplattingFractalMax());
        List<GroundSplattingEntry> groundSplattingEntries = new ArrayList<>();
        for (int x = 0; x < groundConfigEntity.getSplattingXCount(); x++) {
            for (int y = 0; y < groundConfigEntity.getSplattingYCount(); y++) {
                groundSplattingEntries.add(new GroundSplattingEntry(x, y, splattingField.getValue(x, y)));
            }
        }
        GroundSkeletonEntity groundSkeletonEntity = groundConfigEntity.getGroundSkeletonEntity();
        if (groundSkeletonEntity == null) {
            groundSkeletonEntity = new GroundSkeletonEntity();
            groundConfigEntity.setGroundSkeletonEntity(groundSkeletonEntity);
        }

        groundSkeletonEntity.setValues(groundSplattingEntries, groundHeightEntries, groundConfigEntity);
    }
}
