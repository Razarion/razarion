package com.btxtech.client.sidebar.slopeeditor;

import com.btxtech.game.jsre.client.common.Index;
import elemental.events.MouseEvent;

/**
 * Created by Beat
 * 06.02.2016.
 */
public interface Model {
    Index convertMouseToSvg(MouseEvent event);

    void createCorner(Index position, Corner previous);

    void cornerMoved(Index position, Corner corner);

    void selectionChanged(Corner corner);
}
