import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase, tipGraceful} from './tip-case';

/**
 * Catalog section 5, FAB: the fabricate quests 359, 364, 369 and 387.
 *
 * The scene is the base after quest 358: a factory at the origin, the camera on it.
 */
describe('Tip test bed - fabricate', () => {

  function base(bed: TipTestbed): Unit {
    const factory = bed.own(ItemTypeId.FACTORY, 0, 0);
    bed.lookAt(0, 0);
    return factory;
  }

  tipCase('SEL-01 / FAB-01 the whole of quest 359', bed => {
    const factory = base(bed);
    bed.activateQuest(359);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', factory.id), '"Click to select" on the factory');

    bed.click(factory);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.HARVESTER, 'hint on the harvester button');

    bed.clickBuildButton(ItemTypeId.HARVESTER);
    bed.run(2000);
    bed.check(bed.view().prompts.length === 0 && bed.view().cockpitHintTypeId === null, 'quiet while fabricating');
    bed.runUntil(() => bed.questPassed, 20000, 'quest 359 passing');
    bed.run(2000);
  });

  tipCase('FAB-04 factory selected, camera elsewhere: the hint is on the button', bed => {
    const factory = base(bed);
    bed.click(factory);
    bed.lookAt(-60, 0);
    bed.activateQuest(359);
    bed.run(2000);

    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.HARVESTER, 'hint on the harvester button');
  });

  tipCase('FAB-06 quest 369: after each viper the hint comes back, without a detour through select', bed => {
    const factory = base(bed);
    bed.own(ItemTypeId.VIPER, 8, -6); // from quest 364, counts towards the three
    bed.click(factory);
    bed.activateQuest(369);
    bed.run(1500);

    for (let viper = 2; viper <= 3; viper++) {
      bed.check(bed.view().cockpitHintTypeId === ItemTypeId.VIPER, `hint on the viper button for viper ${viper}`);
      bed.clickBuildButton(ItemTypeId.VIPER);
      bed.runUntil(() => bed.world.ownCount(ItemTypeId.VIPER) === viper, 20000, `viper ${viper} built`);
      bed.run(2500);
      bed.check(!bed.view().prompts.some(prompt => prompt.text === 'Click to select'), 'no detour through select');
    }
    bed.runUntil(() => bed.questPassed, 5000, 'quest 369 passing');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): After the click the chain waits in the idle task until the factory is done - and the idle task
   * can only read a factory that is on screen. With the camera elsewhere it points the arrow at the
   * factory the whole time, and the hint for the next viper never comes, although the cockpit and
   * its button stay right there.
   */
  tipCase('FAB-07 quest 369, the camera leaves after the click: the hint for the next viper comes anyway', bed => {
    const factory = base(bed);
    bed.own(ItemTypeId.VIPER, 8, -6);
    bed.click(factory);
    bed.activateQuest(369);
    bed.run(1500);
    bed.clickBuildButton(ItemTypeId.VIPER);
    bed.run(500);
    bed.lookAt(-60, 0);
    bed.runUntil(() => bed.world.ownCount(ItemTypeId.VIPER) === 2, 20000, 'second viper built');
    bed.run(3000);

    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.VIPER, 'hint on the viper button');
  });

  tipCase('FAB-01 quest 387: dockyard and hydra work the same way', bed => {
    const dockyard = bed.own(ItemTypeId.DOCKYARD, 0, 0);
    bed.lookAt(0, 0);
    bed.activateQuest(387);
    bed.run(1500);
    bed.click(dockyard);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.HYDRA, 'hint on the hydra button');
    bed.clickBuildButton(ItemTypeId.HYDRA);
    bed.runUntil(() => bed.questPassed, 20000, 'quest 387 passing');
  });

  /*
   * Quest 389 (level 8, the transporter) was missing from the test bed, and PROD shows why it has
   * to be in: SELECT|ACTOR_NOT_FOUND on 389 went from 11 records to 1 with the guide, and
   * SELECT|AWAIT_SELECTION appeared in its place. That is the improvement written down - the tip
   * now finds the dockyard and asks for it, where the chain claimed there was nothing to point at -
   * and the case makes sure the reason stays that one.
   */
  tipCase('FAB-08 quest 389: select the dockyard, then the hint on the transporter button', bed => {
    const dockyard = bed.own(ItemTypeId.DOCKYARD, 0, 0);
    bed.lookAt(0, 0);
    bed.activateQuest(389);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', dockyard.id), '"Click to select" on the dockyard');

    bed.run(31000); // the player does not act: the watchdog names what the tip is waiting for
    bed.check(bed.stallReasons().every(reason => reason === 'AWAIT_SELECTION'),
      `AWAIT_SELECTION and nothing else, got ${bed.stallReasons().join(', ') || 'nothing'}`);

    bed.click(dockyard);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.TRANSPORTER, 'hint on the transporter button');
    bed.clickBuildButton(ItemTypeId.TRANSPORTER);
    bed.runUntil(() => bed.questPassed, 20000, 'quest 389 passing');
  });

  tipGraceful('FAB-02 too little Razarion: no hint on a disabled button', bed => {
    const factory = base(bed);
    bed.world.razarion = 5;
    bed.click(factory);
    bed.activateQuest(359);
    bed.run(3000);

    bed.check(bed.view().cockpitHintTypeId === null, 'no hint on the disabled button');
  });
});
