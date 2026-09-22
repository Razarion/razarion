/**
 * The item types of the tipped quests, with the numbers that matter to a tip: ids as in the game,
 * prices, ranges and speeds close enough to the planet 117 configuration that a fight or a drive
 * takes about as long as it does there.
 */
export interface FakeItemTypeSpec {
  id: number;
  name: string;
  price: number;
  radius: number;
  /** Ground units per second; 0 for a building. */
  speed: number;
  health: number;
  weapon?: { range: number, damage: number, reloadSeconds: number };
  harvester?: { range: number, progressPerSecond: number };
  /** Builder: the buildings it can place. */
  builds?: number[];
  /** Factory: the units it fabricates. */
  fabricates?: number[];
  /** Container: how far from it a unit can be loaded or put down, and what it carries. */
  container?: { range: number, carries: number[] };
  /** Seconds from start to finished, for buildings and fabricated units alike. */
  buildSeconds: number;
}

export const ItemTypeId = {
  BUILDER: 1,
  HARVESTER: 2,
  VIPER: 3,
  FACTORY: 4,
  RADAR: 6,
  POWERPLANT: 7,
  DOCKYARD: 11,
  HYDRA: 12,
  TRANSPORTER: 18,
  BOT_EXTRACTOR: 22,
  BOT_REFINERY: 24,
  BOT_TESLA: 25,
  BOT_HYDRA: 26
} as const;

const specs: FakeItemTypeSpec[] = [
  {
    id: ItemTypeId.BUILDER, name: 'Builder', price: 50, radius: 1, speed: 4, health: 20, buildSeconds: 5,
    builds: [ItemTypeId.FACTORY, ItemTypeId.RADAR, ItemTypeId.POWERPLANT, ItemTypeId.DOCKYARD]
  },
  {
    id: ItemTypeId.HARVESTER, name: 'Harvester', price: 15, radius: 1, speed: 4, health: 12, buildSeconds: 5,
    harvester: {range: 2, progressPerSecond: 1}
  },
  {
    id: ItemTypeId.VIPER, name: 'Viper', price: 10, radius: 1, speed: 6, health: 10, buildSeconds: 5,
    weapon: {range: 10, damage: 5, reloadSeconds: 1}
  },
  {
    id: ItemTypeId.FACTORY, name: 'Factory', price: 35, radius: 3, speed: 0, health: 40, buildSeconds: 10,
    fabricates: [ItemTypeId.BUILDER, ItemTypeId.HARVESTER, ItemTypeId.VIPER]
  },
  {id: ItemTypeId.RADAR, name: 'Radar', price: 35, radius: 2, speed: 0, health: 30, buildSeconds: 10},
  {id: ItemTypeId.POWERPLANT, name: 'Powerplant', price: 35, radius: 2, speed: 0, health: 30, buildSeconds: 10},
  {
    id: ItemTypeId.DOCKYARD, name: 'Dockyard', price: 35, radius: 3, speed: 0, health: 40, buildSeconds: 10,
    fabricates: [ItemTypeId.HYDRA, ItemTypeId.TRANSPORTER]
  },
  {
    id: ItemTypeId.HYDRA, name: 'Hydra', price: 13, radius: 1, speed: 5, health: 13, buildSeconds: 5,
    weapon: {range: 10, damage: 4, reloadSeconds: 1}
  },
  {
    id: ItemTypeId.TRANSPORTER, name: 'Transporter', price: 50, radius: 2, speed: 3, health: 25, buildSeconds: 8,
    container: {range: 20, carries: [ItemTypeId.BUILDER]}
  },
  {id: ItemTypeId.BOT_EXTRACTOR, name: '(Bot1) Extractor', price: 0, radius: 2, speed: 0, health: 15, buildSeconds: 0},
  {id: ItemTypeId.BOT_REFINERY, name: '(Bot1) Refinery', price: 0, radius: 3, speed: 0, health: 15, buildSeconds: 0},
  {
    id: ItemTypeId.BOT_TESLA, name: '(Bot1) Tesla', price: 0, radius: 2, speed: 0, health: 30, buildSeconds: 0,
    weapon: {range: 15, damage: 5, reloadSeconds: 1}
  },
  {
    id: ItemTypeId.BOT_HYDRA, name: '(Bot1) Hydra', price: 0, radius: 1, speed: 0, health: 13, buildSeconds: 0,
    weapon: {range: 10, damage: 4, reloadSeconds: 1}
  }
];

export function itemTypeSpec(id: number): FakeItemTypeSpec {
  const spec = specs.find(candidate => candidate.id === id);
  if (!spec) {
    throw new Error(`No fake item type ${id}`);
  }
  return spec;
}

/**
 * The BaseItemType the renderer, the selection and the action service read. Only what they call.
 */
export function fakeBaseItemType(id: number): any {
  const spec = itemTypeSpec(id);
  return {
    getId: () => spec.id,
    getName: () => spec.name,
    getInternalName: () => spec.name,
    getPrice: () => spec.price,
    getWeaponType: () => spec.weapon ? {checkItemTypeDisallowed: () => false} : null,
    getHarvesterType: () => spec.harvester ? {} : null,
    getBuilderType: () => spec.builds
      ? {checkAbleToBuild: (typeId: number) => spec.builds!.includes(typeId)}
      : null,
    getFactoryType: () => spec.fabricates ? {getAbleToBuildIds: () => spec.fabricates} : null,
    getItemContainerType: () => spec.container
      ? {isAbleToContain: (typeId: number) => spec.container!.carries.includes(typeId), getRange: () => spec.container!.range}
      : null,
    getPhysicalAreaConfig: () => ({
      fulfilledMovable: () => spec.speed > 0,
      getRadius: () => spec.radius
    })
  };
}
