import {questConditionText} from './quest-condition-text';
import {ComparisonConfig, ConditionConfig, ConditionTrigger} from '../../generated/razarion-share';

describe('questConditionText', () => {
  const NAMES = new Map<number, string>([[3, 'Viper'], [11, 'Dockyard'], [22, '(Bot1) Extractor']]);

  function condition(trigger: ConditionTrigger, comparison: Partial<ComparisonConfig>): ConditionConfig {
    return {conditionTrigger: trigger, comparisonConfig: comparison as ComparisonConfig};
  }

  it('names the type a kill quest asks for', () => {
    expect(questConditionText(
      condition(ConditionTrigger.SYNC_ITEM_KILLED, {typeCount: {22: 1}}), NAMES))
      .toBe('Destroy 1 (Bot1) Extractor');
  });

  it('falls back to the plain count when the quest names no type', () => {
    expect(questConditionText(
      condition(ConditionTrigger.SYNC_ITEM_KILLED, {count: 1}), NAMES))
      .toBe('Destroy 1 units or buildings');
  });

  /**
   * QuestService takes getTypeCount() before getCount(), so a quest carrying both is decided by
   * the types alone. A row naming the count would be naming a number the engine never reads -
   * which is exactly how quest 365 came to look like "destroy anything" in the funnel while the
   * tip was pointing at whatever stood nearest.
   */
  it('lets the type win over a count the engine would ignore', () => {
    expect(questConditionText(
      condition(ConditionTrigger.SYNC_ITEM_KILLED, {count: 1, typeCount: {22: 1}}), NAMES))
      .toBe('Destroy 1 (Bot1) Extractor');
  });

  it('says a build quest is a build quest', () => {
    expect(questConditionText(
      condition(ConditionTrigger.SYNC_ITEM_CREATED, {typeCount: {3: 3}}), NAMES))
      .toBe('Build 3 Viper');
  });

  it('marks the quest that sends the player to a place', () => {
    expect(questConditionText(
      condition(ConditionTrigger.SYNC_ITEM_POSITION, {typeCount: {11: 1}}), NAMES))
      .toBe('Place 1 Dockyard on a region');
  });

  it('carries a time limit where there is one', () => {
    expect(questConditionText(
      condition(ConditionTrigger.HARVEST, {count: 50, timeSeconds: 120}), NAMES))
      .toBe('Harvest 50 razarion within 120 s');
  });

  /**
   * An item type the names do not cover still has to read as something. Showing the bare id beats
   * an empty row, and it is the id that a lookup in the editor starts from.
   */
  it('falls back to the type id when the name is unknown', () => {
    expect(questConditionText(
      condition(ConditionTrigger.SYNC_ITEM_KILLED, {typeCount: {999: 2}}), NAMES))
      .toBe('Destroy 2 item type 999');
  });

  it('says nothing rather than guessing when there is no condition', () => {
    expect(questConditionText(undefined, NAMES)).toBe('');
  });
});
