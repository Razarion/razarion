package com.btxtech.server.rest.editor;

import com.btxtech.server.service.studio.StudioSceneService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD for studio scenes. Admin-gated like every other studio endpoint.
 *   GET  /studio-scene          → summary list (no jsonContent)
 *   GET  /studio-scene/{id}     → full payload
 *   POST /studio-scene          → create, returns new dto
 *   POST /studio-scene/{id}     → update existing
 *   DELETE /studio-scene/{id}   → delete
 */
@RestController
@RequestMapping("/rest/editor/studio-scene")
@PreAuthorize("hasAuthority('ADMIN')")
public class StudioSceneEditorController {
    private final StudioSceneService service;

    public StudioSceneEditorController(StudioSceneService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudioSceneSummary> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public StudioSceneDto read(@PathVariable("id") int id) {
        return service.read(id);
    }

    @PostMapping
    public StudioSceneDto create(@RequestBody StudioSceneDto dto) {
        return service.create(dto);
    }

    @PostMapping("/{id}")
    public StudioSceneDto update(@PathVariable("id") int id, @RequestBody StudioSceneDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int id) {
        service.delete(id);
    }
}
