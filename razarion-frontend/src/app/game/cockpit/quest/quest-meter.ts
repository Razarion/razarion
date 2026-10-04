import {TipItemState} from '../../../gwtangular/GwtAngularFacade';

/**
 * The quest progress as a meter, drawn like the power meter in the main cockpit (2026-10-04).
 *
 * The rows used to be text only - "Viper created 1 of 3" - and a count of created items does not
 * move while the item is being made: a factory site stands for 8 s, a radar for 15 s, at the
 * same number. The meter fills the next cell with what is under construction, so the waiting
 * shows as something growing.
 */
export interface QuestMeter {
  /**
   * The segments of the bar, each filled 0..1 from the left: one per item, or a single one for
   * a target too large to count in segments, such as razarion to harvest.
   */
  cells: number[];
  /** The whole row, 0..1. */
  fraction: number;
}

/** Up to this many items get a cell each; beyond, the cells would be too thin to count. */
export const QUEST_METER_MAX_CELLS = 10;

/**
 * @param actual what the quest has counted
 * @param target what it wants
 * @param inProgress how far each item under construction is, 0..1
 */
export function questMeter(actual: number, target: number, inProgress: number[] = []): QuestMeter | null {
  if (!(target > 0)) {
    return null;
  }
  const whole = Math.max(0, Math.min(actual, target));
  // The furthest along first: they are the ones about to be counted.
  const partial = inProgress
    .filter(progress => progress > 0)
    .map(progress => Math.min(progress, 1))
    .sort((a, b) => b - a)
    .slice(0, target - whole);
  const fraction = Math.min(1, (whole + partial.reduce((sum, progress) => sum + progress, 0)) / target);
  if (target > QUEST_METER_MAX_CELLS) {
    return {cells: [fraction], fraction};
  }
  const cells: number[] = [];
  for (let i = 0; i < target; i++) {
    cells.push(i < whole ? 1 : (partial[i - whole] ?? 0));
  }
  return {cells, fraction};
}

/**
 * How far each own item of the given type is that is being made: a building by the buildup of
 * its construction site, a unit by the production of the factory making it.
 *
 * A builder reports the site it works on as well, so for a type that has sites only the sites
 * count - otherwise a factory site with its builder would fill two cells.
 */
export function inProgressOf(itemTypeId: number, items: TipItemState[]): number[] {
  const own = items.filter(item => item.own);
  const sites = own.filter(item => item.itemTypeId === itemTypeId && item.buildup < 1);
  if (sites.length > 0) {
    return sites.map(site => site.buildup);
  }
  return own
    .filter(item => item.constructingTypeId === itemTypeId)
    .map(item => item.constructing);
}
