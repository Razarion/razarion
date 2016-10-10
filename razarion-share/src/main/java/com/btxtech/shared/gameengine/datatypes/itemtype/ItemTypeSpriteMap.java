package com.btxtech.shared.gameengine.datatypes.itemtype;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

import java.util.Collection;

/**
 * User: beat
 * Date: 01.08.12
 * Time: 13:43
 */
@Deprecated
public class ItemTypeSpriteMap {
    public enum SyncObjectState {
        BUILD_UP,
        RUN_TIME,
        DEMOLITION
    }

    private int imageWidth;
    private int imageHeight;
    private int buildupSteps;
    private int buildupAnimationFrames;
    private int buildupAnimationDuration;
    private int runtimeXOffset;
    private int runtimeAnimationFrames;
    private int runtimeAnimationDuration;
    private int demolitionXOffset;
    private DemolitionStepSpriteMap[] demolitionSteps;
    private int demolitionFramesPerAngel;
    private BoundingBox boundingBox;
    private Index cosmeticImageOffset;
    private int spriteWidth;
    private int spriteHeight;

    /**
     * Used by GWT
     */
    public ItemTypeSpriteMap() {
    }

    public ItemTypeSpriteMap(BoundingBox boundingBox,
                             int imageWidth,
                             int imageHeight,
                             int buildupSteps,
                             int buildupAnimationFrames,
                             int buildupAnimationDuration,
                             int runtimeAnimationFrames,
                             int runtimeAnimationDuration,
                             DemolitionStepSpriteMap[] demolitionSteps) {
        this.boundingBox = boundingBox;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.buildupSteps = buildupSteps;
        this.buildupAnimationFrames = buildupAnimationFrames;
        this.buildupAnimationDuration = buildupAnimationDuration;
        this.runtimeAnimationFrames = runtimeAnimationFrames;
        this.runtimeAnimationDuration = runtimeAnimationDuration;
        this.demolitionSteps = demolitionSteps;
        runtimeXOffset = imageWidth * buildupSteps * buildupAnimationFrames;
        demolitionXOffset = runtimeXOffset + imageWidth * boundingBox.getAngelCount() * runtimeAnimationFrames;
        cosmeticImageOffset = getRuntimeImageOffset(boundingBox.getCosmeticAngelIndex(), 0);
        setupDemolitionFramesPerAngel();
        spriteWidth = imageWidth * (buildupSteps * buildupAnimationFrames + boundingBox.getAngelCount() * runtimeAnimationFrames + boundingBox.getAngelCount() * demolitionFramesPerAngel);
        spriteHeight = imageHeight;
    }

