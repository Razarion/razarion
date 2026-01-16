package com.btxtech.server;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.GuardingItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceRegion;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.bot.BotEnragementState;
import com.btxtech.shared.gameengine.planet.bot.BotItemContainer;
import com.btxtech.shared.gameengine.planet.bot.BotRunner;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.bot.BotSyncBaseItem;
import com.btxtech.shared.gameengine.planet.bot.IntruderHandler;
import com.btxtech.shared.gameengine.planet.energy.BaseEnergy;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncBuilder;
import com.btxtech.shared.gameengine.planet.model.SyncConsumer;
import com.btxtech.shared.gameengine.planet.model.SyncFactory;
import com.btxtech.shared.gameengine.planet.model.SyncGenerator;
import com.btxtech.shared.gameengine.planet.model.SyncHarvester;
import com.btxtech.shared.gameengine.planet.model.SyncHouse;
import com.btxtech.shared.gameengine.planet.model.SyncItemContainer;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.model.SyncWeapon;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.quest.BaseItemCountComparison;
import com.btxtech.shared.gameengine.planet.quest.BaseItemPositionComparison;
import com.btxtech.shared.gameengine.planet.quest.BaseItemTypeComparison;
import com.btxtech.shared.gameengine.planet.quest.CountComparison;
import com.btxtech.shared.gameengine.planet.quest.CountPositionComparison;
import com.btxtech.shared.gameengine.planet.quest.InventoryItemCountComparison;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileBuilder;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileFactory;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.alarm.AlarmService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class GameEngineConfiguration {
    // private final Logger logger = LoggerFactory.getLogger(GameEngineConfiguration.class);

    @Bean
    public AlarmService alarmService() {
        return new AlarmService();
    }

    @Bean
    public InitializeService initializeService() {
        return new InitializeService();
    }

    @Bean
    @Scope("prototype")
    public IntruderHandler intruderHandler(SyncItemContainerServiceImpl syncItemContainerService) {
        return new IntruderHandler(syncItemContainerService);
    }

    @Bean
    @Scope("prototype")
    public BotRunner botRunner(SimpleExecutorService simpleExecutorService,
                               jakarta.inject.Provider<IntruderHandler> intruderHandlerInstance,
                               jakarta.inject.Provider<BotEnragementState> enragementStateInstance,
                               BaseItemService baseItemService) {
        return new BotRunner(simpleExecutorService,
                intruderHandlerInstance::get,
                enragementStateInstance::get,
                baseItemService);
    }

    @Bean
    @Scope("prototype")
    public BotItemContainer botItemContainer(jakarta.inject.Provider<BotSyncBaseItem> baseItemInstance,
                                             BotService botService,
                                             SyncItemContainerServiceImpl syncItemContainerService,
                                             BaseItemService baseItemService,
                                             ItemTypeService itemTypeService) {
        return new BotItemContainer(baseItemInstance::get,
                botService,
                syncItemContainerService,
                baseItemService,
                itemTypeService);
    }

    @Bean
    @Scope("prototype")
    public BotSyncBaseItem botSyncBaseItem(TerrainService terrainService,
                                           CommandService commandService,
                                           SyncItemContainerServiceImpl syncItemContainerService,
                                           BaseItemService baseItemService) {
        return new BotSyncBaseItem(terrainService, commandService, syncItemContainerService, baseItemService);
    }

    @Bean
    @Scope("prototype")
    public BotEnragementState botEnragementState(jakarta.inject.Provider<BotItemContainer> containerInstance) {
        return new BotEnragementState(containerInstance::get);
    }

    @Bean
    public BotService botService(jakarta.inject.Provider<BotRunner> botRunnerInstance) {
        return new BotService(botRunnerInstance::get);
    }

    @Bean
    public TerrainTypeService terrainTypeService(InitializeService initializeService) {
        return new TerrainTypeService(initializeService);
    }

    @Bean
    @Scope("prototype")
    public BaseEnergy baseEnergy(GameLogicService gameLogicService) {
        return new BaseEnergy(gameLogicService);
    }

    @Bean
    public EnergyService energyService(jakarta.inject.Provider<BaseEnergy> baseEnergyProvider) {
        return new EnergyService(baseEnergyProvider::get);
    }

    @Bean
    @Scope("prototype")
    public ResourceRegion resourceRegion(ResourceService resourceService,
                                         SyncItemContainerServiceImpl syncItemContainerService,
                                         ItemTypeService itemTypeService) {
        return new ResourceRegion(resourceService, syncItemContainerService, itemTypeService);
    }

    @Bean
    public ResourceService resourceService(jakarta.inject.Provider<ResourceRegion> instance,
                                           GameLogicService gameLogicService,
                                           ItemTypeService itemTypeService,
                                           SyncItemContainerServiceImpl syncItemContainerService,
                                           InitializeService initializeService) {
        return new ResourceService(instance::get,
                gameLogicService,
                itemTypeService,
                syncItemContainerService,
                initializeService);
    }

    @Bean
    public TerrainService terrainService(jakarta.inject.Provider<NativeTerrainShapeAccess> nativeTerrainShapeAccess,
                                         TerrainTileFactory terrainTileFactory,
                                         TerrainTypeService terrainTypeService) {
        return new TerrainService(nativeTerrainShapeAccess::get, terrainTileFactory, terrainTypeService);
    }

    @Bean
    @Scope("prototype")
    public TerrainTileBuilder terrainTileBuilder(NativeTerrainShapeAccess nativeTerrainShapeAccess) {
        return new TerrainTileBuilder(nativeTerrainShapeAccess);
    }

    @Bean
    public TerrainTileFactory terrainTileFactory(jakarta.inject.Provider<TerrainTileBuilder> terrainTileBuilderInstance) {
        return new TerrainTileFactory(terrainTileBuilderInstance::get);
    }

    @Bean
    @Scope("prototype")
    public Path path(TerrainService terrainService) {
        return new Path(terrainService);
    }

    @Bean
    @Scope("prototype")
    public SyncPhysicalMovable syncPhysicalMovable(SyncItemContainerServiceImpl syncItemContainerService,
                                                   jakarta.inject.Provider<Path> instancePath) {
        return new SyncPhysicalMovable(syncItemContainerService, instancePath::get);
    }

    @Bean
    @Scope("prototype")
    public SyncPhysicalArea syncPhysicalArea(SyncItemContainerServiceImpl syncItemContainerService) {
        return new SyncPhysicalArea(syncItemContainerService);
    }

    @Bean
    @Scope("prototype")
    public SyncBoxItem syncBoxItem() {
        return new SyncBoxItem();
    }

    @Bean
    @Scope("prototype")
    public SyncResourceItem syncResourceItem(ResourceService resourceService) {
        return new SyncResourceItem(resourceService);
    }

    @Bean
    @Scope("prototype")
    public SyncItemContainer syncItemContainer(SyncService syncService,
                                               BaseItemService baseItemService,
                                               GameLogicService gameLogicService,
                                               TerrainService terrainService,
                                               SyncItemContainerServiceImpl syncItemContainerService) {
        return new SyncItemContainer(syncService,
                baseItemService,
                gameLogicService,
                terrainService,
                syncItemContainerService);
    }

    @Bean
    @Scope("prototype")
    public SyncHouse syncHouse() {
        return new SyncHouse();
    }

    @Bean
    @Scope("prototype")
    public SyncConsumer syncConsumer(BaseItemService baseItemService, EnergyService energyService) {
        return new SyncConsumer(baseItemService, energyService);
    }

    @Bean
    @Scope("prototype")
    public SyncGenerator syncGenerator(BaseItemService baseItemService, EnergyService energyService) {
        return new SyncGenerator(baseItemService, energyService);
    }

    @Bean
    @Scope("prototype")
    public SyncHarvester syncHarvester(BaseItemService baseItemService,
                                       ResourceService resourceService,
                                       GameLogicService gameLogicService) {
        return new SyncHarvester(baseItemService, resourceService, gameLogicService);
    }

    @Bean
    @Scope("prototype")
    public SyncBuilder syncBuilder(SyncService syncService,
                                   SyncItemContainerServiceImpl syncItemContainerService,
                                   TerrainService terrainService,
                                   BaseItemService baseItemService,
                                   GameLogicService gameLogicService,
                                   ItemTypeService itemTypeService) {
        return new SyncBuilder(syncService,
                syncItemContainerService,
                terrainService,
                baseItemService,
                gameLogicService,
                itemTypeService);
    }

    @Bean
    @Scope("prototype")
    public SyncFactory syncFactory(CommandService commandService,
                                   TerrainService terrainService,
                                   ItemTypeService itemTypeService,
                                   BaseItemService baseItemService,
                                   GameLogicService gameLogicService) {
        return new SyncFactory(commandService,
                terrainService,
                itemTypeService,
                baseItemService,
                gameLogicService);
    }


    @Bean
    @Scope("prototype")
    public SyncWeapon syncWeapon(SyncService syncService,
                                 PathingService pathingService,
                                 SyncItemContainerServiceImpl syncItemContainerService,
                                 ProjectileService projectileService,
                                 BaseItemService baseItemService) {
        return new SyncWeapon(syncService,
                pathingService,
                syncItemContainerService,
                projectileService,
                baseItemService);
    }

    @Bean
    @Scope("prototype")
    public SyncBaseItem syncBaseItem(SyncItemContainerServiceImpl syncItemContainerService,
                                     BoxService boxService,
                                     GameLogicService gameLogicService,
                                     CommandService commandService,
                                     BaseItemService baseItemService,
                                     jakarta.inject.Provider<SyncHouse> syncHouseProvider,
                                     jakarta.inject.Provider<SyncItemContainer> syncItemContainerProvider,
                                     jakarta.inject.Provider<SyncConsumer> syncConsumerProvider,
                                     jakarta.inject.Provider<SyncGenerator> syncGeneratorProvider,
                                     jakarta.inject.Provider<SyncHarvester> syncHarvesterProvider,
                                     jakarta.inject.Provider<SyncBuilder> syncBuilderProvider,
                                     jakarta.inject.Provider<SyncFactory> syncFactoryProvider,
                                     jakarta.inject.Provider<SyncWeapon> syncWeaponProvider) {
        return new SyncBaseItem(syncItemContainerService,
                boxService,
                gameLogicService,
                commandService,
                baseItemService,
                syncHouseProvider::get,
                syncItemContainerProvider::get,
                syncConsumerProvider::get,
                syncGeneratorProvider::get,
                syncHarvesterProvider::get,
                syncBuilderProvider::get,
                syncFactoryProvider::get,
                syncWeaponProvider::get);
    }

    @Bean
    public SyncItemContainerServiceImpl syncItemContainerServiceImpl(jakarta.inject.Provider<BotService> botService,
                                                                     jakarta.inject.Provider<GuardingItemService> guardingItemServiceInstanceInstance,
                                                                     TerrainService terrainService,
                                                                     jakarta.inject.Provider<SyncPhysicalMovable> syncPhysicalMovableInstance,
                                                                     jakarta.inject.Provider<SyncPhysicalArea> syncPhysicalAreaInstance,
                                                                     jakarta.inject.Provider<SyncBoxItem> syncBoxItemProvider,
                                                                     jakarta.inject.Provider<SyncResourceItem> syncResourceItemProvider,
                                                                     jakarta.inject.Provider<SyncBaseItem> syncBaseItemProvider) {
        return new SyncItemContainerServiceImpl(botService::get,
                guardingItemServiceInstanceInstance::get,
                terrainService,
                syncPhysicalMovableInstance::get,
                syncPhysicalAreaInstance::get,
                syncBoxItemProvider::get,
                syncResourceItemProvider::get,
                syncBaseItemProvider::get);
    }

    @Bean
    public ProjectileService projectileService(SyncItemContainerServiceImpl syncItemContainerService,
                                               GameLogicService gameLogicService,
                                               BaseItemService baseItemService) {
        return new ProjectileService(syncItemContainerService, gameLogicService, baseItemService);
    }

    @Bean
    public BoxService boxService(InventoryTypeService inventoryTypeService,
                                 GameLogicService gameLogicService,
                                 ItemTypeService itemTypeService,
                                 SyncItemContainerServiceImpl syncItemContainerService,
                                 InitializeService initializeService) {
        return new BoxService(inventoryTypeService,
                gameLogicService,
                itemTypeService,
                syncItemContainerService,
                initializeService);
    }

    @Bean
    public InventoryTypeService inventoryTypeService(InitializeService initializeService) {
        return new InventoryTypeService(initializeService);
    }

    @Bean
    public GameLogicService gameLogicService(jakarta.inject.Provider<GuardingItemService> guardingItemServiceInstance,
                                             jakarta.inject.Provider<BotService> botServiceInstance,
                                             jakarta.inject.Provider<QuestService> questServiceInstance) {
        return new GameLogicService(guardingItemServiceInstance::get, botServiceInstance::get, questServiceInstance::get);
    }

    @Bean
    public LevelService levelService(InitializeService initializeService) {
        return new LevelService(initializeService);
    }

    @Bean
    @Scope("prototype")
    public BaseItemPositionComparison baseItemPositionComparison(GameLogicService gameLogicService,
                                                                 BaseItemService baseItemService) {
        return new BaseItemPositionComparison(gameLogicService, baseItemService);
    }

    @Bean
    @Scope("prototype")
    public CountPositionComparison countPositionComparison(GameLogicService gameLogicService,
                                                           BaseItemService baseItemService) {
        return new CountPositionComparison(gameLogicService, baseItemService);
    }

    @Bean
    @Scope("prototype")
    public BaseItemCountComparison baseItemCountComparison(GameLogicService gameLogicService,
                                                           BotService botService,
                                                           BaseItemService baseItemService) {
        return new BaseItemCountComparison(gameLogicService, botService, baseItemService);
    }

    @Bean
    @Scope("prototype")
    public BaseItemTypeComparison baseItemTypeComparison(GameLogicService gameLogicService,
                                                         BotService botService,
                                                         BaseItemService baseItemService,
                                                         ItemTypeService itemTypeService) {
        return new BaseItemTypeComparison(gameLogicService, botService, baseItemService, itemTypeService);
    }

    @Bean
    @Scope("prototype")
    public InventoryItemCountComparison inventoryItemCountComparison(GameLogicService gameLogicService) {
        return new InventoryItemCountComparison(gameLogicService);
    }

    @Bean
    @Scope("prototype")
    public CountComparison countComparison(GameLogicService gameLogicService) {
        return new CountComparison(gameLogicService);
    }

    @Bean
    public QuestService questService(jakarta.inject.Provider<BaseItemPositionComparison> baseItemPositionComparisonProvider,
                                     jakarta.inject.Provider<CountPositionComparison> countPositionComparisonProvider,
                                     jakarta.inject.Provider<BaseItemCountComparison> baseItemCountComparisonProvider,
                                     jakarta.inject.Provider<BaseItemTypeComparison> baseItemTypeComparisonProvider,
                                     jakarta.inject.Provider<InventoryItemCountComparison> inventoryItemCountComparisonnProvider,
                                     jakarta.inject.Provider<CountComparison> countComparisonProvider,
                                     ItemTypeService itemTypeService) {
        return new QuestService(baseItemPositionComparisonProvider::get,
                countPositionComparisonProvider::get,
                baseItemCountComparisonProvider::get,
                baseItemTypeComparisonProvider::get,
                inventoryItemCountComparisonnProvider::get,
                countComparisonProvider::get, itemTypeService);
    }

    @Bean
    public ItemTypeService itemTypeService(InitializeService initializeService) {
        return new ItemTypeService(initializeService);
    }

    @Bean
    public BaseItemService baseItemService(SyncService syncService,
                                           GuardingItemService guardingItemService,
                                           TerrainService terrainService,
                                           BoxService boxService,
                                           InventoryTypeService inventoryTypeService,
                                           EnergyService energyService,
                                           ItemTypeService itemTypeService,
                                           LevelService levelService,
                                           SyncItemContainerServiceImpl syncItemContainerService,
                                           GameLogicService gameLogicService,
                                           InitializeService initializeService,
                                           QuestService questService) {
        return new BaseItemService(syncService,
                guardingItemService,
                terrainService,
                boxService,
                inventoryTypeService,
                energyService,
                itemTypeService,
                levelService,
                syncItemContainerService,
                gameLogicService,
                initializeService,
                questService);
    }

    @Bean
    public CommandService commandService(GuardingItemService guardingItemService,
                                         PlanetService planetService,
                                         ItemTypeService itemTypeService,
                                         SyncItemContainerServiceImpl syncItemContainerService,
                                         BoxService boxService,
                                         ResourceService resourceService,
                                         BaseItemService baseItemService,
                                         GameLogicService gameLogicService,
                                         PathingService pathingService) {
        return new CommandService(guardingItemService,
                planetService,
                itemTypeService,
                syncItemContainerService,
                boxService,
                resourceService,
                baseItemService,
                gameLogicService,
                pathingService);
    }

    @Bean
    public GuardingItemService guardingItemService(jakarta.inject.Provider<CommandService> commandService,
                                                   SyncItemContainerServiceImpl syncItemContainerService) {
        return new GuardingItemService(commandService::get, syncItemContainerService);
    }

    @Bean
    public PathingService pathingService(TerrainService terrainService,
                                         SyncItemContainerServiceImpl syncItemContainerService) {
        return new PathingService(terrainService, syncItemContainerService);
    }

    @Bean
    public PlanetService planetService(SyncService syncService,
                                       EnergyService energyService,
                                       ResourceService resourceService,
                                       TerrainService terrainService,
                                       SyncItemContainerServiceImpl syncItemContainerService,
                                       ProjectileService projectileService,
                                       BoxService boxService,
                                       QuestService questService,
                                       BaseItemService baseItemService,
                                       PathingService pathingService,
                                       SimpleExecutorService simpleExecutorService,
                                       InitializeService initializeService) {
        return new PlanetService(syncService,
                energyService,
                resourceService,
                terrainService,
                syncItemContainerService,
                projectileService,
                boxService,
                questService,
                baseItemService,
                pathingService,
                simpleExecutorService,
                initializeService);
    }
}
