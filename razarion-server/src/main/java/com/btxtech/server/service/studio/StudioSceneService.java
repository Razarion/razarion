package com.btxtech.server.service.studio;

import com.btxtech.server.model.studio.StudioSceneEntity;
import com.btxtech.server.repository.studio.StudioSceneRepository;
import com.btxtech.server.rest.editor.StudioSceneDto;
import com.btxtech.server.rest.editor.StudioSceneSummary;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Storage for studio scenes. The JSON payload is opaque — schema validation,
 * versioning and migrations live in the studio frontend.
 */
@Service
public class StudioSceneService {
    private final StudioSceneRepository repository;

    public StudioSceneService(StudioSceneRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<StudioSceneSummary> list() {
        return repository.findAll().stream()
                .map(StudioSceneService::toSummary)
                .sorted(Comparator.comparing(StudioSceneSummary::getLastModified,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public StudioSceneDto read(int id) {
        return toDto(load(id));
    }

    @Transactional
    public StudioSceneDto create(StudioSceneDto dto) {
        StudioSceneEntity entity = new StudioSceneEntity();
        entity.setSceneName(dto.getName());
        entity.setJsonContent(dto.getJsonContent());
        entity.setLastModified(Instant.now());
        return toDto(repository.save(entity));
    }

    @Transactional
    public StudioSceneDto update(int id, StudioSceneDto dto) {
        StudioSceneEntity entity = load(id);
        if (dto.getName() != null) {
            entity.setSceneName(dto.getName());
        }
        entity.setJsonContent(dto.getJsonContent());
        entity.setLastModified(Instant.now());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(int id) {
        repository.delete(load(id));
    }

    private StudioSceneEntity load(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("StudioScene not found: " + id));
    }

    private static StudioSceneSummary toSummary(StudioSceneEntity entity) {
        StudioSceneSummary s = new StudioSceneSummary();
        s.setId(entity.getId());
        s.setName(entity.getSceneName());
        s.setLastModified(entity.getLastModified());
        return s;
    }

    private static StudioSceneDto toDto(StudioSceneEntity entity) {
        StudioSceneDto d = new StudioSceneDto();
        d.setId(entity.getId());
        d.setName(entity.getSceneName());
        d.setJsonContent(entity.getJsonContent());
        d.setLastModified(entity.getLastModified());
        return d;
    }
}
