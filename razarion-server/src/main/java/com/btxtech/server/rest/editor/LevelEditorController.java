package com.btxtech.server.rest.editor;

import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.engine.LevelCrudPersistence;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/level")
public class LevelEditorController extends AbstractBaseController<LevelEntity> {
    private final LevelCrudPersistence levelCrudPersistence;

    public LevelEditorController(LevelCrudPersistence levelCrudPersistence) {
        this.levelCrudPersistence = levelCrudPersistence;
    }

    @Override
    protected AbstractBaseEntityCrudService<LevelEntity> getEntityCrudPersistence() {
        return levelCrudPersistence;
    }
}
