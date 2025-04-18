package com.btxtech.server.rest.editor;

import com.btxtech.server.model.engine.DbPropertyConfig;
import com.btxtech.server.service.engine.DbPropertiesService;
import com.btxtech.server.user.SecurityCheck;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/editor/properties-editor")
public class DbPropertiesEditorController {
    private final DbPropertiesService dbPropertiesService;

    public DbPropertiesEditorController(DbPropertiesService dbPropertiesService) {
        this.dbPropertiesService = dbPropertiesService;
    }

    @SecurityCheck
    @GetMapping(value = "get-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DbPropertyConfig> readAllProperties() {
        return dbPropertiesService.getDbPropertyConfigs();
    }

    @SecurityCheck
    @PutMapping(value = "update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateProperty(@RequestBody DbPropertyConfig dbPropertyConfig) {
        dbPropertiesService.saveDbPropertyConfig(dbPropertyConfig);
    }

}
