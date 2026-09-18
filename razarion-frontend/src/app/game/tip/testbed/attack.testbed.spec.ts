import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase, tipRegression} from './tip-case';

/**
 * Catalog section 5, ATK and GRP: the attack quests 365, 379 and 388.
 *
 * The scene is quest 365 on planet 117 in small: the player's base around the origin, the bot's
 * unarmed extractor 35 units east - off screen and outside the render box while the camera is on
 * the base.
 */
describe('Tip test bed - attack', () => {

  function base(bed: TipTestbed): { viper: Unit, extractor: Unit } {
    bed.own(ItemTypeId.FACTORY, 0, 0);
    const viper = bed.own(ItemTypeId.VIPER, 5, -6);
    const extractor = bed.bot(ItemTypeId.BOT_EXTRACTOR, 35, 0);
    return {viper, extractor};
  }

  tipCase('ATK-01 target on screen: "Click to attack" on it, and the quest passes', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAt(20, 0); // viper and extractor both on screen
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);

    bed.check(bed.showsOnly('Click to attack', extractor.id), '"Click to attack" on the extractor');
    bed.click(extractor);
    bed.runUntil(() => bed.questPassed, 20000, 'quest 365 passing');
    bed.run(2000);
  });

  tipCase('ATK-02 target off screen: the arrow points at it', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAtUnit(viper);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(2500);

    bed.check(bed.arrowPointsAt(extractor.x, extractor.y), 'arrow to the extractor');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): The attack task knows where its attacker is only while the attacker is on screen: it never
   * samples the position, and the select step that does was skipped because the viper was already
   * selected. Scrolled to the target, it finds neither attacker nor target and shows nothing.
   */
  tipCase('ATK-05 selected viper, player scrolls to the target: "Click to attack" there, quiet on arrival', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAtUnit(viper);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);
    bed.lookAtUnit(extractor); // the viper is off screen now, still selected
    bed.run(1500);

    bed.check(bed.showsOnly('Click to attack', extractor.id), '"Click to attack" on the extractor');
    bed.click(extractor);
    bed.runUntil(() => bed.questPassed, 30000, 'quest 365 passing');
    bed.run(2000);
  });

  tipCase('ATK-06 / SEL-03 attacker never seen, nothing selected: the arrow points at it', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAtUnit(extractor);
    bed.activateQuest(365);
    bed.run(3000);

    bed.check(bed.arrowPointsAt(viper.x, viper.y), 'arrow to the viper');
  });

  tipCase('ATK-03 target dies by another hand: the prompt moves to the next target within a second', bed => {
    const {viper, extractor} = base(bed);
    const second = bed.bot(ItemTypeId.BOT_EXTRACTOR, 30, 8);
    bed.lookAt(20, 0);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);
    const first = bed.onlyPrompt()!.itemId;
    bed.world.kill(first);
    bed.run(1500);

    const other = first === extractor.id ? second.id : extractor.id;
    bed.check(bed.showsOnly('Click to attack', other), '"Click to attack" moved to the other extractor');
  });

  tipCase('ATK-07 the last viper dies: select the factory, build a viper, attack again', bed => {
    const {viper} = base(bed);
    bed.lookAtUnit(viper);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);
    bed.world.kill(viper.id);
    bed.run(3000);

    const prompt = bed.onlyPrompt();
    bed.check(prompt?.text === 'Click to select' && prompt.typeId === ItemTypeId.FACTORY, '"Click to select" on the factory');
  });

  tipCase('ATK-09 no target on the planet: nothing, then guidance once it is back', bed => {
    bed.own(ItemTypeId.FACTORY, 0, 0);
    const viper = bed.own(ItemTypeId.VIPER, 5, -6);
    const extractor = bed.bot(ItemTypeId.BOT_EXTRACTOR, 35, 0, 5000);
    bed.world.kill(extractor.id);
    bed.lookAtUnit(viper);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(3000);
    bed.check(!bed.hasGuidance(), 'nothing while the target is gone');

    bed.run(4000); // respawned
    bed.check(bed.hasGuidance(), 'guidance once it is back');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): The click callback sits on the marked extractor only. An attack on the other one is an attack
   * all the same and the quest counts it, but the tip keeps asking for the marked one.
   */
  tipCase('ATK-10a another extractor than the marked one: counts, the tip goes quiet', bed => {
    const {viper, extractor} = base(bed);
    const second = bed.bot(ItemTypeId.BOT_EXTRACTOR, 30, 8);
    bed.lookAt(20, 0);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);
    const marked = bed.onlyPrompt()!.itemId;
    bed.click(marked === extractor.id ? second : extractor);
    bed.run(1500);

    bed.check(bed.view().prompts.length === 0, 'no prompt while the viper attacks');
    bed.runUntil(() => bed.questPassed, 20000, 'quest 365 passing');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): Asked for a group from 2026-09-16, when 365 was given a target type and the factory tied the
   * group to that. Now the group is a field of the tip config, set on 379 only.
   */
  tipCase('GRP-05 the first attack asks for no group, even with three vipers', bed => {
    const {viper} = base(bed);
    bed.own(ItemTypeId.VIPER, 7, -6);
    bed.own(ItemTypeId.VIPER, 9, -6);
    bed.lookAtUnit(viper);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(2000);

    bed.check(!bed.view().groupAsked, 'no group tip');
  });

  tipCase('GRP-02 quest 379 with three vipers asks for the group', bed => {
    const {viper} = base(bed);
    bed.own(ItemTypeId.VIPER, 7, -6);
    bed.own(ItemTypeId.VIPER, 9, -6);
    bed.bot(ItemTypeId.BOT_REFINERY, 60, 20);
    bed.lookAtUnit(viper);
    bed.click(viper);
    bed.activateQuest(379);
    bed.run(2000);

    bed.check(bed.view().groupAsked, 'group tip up');
  });

  /*
   * Reported 2026-09-18: a viper selected, the camera on the extractor, the quest activated, the
   * extractor clicked - and "Click to select" on the viper as it arrived. The select step asked the
   * rendered instance whether the viper was selected, and off screen there is none. Fixed in
   * c3f1ce3a7; with the fix reverted this reports R2 and R3 at 3.8 s. The same scene still breaks
   * R1 - no "Click to attack" - which is SEL-05.
   */
  tipRegression('2026-09-18 quest activated with the selected viper off screen: no select prompt on arrival',
    ['R2', 'R3'], bed => {
      const {viper, extractor} = base(bed);
      bed.lookAt(20, 0);
      bed.click(viper);
      bed.lookAtUnit(extractor);
      bed.activateQuest(365);
      bed.run(1500);
      bed.click(extractor);
      bed.runUntil(() => bed.questPassed, 30000, 'quest 365 passing');
    });

  /*
   * After the attack the chain waits in the idle task, and that task points the direction arrow at
   * the viper for as long as it is off screen - an invitation to scroll there and click it while
   * it is doing exactly what it was told (decision Q2: show nothing).
   */
  tipCase('IDL-02 attacker works off screen: no arrow to it', bed => {
    const {viper, extractor} = base(bed);
    bed.lookAt(20, 0);
    bed.click(viper);
    bed.activateQuest(365);
    bed.run(1500);
    bed.lookAtUnit(extractor);
    bed.run(1500);
    bed.click(extractor);
    bed.runUntil(() => bed.questPassed, 30000, 'quest 365 passing');
  });
});
