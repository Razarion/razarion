package com.btxtech.server.rest.director;

import com.btxtech.server.service.director.DirectorService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Director mode = filming the live game world for social-media clips.
 * <p>
 * Everything here leaves the world exactly as it found it: it hands the rendering client a camera
 * flight to fly and a recording to start, and answers questions about what is out there. Nothing
 * spawns, builds or destroys — the endpoints that do live in {@link DirectorStagingController}
 * behind their own property, so this half can be switched on against the live planet without
 * bringing the other half with it.
 * <p>
 * Writing is not the same as changing the world: plans and the transport command are stored in
 * the director's own table and an in-memory slot. A base filmed by this controller cannot tell
 * that anyone was watching.
 * <p>
 * GET    /rest/director/plan          → plan summary list
 * GET    /rest/director/plan/{id}     → full plan payload
 * POST   /rest/director/plan          → create
 * POST   /rest/director/plan/{id}     → update
 * DELETE /rest/director/plan/{id}     → delete
 * POST   /rest/director/command       → studio publishes a transport command
 * GET    /rest/director/command       → director-mode client polls the latest
 * POST   /rest/director/camera        → client publishes its live camera pose
 * GET    /rest/director/camera        → studio reads it back to author a keyframe
 * GET    /rest/director/bases         → which bases exist, to pick one to follow
 */
@RestController
@RequestMapping("/rest/director")
// Two guards, unchanged in kind - only the default changed from "never on prod" to "on where it
// is asked for":
//  1. @ConditionalOnProperty — the bean exists only where razarion.director.enabled is true.
//     application.properties reads it from RAZARION_DIRECTOR_ENABLED and defaults to false, so
//     an environment that says nothing gets no endpoints at all → 404.
//  2. @PreAuthorize ADMIN — only authenticated admins pass. Both the studio and the rendering
//     client send the JWT via AuthInterceptor.
@ConditionalOnProperty(name = "razarion.director.enabled", havingValue = "true")
@PreAuthorize("hasAuthority('ADMIN')")
public class DirectorController {
    private final DirectorService service;
    private final BaseItemService baseItemService;

    public DirectorController(DirectorService service, BaseItemService baseItemService) {
        this.service = service;
        this.baseItemService = baseItemService;
    }

    @GetMapping("/plan")
    public List<DirectorPlanSummary> list() {
        return service.list();
    }

    @GetMapping("/plan/{id}")
    public DirectorPlanDto read(@PathVariable("id") int id) {
        return service.read(id);
    }

    @PostMapping("/plan")
    public DirectorPlanDto create(@RequestBody DirectorPlanDto dto) {
        return service.create(dto);
    }

    @PostMapping("/plan/{id}")
    public DirectorPlanDto update(@PathVariable("id") int id, @RequestBody DirectorPlanDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/plan/{id}")
    public void delete(@PathVariable("id") int id) {
        service.delete(id);
    }

    @PostMapping("/command")
    public DirectorCommand postCommand(@RequestBody DirectorCommand command) {
        return service.postCommand(command);
    }

    /**
     * The rendering client polls this four times a second; the studio also reads it, so only a
     * caller that says {@code client=render} counts as "a client is listening".
     */
    @GetMapping("/command")
    public DirectorCommand lastCommand(@RequestParam(name = "client", required = false) String client) {
        if (client != null) {
            service.noteClientPoll();
        }
        return service.lastCommand();
    }

    /**
     * Is anything out there? The command channel is one-way: a command lands in a slot, and
     * nothing in the answer says whether a client ever read it. Without this, a studio driving a
     * client that is signed in as the wrong user looks identical to one driving nothing at all.
     */
    @GetMapping("/status")
    public DirectorStatus status() {
        DirectorCommand last = service.lastCommand();
        return new DirectorStatus(service.clientLastSeenMillisAgo(), last != null ? last.getSeq() : null);
    }

    @PostMapping("/camera")
    public void postCamera(@RequestBody DirectorCameraPose pose) {
        service.setCamera(pose);
    }

    @GetMapping("/camera")
    public DirectorCameraPose lastCamera() {
        return service.lastCamera();
    }

    /**
     * All current bases with where they are, so the studio can pick one to follow.
     * <p>
     * The position is the part that matters: a client is only sent what happens near where it is
     * looking, so a camera told to follow a base it has never seen has nothing to aim at. With a
     * centre from here the plan can start by flying there; from then on the client has the units
     * and follows them itself.
     */
    @GetMapping("/bases")
    public List<DirectorBaseInfo> bases() {
        return baseItemService.getPlayerBaseInfos().stream()
                .map(this::toDirectorBaseInfo)
                .toList();
    }

    private DirectorBaseInfo toDirectorBaseInfo(PlayerBaseInfo info) {
        PlayerBase base = baseItemService.getPlayerBase4BaseId(info.getBaseId());
        List<DecimalPosition> positions = base instanceof PlayerBaseFull full
                ? full.getItems().stream()
                .map(item -> item.getAbstractSyncPhysical().getPosition())
                .filter(Objects::nonNull)
                .toList()
                : List.of();
        if (positions.isEmpty()) {
            return new DirectorBaseInfo(info.getBaseId(), info.getName(), info.getCharacter(),
                    info.getUserId(), 0, null, null, null);
        }
        // The mean, not the middle of the bounding box: a base is usually a cluster with one
        // harvester out at a resource, and the box would put the camera on empty ground between
        // the two.
        double x = positions.stream().mapToDouble(DecimalPosition::getX).average().orElse(0);
        double y = positions.stream().mapToDouble(DecimalPosition::getY).average().orElse(0);
        double radius = positions.stream()
                .mapToDouble(p -> Math.hypot(p.getX() - x, p.getY() - y))
                .max().orElse(0);
        return new DirectorBaseInfo(info.getBaseId(), info.getName(), info.getCharacter(),
                info.getUserId(), positions.size(), x, y, radius);
    }
}
