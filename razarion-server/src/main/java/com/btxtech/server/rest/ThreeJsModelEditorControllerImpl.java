package com.btxtech.server.rest;

import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.rest.ThreeJsModelEditorController;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.btxtech.server.rest.ImageProviderImpl.inputStreamToArray;

public class ThreeJsModelEditorControllerImpl extends AbstractCrudController<ThreeJsModelConfig, ThreeJsModelConfigEntity> implements ThreeJsModelEditorController {
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    @Override
    protected ThreeJsModelCrudPersistence getCrudPersistence() {
        return threeJsModelCrudPersistence;
    }

    @Override
    @SecurityCheck
    public void upload(int id, Map<String, InputStream> formData) {
        try {
            InputStream inputStream = formData.get("http_form_data_model");
            byte[] bytes = inputStreamToArray(inputStream);
            threeJsModelCrudPersistence.saveData(id, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
