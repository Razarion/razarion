import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, SEL: selecting the unit a tip is about. Run on the attack quest 365, the
 * first chain that starts with a unit that can drive away.
 */
describe('Tip test bed - select', () => {

  function base(bed: TipTestbed): { viper: Unit, extractor: Unit } {
    bed.own(ItemTypeId.FACTORY, 0, 0);
    const viper = bed.own(ItemTypeId.VIPER, 5, -6);
    const extractor = bed.bot(ItemTypeId.BOT_EXTRACTOR, 35, 0);
    return {viper, extractor};
  }

  tipCase('SEL-01 not selected, on screen: "Click to select" on it', bed => {
    const {viper} = base(bed);
    bed.lookAtUnit(viper);
    bed.activateQuest(365);
    bed.run(1500);

    bed.check(bed.showsOnly('Click to select', viper.id), '"Click to select" on the viper');
    bed.click(viper);
    bed.run(1500);
    bed.check(!bed.view().prompts.some(prompt => prompt.text === 'Click to select'), 'select prompt gone');
  });

  tipCase('SEL-02 not selected, scrolled away after being seen: the arrow points at it', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAtUnit(viper);
    bed.activateQuest(365);
    bed.run(1500);
    bed.lookAtUnit(extractor);
    bed.run(2000);

    bed.check(bed.arrowPointsAt(viper.x, viper.y), 'arrow to the viper');
    bed.lookAtUnit(viper);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', viper.id), '"Click to select" on the viper again');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): Nothing tells the tip where an own unit is that it has never had on screen: the worker has
   * no position query for own units (catalog W6). Reported 2026-09-18.
   */
  tipCase('SEL-03 not selected, never seen: the arrow points at it', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAtUnit(extractor);
    bed.activateQuest(365);
    bed.run(3000);

    bed.check(bed.arrowPointsAt(viper.x, viper.y), 'arrow to the viper');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): The select step is skipped correctly since c3f1ce3a7 - and the attack step after it is blind,
   * the same defect as ATK-05: it has no attacker position to search from.
   */
  tipCase('SEL-05 selected, off screen: the step is skipped, "Click to attack" follows', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAtUnit(viper);
    bed.click(viper);
    bed.lookAtUnit(extractor);
    bed.activateQuest(365);
    bed.run(2000);

    bed.check(bed.showsOnly('Click to attack', extractor.id), '"Click to attack" on the extractor');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): findActor() takes the first viper in the renderer's list, which is the oldest one - here the
   * one at the edge of the screen (decision Q6: the nearest).
   */
  tipCase('SEL-08 several vipers, none selected: the prompt is on the nearest one', bed => {
    const far = bed.own(ItemTypeId.VIPER, -10, 10); // the oldest, near the edge of the screen
    const {viper} = base(bed);
    bed.lookAt(5, -4); // viper at (5,-6) right in the centre
    bed.activateQuest(365);
    bed.run(1500);

    bed.check(far.id < viper.id, 'test setup: the far viper is the older one');
    bed.check(bed.showsOnly('Click to select', viper.id), '"Click to select" on the viper in the centre');
  });

  tipCase('SEL-10 another type selected: the prompt stays on the viper', bed => {
    const {viper} = base(bed);
    const harvester = bed.own(ItemTypeId.HARVESTER, 0, -8);
    bed.lookAtUnit(viper);
    bed.activateQuest(365);
    bed.run(1500);
    bed.click(harvester);
    bed.run(1500);

    bed.check(bed.showsOnly('Click to select', viper.id), '"Click to select" still on the viper');
  });

  /*
   * Was a defect of the chain while 365 had a group step: the backtrack stopped at it (fulfilled
   * with fewer than three vipers) and restarted the attack step - "Click to attack" with nothing
   * selected. Gone with the group field, which leaves 365 without that step.
   */
  tipCase('SEL-13 deselected in a later step: back to "Click to select"', bed => {
    const {viper} = base(bed);
    bed.lookAt(20, 0);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);
    bed.deselect();
    bed.run(3000);

    bed.check(bed.showsOnly('Click to select', viper.id), '"Click to select" on the viper');
  });
});
