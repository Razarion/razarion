package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.PlanetTickListener;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBuilder;
import com.btxtech.shared.gameengine.planet.model.SyncHarvester;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
public abstract class GameEngineWorker implements PlanetTickListener {
    @Inject
    private PlanetService planetService;
    @Inject
    private Event<GameEngineInitEvent> gameEngineInitEvent;
    @Inject
    private BotService botService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private SyncItemContainerService syncItemContainerService;

    protected abstract void sendToClient(GameEngineControlPackage.Command command, Object... object);

    public void initialise(GameEngineConfig gameEngineConfig) {
        gameEngineInitEvent.fire(new GameEngineInitEvent(gameEngineConfig));
        planetService.initialise(gameEngineConfig.getPlanetConfig());
        planetService.addTickListener(this);
    }

    public void start() {
        planetService.start();
    }

    public void stop() {
        planetService.stop();
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case INITIALIZE:
                initialise((GameEngineConfig) controlPackage.getSingleData());
                sendToClient(GameEngineControlPackage.Command.INITIALIZED);
                break;
            case INITIALIZED:
                break;
            case START:
                start();
                sendToClient(GameEngineControlPackage.Command.STARTED);
                break;
            case STARTED:
                break;
            case START_BOTS:
                botService.startBots((Collection<BotConfig>) controlPackage.getSingleData());
                break;
            case EXECUTE_BOT_COMMANDS:
                botService.executeCommands((List<? extends AbstractBotCommandConfig>) controlPackage.getSingleData());
                break;
            case CREATE_RESOURCES:
                resourceService.createResources((Collection<ResourceItemPosition>) controlPackage.getSingleData());
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                baseItemService.createHumanBaseWithBaseItem((UserContext) controlPackage.getData(0), (Integer) controlPackage.getData(1), (DecimalPosition) controlPackage.getData(2));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }

    @Override
    public void onPostTick() {
        List<SyncBaseItemSimpleDto> syncItems = new ArrayList<>();
        syncItemContainerService.iterateOverItems(false, false, null, syncItem -> {
            if (syncItem instanceof SyncBaseItem) {
                SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                SyncBaseItemSimpleDto simpleDto = new SyncBaseItemSimpleDto();
                simpleDto.setBaseItemTypeId(syncBaseItem.getItemType().getId());
                simpleDto.setModel(syncBaseItem.getSyncPhysicalArea().getModelMatrices().getModel());
                if (syncBaseItem.getSyncWeapon() != null && syncBaseItem.getSyncWeapon().getSyncTurret() != null) {
                    simpleDto.setWeaponTurret(syncBaseItem.getSyncWeapon().createTurretModelMatrices4Shape3D());
                }
                simpleDto.setPosition(syncBaseItem.getSyncPhysicalArea().getPosition2d());
                simpleDto.setSpawning(syncBaseItem.getSpawnProgress());
                simpleDto.setBuildup(syncBaseItem.getBuildup());
                simpleDto.setHealth(syncBaseItem.getNormalizedHealth());
                SyncHarvester harvester = syncBaseItem.getSyncHarvester();
                if (harvester != null && harvester.isHarvesting()) {
                    simpleDto.setHarvestingResourcePosition(harvester.getResource().getSyncPhysicalArea().getPosition3d());
                }
                SyncBuilder builder = syncBaseItem.getSyncBuilder();
                if (builder != null && builder.isBuilding()) {
                    simpleDto.setBuildingPosition(builder.getCurrentBuildup().getSyncPhysicalArea().getPosition3d());
                }
                syncItems.add(simpleDto);
            }
            return null;
        });
        sendToClient(GameEngineControlPackage.Command.SYNC_ITEM_UPDATE, syncItems);
    }
}
