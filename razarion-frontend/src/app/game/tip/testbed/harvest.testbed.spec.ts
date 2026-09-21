import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Resource, Unit} from './fake-world';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, HRV: the harvest quests 363 and 366.
 *
 * The scene: the harvester next to the origin, a Razarion field a few units away - both on screen
 * with the camera at home - unless a case moves them.
 */
describe('Tip test bed - harvest', () => {

  function base(bed: TipTestbed, fieldX = 8, fieldY = 5, amount = 50): { harvester: Unit, field: Resource } {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    const harvester = bed.own(ItemTypeId.HARVESTER, 0, -5);
    const field = bed.world.addResource(fieldX, fieldY, amount);
    bed.lookAt(0, 0);
    return {harvester, field};
  }

  tipCase('SEL-01 / HRV-01 / HRV-05 the whole of quest 363', bed => {
    const {harvester, field} = base(bed);
    bed.activateQuest(363);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', harvester.id), '"Click to select" on the harvester');

    bed.click(harvester);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to harvest', field.id), '"Click to harvest" on the field');

    bed.click(field);
    bed.run(2000);
    bed.check(bed.view().prompts.length === 0 && bed.view().arrowAngle === null, 'quiet while harvesting');
    bed.runUntil(() => bed.questPassed, 20000, 'quest 363 passing');
    bed.run(2000);
  });

  tipCase('HRV-02 field off screen: the arrow points at it', bed => {
    const {harvester, field} = base(bed, 40, 0);
    bed.click(harvester);
    bed.activateQuest(363);
    bed.run(2500);

    bed.check(bed.arrowPointsAt(field.x, field.y), 'arrow to the field');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): The harvest task reads the harvester's position from the rendered instance, and asserts it is
   * there when it asks the worker for the nearest field: off screen that throws, the retry timer
   * dies with it, and the tip is gone for the rest of the quest.
   */
  tipCase('HRV-03 harvester selected and off screen, field on screen: "Click to harvest" on it', bed => {
    const {harvester, field} = base(bed, 40, 0);
    bed.click(harvester);
    bed.lookAt(40, 0);
    bed.activateQuest(363);
    bed.run(2500);

    bed.check(bed.showsOnly('Click to harvest', field.id), '"Click to harvest" on the field');
  });

  tipCase('HRV-04 the prompted field disappears: the prompt moves to the next one', bed => {
    const {harvester} = base(bed);
    const second = bed.world.addResource(-6, 8, 50);
    bed.click(harvester);
    bed.activateQuest(363);
    bed.run(1500);
    const first = bed.onlyPrompt()!.itemId;
    bed.world.removeResource(first);
    bed.run(1500);

    const other = [...bed.world.resources.values()].find(resource => resource.id !== first)!;
    bed.check(other.id === second.id || other.id !== first, 'test setup');
    bed.check(bed.showsOnly('Click to harvest', other.id), '"Click to harvest" on the remaining field');
  });

  tipCase('HRV-06 quest 366: the field runs dry before the amount - "Click to harvest" on the next', bed => {
    const {harvester, field} = base(bed, 8, 5, 6);
    const second = bed.world.addResource(-6, 8, 50);
    bed.click(harvester);
    bed.activateQuest(366);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to harvest', field.id), '"Click to harvest" on the nearer field');
    bed.click(field);
    bed.runUntil(() => !bed.world.resources.has(field.id), 20000, 'first field empty');
    bed.run(3000);

    bed.check(bed.showsOnly('Click to harvest', second.id), '"Click to harvest" on the second field');
    bed.click(second);
    bed.runUntil(() => bed.questPassed, 20000, 'quest 366 passing');
  });

  tipCase('HRV-07 no field on the planet: nothing, then guidance once there is one', bed => {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    const harvester = bed.own(ItemTypeId.HARVESTER, 0, -5);
    bed.lookAt(0, 0);
    bed.click(harvester);
    bed.activateQuest(363);
    bed.run(3000);
    bed.check(!bed.hasGuidance(), 'nothing while there is no field');

    const field = bed.world.addResource(8, 5, 50);
    bed.run(2000);
    bed.check(bed.showsOnly('Click to harvest', field.id), '"Click to harvest" once the field is there');
  });

  /*
   * PROD 18.-20.09.2026, a signature the task chain never wrote: 363 and 366 report
   * SEND_HARVEST_COMMAND|CHAIN_THRASHING. A harvest round trip ends one step (IDLE_ITEM, the
   * harvester is working) and opens a lower one (SEND_HARVEST_COMMAND, the next field) - which is
   * exactly HRV-06, the harvest quests are nothing but that cycle. Three small fields inside the
   * ten-second window and the guide reports itself as a restart loop.
   */
  tipCase('X-09 quest 366, three fields run dry in a row: a work cycle is not a restart loop', bed => {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    const harvester = bed.own(ItemTypeId.HARVESTER, 0, -5);
    // Small and close: each round trip is over in about two seconds, three of them in the window.
    bed.world.addResource(2, -4, 1);
    bed.world.addResource(-2, -4, 1);
    bed.world.addResource(0, -8, 1);
    bed.lookAt(0, 0);
    bed.click(harvester);
    bed.activateQuest(366);

    for (let round = 1; round <= 3; round++) {
      bed.runUntil(() => bed.onlyPrompt()?.text === 'Click to harvest', 6000, `harvest prompt ${round}`);
      const fieldId = bed.onlyPrompt()!.itemId;
      bed.click({id: fieldId});
      bed.runUntil(() => !bed.world.resources.has(fieldId), 8000, `field ${round} empty`);
    }
    bed.run(1000);

    bed.check(!bed.stallReasons().includes('CHAIN_THRASHING'), 'no restart loop reported');
  });
});
