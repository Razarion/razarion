import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, SLL: selling a building to move the base - quest 393 the factory, 401 the
 * dockyard (2026-09-25). On PROD 393 fell from 30/36 to 1/7 once the transport tips brought players
 * there who had been shown every step before; none of the six sold anything.
 *
 * The scene is the player just after the crossing: the builder on the far coast with the camera,
 * the factory and the dockyard back on the island, far out of view.
 */
describe('Tip test bed - sell', () => {

  interface Scene {
    builder: Unit;
    factory: Unit;
    dockyard: Unit;
  }

  function base(bed: TipTestbed): Scene {
    const factory = bed.own(ItemTypeId.FACTORY, 0, 0);
    const dockyard = bed.own(ItemTypeId.DOCKYARD, 12, 0);
    const builder = bed.own(ItemTypeId.BUILDER, 90, 60);
    bed.lookAt(90, 60);
    return {builder, factory, dockyard};
  }

  tipCase('SLL-01 the whole of quest 393: back to the island, select the factory, tap sell twice', bed => {
    const {factory} = base(bed);
    bed.activateQuest(393);
    bed.run(1500);
    bed.check(bed.arrowPointsAt(0, 0), 'arrow back to the factory');
    const marker = bed.questMarker.get();
    bed.check(marker !== null && marker.kind === 'point' && marker.x === 0 && marker.y === 0,
      `the minimap marks the factory, was ${JSON.stringify(marker)}`);

    bed.lookAt(0, 0);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', factory.id), '"Click to select" on the factory');

    bed.click(factory);
    bed.run(1500);
    bed.check(bed.view().sellHint, 'the hint on the sell button');

    bed.clickSell();
    bed.run(1000);
    bed.check(bed.view().sellHint, 'the hint stays for the second tap');

    bed.clickSell();
    bed.runUntil(() => bed.questPassed, 5000, 'quest 393 passing');
    bed.run(500);
    bed.check(!bed.view().sellHint, 'the hint is gone');
  });

  tipCase('SLL-02 quest 401 with the dockyard already selected: straight to the sell button', bed => {
    const {dockyard} = base(bed);
    bed.lookAt(6, 0);
    bed.click(dockyard);
    bed.activateQuest(401);
    bed.run(1500);

    bed.check(bed.view().sellHint, 'the hint on the sell button');
    bed.check(bed.view().prompts.length === 0, 'no prompt on the map');
  });

  tipCase('SLL-03 the wrong building selected: select the right one first', bed => {
    const {factory, dockyard} = base(bed);
    bed.lookAt(6, 0);
    bed.click(dockyard);
    bed.activateQuest(393);
    bed.run(1500);

    bed.check(!bed.view().sellHint, 'no sell hint on the dockyard');
    bed.check(bed.showsOnly('Click to select', factory.id), '"Click to select" on the factory');
  });
});