    public SyncObjectState getSyncObjectState(SyncItem syncItem) {
        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (!syncBaseItem.isBuildup()) {
                if (buildupSteps > 0) {
                    return SyncObjectState.BUILD_UP;
                } else {
                    return SyncObjectState.RUN_TIME;
                }
            } else if (syncBaseItem.isHealthy()) {
                return SyncObjectState.RUN_TIME;
            } else {
                if (demolitionSteps != null && demolitionSteps.length > 0) {
                    return SyncObjectState.DEMOLITION;
                } else {
                    return SyncObjectState.RUN_TIME;
                }
            }
        } else {
            return SyncObjectState.RUN_TIME;
        }
    }

    public Index getItemTypeImageOffset(SyncItem syncItem, long timeStamp) {
        switch (getSyncObjectState(syncItem)) {
            case BUILD_UP:
                return getBuildupImageOffset((SyncBaseItem) syncItem, timeStamp);
            case RUN_TIME:
                return getRuntimeImageOffset(syncItem, timeStamp);
            case DEMOLITION:
                return getDemolitionImageOffset((SyncBaseItem) syncItem, timeStamp);
            default:
                throw new IllegalArgumentException("ItemTypeSpriteMap.getItemTypeImageOffset() unknown SyncObjectState: " + getSyncObjectState(syncItem));
        }
    }

    private Index getBuildupImageOffset(SyncBaseItem syncBaseItem, long timeStamp) {
        int step = getBuildupStep(syncBaseItem);
        int animationFrame = getBuildupAnimationFrame(timeStamp);
        return getBuildupImageOffsetFromFrame(step, animationFrame);
    }

    public Index getBuildupImageOffsetFromFrame(int step, int animationFrame) {
        return new Index(imageWidth * (step * buildupAnimationFrames + animationFrame), 0);
    }

    public int getBuildupAnimationFrame(long timeStamp) {
        int animationFrame = 0;
        if (buildupAnimationDuration > 0) {
            long iteration = timeStamp / buildupAnimationDuration;
            animationFrame = (int) (iteration % buildupAnimationFrames);
        }
        return animationFrame;
    }

    public int getBuildupStep(SyncItem syncItem) {
        if (syncItem instanceof SyncBaseItem) {
            return (int) (((SyncBaseItem) syncItem).getBuildup() * buildupSteps);
        } else {
            return 0;
        }
    }

    private Index getRuntimeImageOffset(SyncItem syncItem, long timeStamp) {
        int angelIndex = boundingBox.angelToAngelIndex(syncItem.getSyncItemArea().getAngel());
        return getRuntimeImageOffset(angelIndex, timeStamp);
    }

    public Index getRuntimeImageOffset(int angelIndex, long timeStamp) {
        int animationFrame = getRuntimeAnimationFrame(timeStamp);
        return getRuntimeImageOffsetFromFrame(angelIndex, animationFrame);
    }

    public int getRuntimeAnimationFrame(long timeStamp) {
        int animationFrame = 0;
        if (runtimeAnimationDuration > 0) {
            long iteration = timeStamp / runtimeAnimationDuration;
            animationFrame = (int) (iteration % runtimeAnimationFrames);
        }
        return animationFrame;
    }

    public Index getRuntimeImageOffsetFromFrame(int angelIndex, int frame) {
        return new Index(runtimeXOffset + imageWidth * (angelIndex * runtimeAnimationFrames + frame), 0);
    }

    public Index getDemolitionImageOffsetFromFrame(int angelIndex, int step, int animationFrame) {
        int totalDemolitionAnimationFrames = demolitionFramesPerAngel * angelIndex;
        for (int i = 0; i < step; i++) {
            totalDemolitionAnimationFrames += demolitionSteps[i].getAnimationFrames();
        }
        return new Index(demolitionXOffset + (totalDemolitionAnimationFrames + animationFrame) * imageWidth, 0);
    }

    private void setupDemolitionFramesPerAngel() {
        demolitionFramesPerAngel = 0;
        int demolitionStepCount = getDemolitionStepCount();
        for (int i = 0; i < demolitionStepCount; i++) {
            DemolitionStepSpriteMap demolitionStepSpriteMaps = demolitionSteps[i];
            demolitionFramesPerAngel += demolitionStepSpriteMaps.getAnimationFrames();
        }
    }

    public Index getDemolitionImageOffset(SyncBaseItem syncBaseItem, long timeStamp) {
        int step = getDemolitionStep4ItemImage(syncBaseItem);
        if (step < 0) {
            return getRuntimeImageOffset(syncBaseItem, timeStamp);
        }
        int angelIndex = boundingBox.angelToAngelIndex(syncBaseItem.getSyncItemArea().getAngel());
        int animationFrame = getDemolitionAnimationFrame(step, timeStamp);
        return getDemolitionImageOffsetFromFrame(angelIndex, step, animationFrame);
    }

    public int getDemolitionAnimationFrame(int step, long timeStamp) {
        int animationFrame = 0;
        DemolitionStepSpriteMap demolitionStepSpriteMap = getDemolitionStepSpriteMap(step);
        if (demolitionStepSpriteMap.getAnimationDuration() > 0) {
            long iteration = timeStamp / demolitionStepSpriteMap.getAnimationDuration();
            animationFrame = (int) (iteration % demolitionStepSpriteMap.getAnimationFrames());
        }
        return animationFrame;
    }

    public int getDemolitionStep4ItemImage(SyncItem syncItem) {
        if (syncItem instanceof SyncBaseItem) {
            int step = getDemolitionStep(syncItem);
            while (step >= 0 && demolitionSteps[step].getAnimationFrames() == 0) {
                step--;
            }
            return step;
        } else {
            return 0;
        }
    }

    public int getDemolitionStep(SyncItem syncItem) {
        if (syncItem instanceof SyncBaseItem) {
            if (syncItem.isAlive()) {
                int step = (int) (getDemolitionStepCount() * (1.0 - ((SyncBaseItem) syncItem).getNormalizedHealth()));
                if (step >= getDemolitionStepCount()) {
                    return getDemolitionStepCount() - 1;
                } else if (step < 0) {
                    return 0;
                } else {
                    return step;
                }
            } else {
                return getDemolitionStepCount() - 1;
            }
        } else {
            throw new IllegalStateException("Only SyncBaseItem do have a demolition step: " + syncItem);
        }
    }

    public Index getCosmeticImageOffset() {
        return cosmeticImageOffset;
    }

    /* *
     * @param angel angel
     * @return offset = ImageNr * image imageWidth
     */
