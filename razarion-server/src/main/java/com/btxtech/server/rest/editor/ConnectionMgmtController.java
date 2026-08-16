package com.btxtech.server.rest.editor;

import com.btxtech.server.gameengine.ClientGameConnection;
import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ClientSystemConnection;
import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.OpenConnectionInfo;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the server currently has open, for the editor's connection dialog.
 */
@RestController
@RequestMapping("/rest/editor/connection-mgmt")
public class ConnectionMgmtController {
    private final Logger logger = LoggerFactory.getLogger(ConnectionMgmtController.class);
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final ClientGameConnectionService clientGameConnectionService;
    private final UserService userService;
    private final BaseItemService baseItemService;
    private final LevelService levelService;

    public ConnectionMgmtController(ClientSystemConnectionService clientSystemConnectionService,
                                    ClientGameConnectionService clientGameConnectionService,
                                    UserService userService,
                                    BaseItemService baseItemService,
                                    LevelService levelService) {
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.clientGameConnectionService = clientGameConnectionService;
        this.userService = userService;
        this.baseItemService = baseItemService;
        this.levelService = levelService;
    }

    /**
     * One row per connected user, merged from both web sockets. A user shows up as soon as either
     * one is open: the two are established at different moments and either can be missing, and
     * which of them is missing is exactly what makes a row interesting.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("openConnections")
    public List<OpenConnectionInfo> openConnections() {
        Map<String, ClientSystemConnection> systemConnections = clientSystemConnectionService.getOpenConnections();
        Map<String, ClientGameConnection> gameConnections = clientGameConnectionService.getOpenConnections();

        Set<String> userIds = new LinkedHashSet<>(systemConnections.keySet());
        userIds.addAll(gameConnections.keySet());
        Map<String, String> names = userService.getUserNames(userIds);

        List<OpenConnectionInfo> result = new ArrayList<>();
        for (String userId : userIds) {
            OpenConnectionInfo info = new OpenConnectionInfo()
                    .userId(userId)
                    .name(names.get(userId));
            ClientSystemConnection systemConnection = systemConnections.get(userId);
            if (systemConnection != null) {
                info.systemConnectionOpened(systemConnection.getTime())
                        .systemLastMessageSent(systemConnection.getLastMessageSent())
                        .systemLastMessageReceived(systemConnection.getLastMessageReceived())
                        .userAgent(systemConnection.getUserAgent());
            }
            ClientGameConnection gameConnection = gameConnections.get(userId);
            if (gameConnection != null) {
                info.gameConnectionOpened(gameConnection.getTime())
                        .gameLastMessageSent(gameConnection.getLastMessageSent())
                        .gameLastMessageReceived(gameConnection.getLastMessageReceived());
                // The system socket opens first and is the one to believe; the game socket answers
                // for the rows where it is the only one left.
                if (info.getUserAgent() == null) {
                    info.userAgent(gameConnection.getUserAgent());
                }
            }
            addBase(info, userId);
            result.add(info);
        }
        // Newest first: a connection storm shows up at the top, where it is noticed.
        result.sort(Comparator.comparing(ConnectionMgmtController::openedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /**
     * The base behind a connection, asked of the master rather than of the browser. A client's own
     * base list has been seen to be incomplete, and an admin table that inherits that blind spot is
     * worth little.
     */
    private void addBase(OpenConnectionInfo info, String userId) {
        PlayerBaseFull base = baseItemService.getPlayerBaseFull4UserId(userId);
        if (base == null) {
            return;
        }
        info.baseId(base.getBaseId())
                .baseName(base.getName())
                .itemCount(base.getItemCount());
        if (base.getLevelId() != null) {
            try {
                info.levelNumber(levelService.getLevel(base.getLevelId()).getNumber());
            } catch (IllegalArgumentException e) {
                // A level deleted while a base still points at it must not take the table down.
                logger.warn("Base {} refers to unknown level {}", base.getBaseId(), base.getLevelId());
            }
        }
        // Somewhere to send the camera. The first item is an arbitrary but always real spot on the
        // map - a centre of gravity would be prettier and could land on empty ground.
        base.getItems().stream().findFirst().ifPresent(item -> {
            DecimalPosition position = item.getAbstractSyncPhysical().getPosition();
            if (position != null) {
                info.baseX(position.getX()).baseY(position.getY());
            }
        });
    }

    /** The moment this client first appeared, whichever of its two sockets opened first. */
    private static java.util.Date openedAt(OpenConnectionInfo info) {
        java.util.Date system = info.getSystemConnectionOpened();
        java.util.Date game = info.getGameConnectionOpened();
        if (system == null) {
            return game;
        }
        if (game == null) {
            return system;
        }
        return system.before(game) ? system : game;
    }
}
