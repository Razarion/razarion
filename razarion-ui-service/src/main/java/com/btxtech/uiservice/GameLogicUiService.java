package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.planet.GameLogicDelegate;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.clip.ClipService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 30.11.2016.
 */
@ApplicationScoped
public class GameLogicUiService implements GameLogicDelegate {
    private Logger logger = Logger.getLogger(GameLogicUiService.class.getName());
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private ClipService clipService;
    @Inject
    private SelectionHandler selectionHandler;

    public void onVisualConfig(@Observes VisualConfig visualConfig) {
        gameLogicService.setGameLogicDelegate(this);
    }

    @Override
    public void onProjectileFired(SyncBaseItem syncBaseItem, Vertex muzzlePosition, Vertex muzzleDirection, Integer clipId, long timeStamp) {
        if (clipId != null) {
            clipService.playClip(muzzlePosition, muzzleDirection, clipId, timeStamp);
        } else {
            logger.warning("No MuzzleFlashClipId configured for: " + syncBaseItem);
        }
    }

    @Override
    public void onProjectileDetonation(SyncBaseItem syncBaseItem, Vertex position, Integer clipId, long timeStamp) {
        if (clipId != null) {
            clipService.playClip(position, clipId, timeStamp);
        } else {
            logger.warning("No projectile detonation configured for: " + syncBaseItem);
        }
    }

    @Override
    public void onKilledSyncBaseItem(SyncBaseItem target, SyncBaseItem actor, long timeStamp) {
        if (target.getBaseItemType().getExplosionClipId() != null) {
            clipService.playClip(target.getSyncPhysicalArea().getPosition3d(), target.getBaseItemType().getExplosionClipId(), timeStamp);
        } else {
            logger.warning("No explosion ClipId configured for: " + target);
        }
        selectionHandler.itemKilled(target);
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem target) {
        selectionHandler.itemKilled(target);
    }
}
