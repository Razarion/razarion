package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 14.10.2016.
 */
public interface ClipService {
    void playClip(Vertex position, Vertex direction, int clipId, long timeStamp);
}