/*    public int angelToImageOffset(double angel) {
        return angelToImageNr(angel) * imageWidth;
    } */
    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Index getMiddleFromImage() {
        return new Index(imageWidth / 2, imageHeight / 2);
    }

    /**
     * The cosmetic image index starts with 0.
     *
     * @return The cosmetic image index starts with 0.
     */
    /*   public int getCosmeticImageIndex() {
        return getCosmeticImageIndex(boundingBox.getAngels().length);
    }*/
    public int getBuildupSteps() {
        return buildupSteps;
    }

    public void setBuildupSteps(int buildupSteps) {
        this.buildupSteps = buildupSteps;
    }

    public int getBuildupAnimationFrames() {
        return buildupAnimationFrames;
    }

    public void setBuildupAnimationFrames(int buildupAnimationFrames) {
        this.buildupAnimationFrames = buildupAnimationFrames;
    }

    public int getRuntimeAnimationFrames() {
        return runtimeAnimationFrames;
    }

    public int getRuntimeAnimationDuration() {
        return runtimeAnimationDuration;
    }

    public void setRuntimeAnimationFrames(int runtimeAnimationFrames) {
        this.runtimeAnimationFrames = runtimeAnimationFrames;
    }

    public void setRuntimeAnimationDuration(int runtimeAnimationDuration) {
        this.runtimeAnimationDuration = runtimeAnimationDuration;
    }

    public int getBuildupAnimationDuration() {
        return buildupAnimationDuration;
    }

    public void setBuildupAnimationDuration(int buildupAnimationDuration) {
        this.buildupAnimationDuration = buildupAnimationDuration;
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public int getDemolitionStepCount() {
        if (demolitionSteps != null) {
            return demolitionSteps.length;
        } else {
            return 0;
        }
    }

    public DemolitionStepSpriteMap getDemolitionStepSpriteMap(int step) {
        if (demolitionSteps == null || demolitionSteps.length == 0) {
            return null;
        } else {
            return demolitionSteps[step];
        }
    }

    public Collection<ItemClipPosition> getDemolitionClipIds(SyncBaseItem syncBaseItem) {
        if (demolitionSteps == null) {
            return null;
        }
        return demolitionSteps[getDemolitionStep(syncBaseItem)].getItemClipPositions();
    }

    public DemolitionStepSpriteMap[] getDemolitionSteps() {
        return demolitionSteps;
    }

    public void setDemolitionSteps(DemolitionStepSpriteMap[] demolitionSteps) {
        this.demolitionSteps = demolitionSteps;
    }

    public int getDemolitionFramesPerAngel() {
        return demolitionFramesPerAngel;
    }
}
