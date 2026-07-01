package com.btxtech.server.service.director;

import com.btxtech.server.model.director.DirectorPlanEntity;
import com.btxtech.server.repository.director.DirectorPlanRepository;
import com.btxtech.server.rest.director.DirectorCameraPose;
import com.btxtech.server.rest.director.DirectorCommand;
import com.btxtech.server.rest.director.DirectorPlanDto;
import com.btxtech.server.rest.director.DirectorPlanSummary;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Storage for director plans + the studio→client transport command channel.
 * The JSON plan payload is opaque (schema lives in the frontend). The command
 * channel is a single in-memory slot — fine because director mode is a local,
 * single-operator dev tool; it is never used on prod (see DirectorController).
 */
@Service
public class DirectorService {
    private final DirectorPlanRepository repository;
    /** Latest transport command; the client polls and acts on unseen seq. */
    private final AtomicReference<DirectorCommand> lastCommand = new AtomicReference<>();
    private final AtomicLong seqGenerator = new AtomicLong();
    /** Last camera pose captured from the client (for studio "capture view"). */
    private final AtomicReference<DirectorCameraPose> lastCamera = new AtomicReference<>();

    public DirectorService(DirectorPlanRepository repository) {
        this.repository = repository;
    }

    // ===== Plan CRUD =====

    @Transactional
    public List<DirectorPlanSummary> list() {
        return repository.findAll().stream()
                .map(DirectorService::toSummary)
                .sorted(Comparator.comparing(DirectorPlanSummary::getLastModified,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public DirectorPlanDto read(int id) {
        return toDto(load(id));
    }

    @Transactional
    public DirectorPlanDto create(DirectorPlanDto dto) {
        DirectorPlanEntity entity = new DirectorPlanEntity();
        entity.setPlanName(dto.getName());
        entity.setJsonContent(dto.getJsonContent());
        entity.setLastModified(Instant.now());
        return toDto(repository.save(entity));
    }

    @Transactional
    public DirectorPlanDto update(int id, DirectorPlanDto dto) {
        DirectorPlanEntity entity = load(id);
        if (dto.getName() != null) {
            entity.setPlanName(dto.getName());
        }
        entity.setJsonContent(dto.getJsonContent());
        entity.setLastModified(Instant.now());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(int id) {
        repository.delete(load(id));
    }

    // ===== Command channel =====

    /** Stamp the command with a fresh seq and publish it as the latest. */
    public DirectorCommand postCommand(DirectorCommand command) {
        command.setSeq(seqGenerator.incrementAndGet());
        lastCommand.set(command);
        return command;
    }

    /** The latest command, or null if none has been posted yet. */
    public DirectorCommand lastCommand() {
        return lastCommand.get();
    }

    /** Client publishes its current camera pose (in response to a CAPTURE command). */
    public void setCamera(DirectorCameraPose pose) {
        pose.setSeq(seqGenerator.incrementAndGet());
        lastCamera.set(pose);
    }

    /** The latest captured camera pose, or null. */
    public DirectorCameraPose lastCamera() {
        return lastCamera.get();
    }

    private DirectorPlanEntity load(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DirectorPlan not found: " + id));
    }

    private static DirectorPlanSummary toSummary(DirectorPlanEntity entity) {
        DirectorPlanSummary s = new DirectorPlanSummary();
        s.setId(entity.getId());
        s.setName(entity.getPlanName());
        s.setLastModified(entity.getLastModified());
        return s;
    }

    private static DirectorPlanDto toDto(DirectorPlanEntity entity) {
        DirectorPlanDto d = new DirectorPlanDto();
        d.setId(entity.getId());
        d.setName(entity.getPlanName());
        d.setJsonContent(entity.getJsonContent());
        d.setLastModified(entity.getLastModified());
        return d;
    }
}
