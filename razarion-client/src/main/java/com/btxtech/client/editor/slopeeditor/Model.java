package com.btxtech.client.editor.slopeeditor;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import elemental.events.MouseEvent;

/**
 * Created by Beat
 * 06.02.2016.
 */
public interface Model {
    DecimalPosition convertMouseToSvg(MouseEvent event);

    void createCorner(DecimalPosition position, Corner previous);

    void cornerMoved(DecimalPosition position, Corner corner);

    void selectionChanged(Corner corner);
}
