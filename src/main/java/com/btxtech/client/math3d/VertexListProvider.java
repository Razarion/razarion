package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.VertexList;

/**
 * Created by Beat
 * 29.06.2015.
 */
public interface VertexListProvider {
    VertexList provideVertexList(ImageDescriptor imageDescriptor);
}
