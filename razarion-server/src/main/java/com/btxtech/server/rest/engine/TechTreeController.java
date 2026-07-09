package com.btxtech.server.rest.engine;

import com.btxtech.server.service.engine.LevelCrudService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only, player-facing view of all levels (limitations + crystal unlocks) that feeds the
 * in-game tech tree teaser. Unlike the level editor endpoint, this is not restricted to admins.
 */
@RestController
@RequestMapping("/rest/tech-tree")
public class TechTreeController {
    private final LevelCrudService levelCrudService;

    public TechTreeController(LevelCrudService levelCrudService) {
        this.levelCrudService = levelCrudService;
    }

    @Transactional
    @GetMapping(value = "levels", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LevelConfig> readLevels() {
        return levelCrudService.read();
    }
}
