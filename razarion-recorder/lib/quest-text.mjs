/**
 * What a quest asks for, in one line - the same words the backend funnel uses
 * (razarion-frontend/src/app/backend/tracking-container/quest-condition-text.ts), so a quest reads
 * the same in both. Most quests have no name; this is what tells them apart.
 */
export function questConditionText(condition, itemTypeNames) {
  if (!condition) return '';
  const comparison = condition.comparisonConfig;
  const text = triggerText(condition.conditionTrigger, comparison, itemTypeNames);
  if (!text) return '';
  return comparison?.timeSeconds ? `${text} within ${comparison.timeSeconds} s` : text;
}

function triggerText(trigger, comparison, names) {
  const count = comparison?.count ?? '?';
  switch (trigger) {
    case 'SYNC_ITEM_KILLED':
      return countOrTypes('Destroy', 'units or buildings', comparison, names);
    case 'SYNC_ITEM_CREATED':
      return countOrTypes('Build', 'units or buildings', comparison, names);
    case 'SELL':
      return countOrTypes('Sell', 'units or buildings', comparison, names);
    case 'SYNC_ITEM_POSITION':
      return `${countOrTypes('Place', 'units or buildings', comparison, names)} on a region`;
    case 'SYNC_ITEM_LOADED':
      return `${countOrTypes('Load', 'units', comparison, names)} into a transporter`;
    case 'LOADED_CONTAINER_POSITION':
      return `${countOrTypes('Sail', 'transporters', comparison, names)} loaded to a region`;
    case 'REGION_CLEARED':
      return 'Clear a region of own units and buildings';
    case 'HARVEST':
      return `Harvest ${count} razarion`;
    case 'BASE_KILLED':
      return `Destroy ${count} bases`;
    case 'BOX_PICKED':
      return `Pick up ${count} boxes`;
    case 'INVENTORY_ITEM_PLACED':
      return `Place ${count} inventory items`;
    case 'UNLOCKED':
      return `Unlock ${count} items`;
    default:
      return '';
  }
}

// The typed form wins over the plain count, the way the engine reads it (QuestService).
function countOrTypes(verb, anything, comparison, names) {
  const typeCount = comparison?.typeCount;
  if (typeCount) {
    const parts = Object.entries(typeCount).map(([id, n]) => `${n} ${names.get(Number(id)) ?? `item type ${id}`}`);
    if (parts.length) return `${verb} ${parts.join(' + ')}`;
  }
  if (comparison?.count) return `${verb} ${comparison.count} ${anything}`;
  return verb;
}
