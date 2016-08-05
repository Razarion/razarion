package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;

/**
 * User: beat
 * Date: 06.11.12
 * Time: 18:02
 */
@Portable
public class DemolitionStepSpriteMap {
    private int animationFrames;
    private int animationDuration;
    private Collection<ItemClipPosition> itemClipPositions;

    public DemolitionStepSpriteMap() {
    }

    public DemolitionStepSpriteMap(int animationFrames, int animationDuration, Collection<ItemClipPosition> itemClipPositions) {
        this.animationFrames = animationFrames;
        this.animationDuration = animationDuration;
        this.itemClipPositions = itemClipPositions;
    }

    public int getAnimationFrames() {
        return animationFrames;
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public Collection<ItemClipPosition> getItemClipPositions() {
        return itemClipPositions;
    }

    public void setItemClipPositions(Collection<ItemClipPosition> itemClipPositions) {
        this.itemClipPositions = itemClipPositions;
    }

    public void setAnimationFrames(int animationFrames) {
        this.animationFrames = animationFrames;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }
}
