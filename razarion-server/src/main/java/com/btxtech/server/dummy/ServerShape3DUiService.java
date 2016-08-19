package com.btxtech.server.dummy;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.uiservice.Shape3DUiService;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 17.08.2016.
 */
@Deprecated
public class ServerShape3DUiService extends Shape3DUiService {

    @Override
    public void create(String dataUrl, Consumer<List<Shape3D>> callback) {

    }

    @Override
    public void reload(Consumer<List<Shape3D>> callback) {

    }

    @Override
    protected void convertColladaText(String colladaText, Consumer<Shape3D> callback) {

    }
}
