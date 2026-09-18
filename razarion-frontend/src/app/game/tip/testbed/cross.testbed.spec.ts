import {ItemTypeId} from './fake-item-types';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, X: what holds for every tip.
 */
describe('Tip test bed - cross-cutting', () => {

  tipCase('X-01 quest changes in the middle of a step: the old prompt is gone', bed => {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    const viper = bed.own(ItemTypeId.VIPER, 5, -6);
    const harvester = bed.own(ItemTypeId.HARVESTER, 0, -5);
    const extractor = bed.bot(ItemTypeId.BOT_EXTRACTOR, 30, 8);
    bed.world.addResource(-5, 8, 50);
    bed.lookAt(15, 0);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to attack', extractor.id), 'test setup: "Click to attack" up');

    bed.activateQuest(363);
    bed.run(1500);
    bed.check(!bed.view().prompts.some(prompt => prompt.text === 'Click to attack'), 'the attack prompt is gone');
    bed.check(bed.showsOnly('Click to select', harvester.id), '"Click to select" on the harvester');
  });

  tipCase('X-03 tips switched off: everything is gone at once', bed => {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    const viper = bed.own(ItemTypeId.VIPER, 5, -6);
    bed.bot(ItemTypeId.BOT_EXTRACTOR, 35, 0);
    bed.lookAt(0, 0);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(2000);
    bed.check(bed.view().arrowAngle !== null, 'test setup: arrow up');

    bed.setTipsVisible(false);
    bed.quest = null; // the quest stays open, but tips are the player's choice now
    bed.run(500);
    bed.check(!bed.hasGuidance(), 'nothing on screen');
  });

  tipCase('IDL-05 without SharedArrayBuffer the order arrives late: no step back', bed => {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    const harvester = bed.own(ItemTypeId.HARVESTER, 0, -5);
    const field = bed.world.addResource(8, 5, 50);
    bed.world.commandLatencyMillis = 2500;
    bed.lookAt(0, 0);
    bed.click(harvester);
    bed.activateQuest(366);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to harvest', field.id), 'test setup: "Click to harvest" up');
    bed.click(field);
    bed.run(2000);

    bed.check(!bed.view().prompts.some(prompt => prompt.text === 'Click to harvest'), 'no second "Click to harvest"');
    bed.runUntil(() => bed.questPassed, 30000, 'quest 366 passing');
  });
});
