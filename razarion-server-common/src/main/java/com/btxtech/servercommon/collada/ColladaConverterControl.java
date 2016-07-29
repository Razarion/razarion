package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 11.05.2016.
 */
@Deprecated
public abstract class ColladaConverterControl {
    private List<Vertex> vertices;
    private List<Vertex> norms;
    private List<TextureCoordinate> textureCoordinates;
    private String materialId;
    private String materialName;
    private Effect effect;

    protected abstract void onNewVertexContainer(VertexContainer vertexContainer);
}
