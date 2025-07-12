package com.btxtech.server.rest.editor;

import com.btxtech.server.model.Roles;
import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.TerrainObjectService;
import com.btxtech.shared.dto.TerrainObjectConfig;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/terrain-object")
public class TerrainObjectEditorController extends AbstractConfigController<TerrainObjectConfig> {
    private final TerrainObjectService terrainObjectService;

    public TerrainObjectEditorController(TerrainObjectService terrainObjectService) {
        this.terrainObjectService = terrainObjectService;
    }

    @Override
    protected TerrainObjectService getConfigCrudService() {
        return terrainObjectService;
    }

    @RolesAllowed(Roles.ADMIN)
    @PostMapping("update-radius/{terrainObjectId}/{radius}")
    void updateRadius(@PathVariable("terrainObjectId") int terrainObjectId, @PathVariable("radius") double radius) {
        terrainObjectService.updateRadius(terrainObjectId, radius);

    }
}
