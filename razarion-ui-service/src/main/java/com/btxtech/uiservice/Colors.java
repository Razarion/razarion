package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Color;

/**
 * Created by Beat
 * 10.09.2016.
 */
public interface Colors {
    Color START_POINT_PLACER_VALID = new Color(0.0, 1.0, 0.0, 0.5);
    Color START_POINT_PLACER_IN_VALID = new Color(1.0, 0.0, 0.0, 0.5);

    Color SELECTION_FRAME = new Color(0.2, 1.0, 0.2, 1.0);

    Color OWN = new Color(0.6, 1.0, 0.6, 1.0);
    Color ENEMY = new Color(1.0, 0.0, 0.0, 1.0);
    Color FRIEND = new Color(0.0, 0.0, 1.0, 1.0);
    Color NONE_BASE = new Color(1.0, 1.0, 0.0, 1.0);

    double SELECTION_ALPHA = 0.8;
    double HOVER_ALPHA = 0.4;

    Color HEALTH_BAR = new Color(0.0, 1.0, 0.0, 1.0);
    Color CONSTRUCTING_BAR = new Color(0.4, 0.4, 1.0, 1.0);
    Color BAR_BG = new Color(0.2, 0.2, 0.2, 1.0);
}
