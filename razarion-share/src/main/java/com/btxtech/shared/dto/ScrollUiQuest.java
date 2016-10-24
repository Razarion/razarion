package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;

/**
 * Created by Beat
 * 24.10.2016.
 */
public class ScrollUiQuest extends QuestDescriptionConfig<ScrollUiQuest> {
    private Rectangle2D scrollTargetRectangle;

    public Rectangle2D getScrollTargetRectangle() {
        return scrollTargetRectangle;
    }

    public ScrollUiQuest setScrollTargetRectangle(Rectangle2D scrollTargetRectangle) {
        this.scrollTargetRectangle = scrollTargetRectangle;
        return this;
    }

    @Override
    public String toString() {
        return "ScrollUiQuest{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", scrollTargetRectangle=" + scrollTargetRectangle +
                '}';
    }
}
