package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.asset.AssetConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.ASSET_EDITOR_PATH)
public interface AssetEditorController extends CrudController<AssetConfig> {
}
