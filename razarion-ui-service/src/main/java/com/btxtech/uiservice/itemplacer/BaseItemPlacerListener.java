package com.btxtech.uiservice.itemplacer;

/**
 * Created by Beat
 * 06.12.2016.
 */
public interface BaseItemPlacerListener {
    void activatePlacer(BaseItemPlacer baseItemPlacer);

    void deactivatePlacer(boolean canceled);
}
