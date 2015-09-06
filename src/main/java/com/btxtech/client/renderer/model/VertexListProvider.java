package com.btxtech.client.renderer.model;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.shared.VertexList;

/**
 * Created by Beat
 * 29.06.2015.
 */
@Deprecated
public interface VertexListProvider {
    VertexList provideVertexList(ImageDescriptor imageDescriptor);
}
