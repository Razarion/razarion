import {ComparisonConfig, ConditionConfig, ConditionTrigger} from '../../generated/razarion-share';

/**
 * What a quest asks for, in one line, for the funnel rows.
 *
 * The funnel used to name quests by id alone, and an id says nothing about why a row is the one
 * players fall off. Quest 386 reading "Build 1 Dockyard" is the whole explanation of its cliff:
 * it is the first building in the chain that has to stand in water.
 *
 * Deliberately not shared with the in-game quest dialog, which says the same thing in the player's
 * words. That one resolves item type names through the WASM engine's ItemTypeService, which does
 * not exist on this page - here the names come from the editor's objectNameIds, which are the
 * internal ones, and internal names are what a row is looked up by anyway.
 */
export function questConditionText(conditionConfig: ConditionConfig | undefined,
                                   itemTypeNames: Map<number, string>): string {
  if (!conditionConfig) {
    return '';
  }
  const comparison = conditionConfig.comparisonConfig;
  const text = triggerText(conditionConfig.conditionTrigger, comparison, itemTypeNames);
  if (!text) {
    return '';
  }
  const seconds = comparison?.timeSeconds;
  return seconds ? `${text} within ${seconds} s` : text;
}

function triggerText(trigger: ConditionTrigger | null,
                     comparison: ComparisonConfig | undefined,
                     itemTypeNames: Map<number, string>): string {
  switch (trigger) {
    case ConditionTrigger.SYNC_ITEM_KILLED:
      return countOrTypes('Destroy', 'units or buildings', comparison, itemTypeNames);
    case ConditionTrigger.SYNC_ITEM_CREATED:
      return countOrTypes('Build', 'units or buildings', comparison, itemTypeNames);
    case ConditionTrigger.SELL:
      return countOrTypes('Sell', 'units or buildings', comparison, itemTypeNames);
    case ConditionTrigger.SYNC_ITEM_POSITION:
      // The place is the point of this one - it is the quest that sends the player somewhere - but
      // a polygon does not fit in a row, so it is named rather than drawn.
      return `${countOrTypes('Place', 'units or buildings', comparison, itemTypeNames)} on a region`;
    case ConditionTrigger.SYNC_ITEM_LOADED:
      return `${countOrTypes('Load', 'units', comparison, itemTypeNames)} into a transporter`;
    case ConditionTrigger.LOADED_CONTAINER_POSITION:
      return `${countOrTypes('Sail', 'transporters', comparison, itemTypeNames)} loaded to a region`;
    case ConditionTrigger.HARVEST:
      return `Harvest ${comparison?.count ?? '?'} razarion`;
    case ConditionTrigger.BASE_KILLED:
      return `Destroy ${comparison?.count ?? '?'} bases`;
    case ConditionTrigger.BOX_PICKED:
      return `Pick up ${comparison?.count ?? '?'} boxes`;
    case ConditionTrigger.INVENTORY_ITEM_PLACED:
      return `Place ${comparison?.count ?? '?'} inventory items`;
    case ConditionTrigger.UNLOCKED:
      return `Unlock ${comparison?.count ?? '?'} items`;
    default:
      return '';
  }
}

/**
 * The typed form wins over the plain count, the way the engine reads it: QuestService takes
 * getTypeCount() before getCount(), so a quest carrying both is decided by the types alone and a
 * row that showed the count would be naming a number the engine never looks at.
 */
function countOrTypes(verb: string, anything: string,
                      comparison: ComparisonConfig | undefined,
                      itemTypeNames: Map<number, string>): string {
  const typeCount = comparison?.typeCount;
  if (typeCount) {
    const parts = Object.keys(typeCount)
      .map(key => `${typeCount[key]} ${itemTypeNames.get(Number(key)) ?? `item type ${key}`}`);
    if (parts.length > 0) {
      return `${verb} ${parts.join(' + ')}`;
    }
  }
  if (comparison?.count) {
    return `${verb} ${comparison.count} ${anything}`;
  }
  return verb;
}
