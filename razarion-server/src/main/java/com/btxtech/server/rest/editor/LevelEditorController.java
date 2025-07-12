package com.btxtech.server.rest.editor;

import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.engine.LevelCrudService;
import com.btxtech.shared.dto.ObjectNameId;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/editor/level")
public class LevelEditorController extends AbstractBaseController<LevelEntity> {
    private final LevelCrudService levelCrudService;

    public LevelEditorController(LevelCrudService levelCrudService) {
        this.levelCrudService = levelCrudService;
    }

    @Override
    protected AbstractBaseEntityCrudService<LevelEntity> getBaseEntityCrudService() {
        return levelCrudService;
    }

    @Transactional
    public List<ObjectNameId> getObjectNameIds() {
        return levelCrudService.getEntities().stream()
                .map(baseEntity -> new ObjectNameId(baseEntity.getId(), Integer.toString(baseEntity.getNumber())))
                .collect(Collectors.toList());
    }

}
