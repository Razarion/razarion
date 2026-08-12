import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ItemCockpitComponent} from './item-cockpit.component';
import {BuildupItemModel, ItemCockpitService, OtherItemCockpitModel, OwnItemCockpitModel} from './item-cockpit.service';
import {UserService} from '../../../auth/user.service';
import {TipService} from '../../tip/tip.service';
import {TipStallReason} from '../../tip/tip-stall';
import {CompactLayoutService} from '../compact-layout.service';

/**
 * The real service reads matchMedia, and the Karma context frame is small enough to match the
 * compact query - which would put every tip test into the phone branch by accident.
 */
function compactLayoutStub(compact = false, openPanel = false): Partial<CompactLayoutService> {
  return {compact: (() => compact) as any, isOpen: () => openPanel};
}

describe('ItemCockpitComponent resource title', () => {
  function createFixture(otherItemCockpit: OtherItemCockpitModel): ComponentFixture<ItemCockpitComponent> {
    const itemCockpitServiceStub: Partial<ItemCockpitService> = {
      ownItemCockpit: null,
      ownMultipleItems: null,
      otherItemCockpit,
      count: 1
    };
    TestBed.configureTestingModule({
      imports: [ItemCockpitComponent],
      providers: [
        {provide: ItemCockpitService, useValue: itemCockpitServiceStub},
        {provide: UserService, useValue: {isAdmin: () => false}},
        {provide: TipService, useValue: {setItemCockpit: () => {}}},
        {provide: CompactLayoutService, useValue: compactLayoutStub()},
      ]
    });
    const fixture = TestBed.createComponent(ItemCockpitComponent);
    fixture.detectChanges();
    return fixture;
  }

  function baseResource(): OtherItemCockpitModel {
    return {
      id: 1,
      imageUrl: '',
      itemTypeName: 'Razarion Spot',
      itemTypeDescr: 'A rich razarion field',
      baseName: '',
      friend: false,
      bot: false,
      resource: true,
      resourceAmount: 5000,
      box: false,
      health: null
    };
  }

  it('renders the config razarion amount in the title', () => {
    const fixture = createFixture(baseResource());
    // The title is the first div under the other-item panel.
    const title = fixture.nativeElement.querySelector('.item-cockpit-title') as HTMLElement;
    expect(title).toBeTruthy();
    expect(title.textContent).toContain('Razarion: 5000');
  });

  it('falls back to baseName in the title for non-resource items', () => {
    const other = baseResource();
    other.resource = false;
    other.resourceAmount = null;
    other.baseName = 'Enemy Base';
    const fixture = createFixture(other);
    const title = fixture.nativeElement.querySelector('.item-cockpit-title') as HTMLElement;
    expect(title.textContent).toContain('Enemy Base');
    expect(title.textContent).not.toContain('Razarion:');
  });
});

describe('ItemCockpitComponent buildup tip', () => {
  function buildupItem(itemTypeId: number, enabled: boolean): BuildupItemModel {
    return {
      imageUrl: '',
      itemTypeId,
      itemTypeName: `Type ${itemTypeId}`,
      price: 35,
      itemCount: 0,
      itemLimit: 1,
      enabled,
      buildLimitReached: !enabled,
      buildHouseSpaceReached: false,
      buildNoMoney: false
    };
  }

  function createFixture(buildupItems: BuildupItemModel[], factoryQueueFull = false,
                         compact: Partial<CompactLayoutService> = compactLayoutStub(),
                         canSell = false): ComponentFixture<ItemCockpitComponent> {
    const ownItemCockpit: OwnItemCockpitModel = {
      imageUrl: '',
      itemTypeName: 'Builder',
      itemTypeDescr: '',
      buildupItems,
      containerCount: null,
      containerId: null,
      canSell,
      buildupProgress: null,
      health: 1,
      factoryId: null,
      factoryQueue: null,
      factoryQueueFull
    };
    const itemCockpitServiceStub: Partial<ItemCockpitService> = {
      ownItemCockpit,
      ownMultipleItems: null,
      otherItemCockpit: null,
      count: 1,
      onBuild: () => {},
      onSell: () => {}
    };
    TestBed.configureTestingModule({
      imports: [ItemCockpitComponent],
      providers: [
        {provide: ItemCockpitService, useValue: itemCockpitServiceStub},
        {provide: UserService, useValue: {isAdmin: () => false}},
        {provide: TipService, useValue: {setItemCockpit: () => {}}},
        {provide: CompactLayoutService, useValue: compact},
      ]
    });
    const fixture = TestBed.createComponent(ItemCockpitComponent);
    fixture.detectChanges();
    return fixture;
  }

  /** The button the tip is measured against, with a rect of its own - jsdom reports zeroes. */
  function stubButtonRect(component: ItemCockpitComponent, ...rects: {left: number, top: number, width: number}[]) {
    const button = component.buildupItemDiv!.first.nativeElement as HTMLElement;
    spyOn(button, 'getBoundingClientRect').and.returnValues(
      ...rects.map(rect => ({...rect, height: 60} as DOMRect)));
    return button;
  }

  it('anchors the tip to the top centre of a clickable button', () => {
    const fixture = createFixture([buildupItem(4, true)]);
    const component = fixture.componentInstance;
    stubButtonRect(component, {left: 100, top: 200, width: 60});
    expect(component.showBuildupTip(4)).toBe(true);
    expect(component.buildTip).toEqual({x: 130, y: 200, mode: 'build'});
  });

  it('renders the tip where it was anchored', () => {
    const fixture = createFixture([buildupItem(4, true)]);
    const component = fixture.componentInstance;
    stubButtonRect(component, {left: 100, top: 200, width: 60});
    expect(component.showBuildupTip(4)).toBe(true);
    fixture.detectChanges();
    const tip = fixture.nativeElement.querySelector('.build-tip') as HTMLElement;
    expect(tip).toBeTruthy();
    expect(tip.textContent).toContain('Click to build');
    expect(tip.style.left).toBe('130px');
    expect(tip.style.top).toBe('200px');
  });

  it('reports failure instead of anchoring the tip to a disabled button', () => {
    const fixture = createFixture([buildupItem(4, false)]);
    expect(fixture.componentInstance.showBuildupTip(4)).toBe(false);
    expect(fixture.componentInstance.buildTip).toBeNull();
  });

  it('reports failure while the factory queue is full', () => {
    const fixture = createFixture([buildupItem(2, true)], true);
    expect(fixture.componentInstance.showBuildupTip(2)).toBe(false);
    expect(fixture.componentInstance.buildTip).toBeNull();
  });

  it('reports failure for an item type the selected builder cannot build', () => {
    const fixture = createFixture([buildupItem(4, true)]);
    expect(fixture.componentInstance.showBuildupTip(23)).toBe(false);
    expect(fixture.componentInstance.buildTip).toBeNull();
  });

  it('pages the carousel to the target button', () => {
    // The Builder's 6 types at 5 per page: House (23) sits on page 2, where the tip would
    // otherwise anchor to a clipped element.
    const fixture = createFixture([4, 6, 7, 11, 21, 23].map(id => buildupItem(id, true)));
    const component = fixture.componentInstance;
    expect(component.showBuildupTip(23)).toBe(true);
    expect(component.buildupCarousel).toBeTruthy();
    expect(component.buildupCarousel!.page).toBe(1);
  });

  it('re-measures the anchor on every call', () => {
    // The tip tasks call this every second to survive a cockpit rebuild: every selection event
    // rebuilds the cockpit model and the carousel re-renders, so the button this points at is
    // regularly somewhere else - or a different element altogether.
    const fixture = createFixture([buildupItem(4, true)]);
    const component = fixture.componentInstance;
    stubButtonRect(component, {left: 100, top: 200, width: 60}, {left: 300, top: 400, width: 60});
    expect(component.showBuildupTip(4)).toBe(true);
    expect(component.buildTip).toEqual({x: 130, y: 200, mode: 'build'});
    expect(component.showBuildupTip(4)).toBe(true);
    expect(component.buildTip).toEqual({x: 330, y: 400, mode: 'build'});
  });

  it('hides the tip when cleared', () => {
    const fixture = createFixture([buildupItem(4, true)]);
    const component = fixture.componentInstance;
    expect(component.showBuildupTip(4)).toBe(true);
    expect(component.showBuildupTip(null)).toBe(true);
    expect(component.buildTip).toBeNull();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.build-tip')).toBeNull();
  });

  it('names why the tip cannot be anchored', () => {
    // The reason is what a stalled tip reports - "showBuildupTip returned false" alone does not
    // say whether the player is blocked or the cockpit is simply not there yet.
    const fixture = createFixture([buildupItem(4, false), buildupItem(6, true)]);
    const component = fixture.componentInstance;
    expect(component.getBuildupTipBlockReason(4)).toBe(TipStallReason.BUTTON_DISABLED);
    expect(component.getBuildupTipBlockReason(23)).toBe(TipStallReason.NOT_BUILDABLE);
    expect(component.getBuildupTipBlockReason(6)).toBeNull();
  });

  it('names the full factory queue as the blocker', () => {
    const fixture = createFixture([buildupItem(2, true)], true);
    const component = fixture.componentInstance;
    expect(component.getBuildupTipBlockReason(2)).toBe(TipStallReason.FACTORY_QUEUE_FULL);
  });

  describe('a buildup button that cannot be pressed', () => {
    /** The tap has to land: a disabled button would swallow it and answer nothing. */
    function tapFirstButton(fixture: ComponentFixture<ItemCockpitComponent>): void {
      const button = fixture.nativeElement.querySelector('.item-cockpit-buildup-button') as HTMLButtonElement;
      expect(button.disabled).withContext('button must stay pressable').toBe(false);
      button.dispatchEvent(new MouseEvent('mousedown'));
      fixture.detectChanges();
    }

    function blockedText(fixture: ComponentFixture<ItemCockpitComponent>): string {
      return (fixture.nativeElement.querySelector('.item-cockpit-blocked-msg') as HTMLElement)?.textContent?.trim() ?? '';
    }

    it('says why instead of doing nothing', () => {
      const fixture = createFixture([buildupItem(4, false)]);
      spyOn(fixture.componentInstance.itemCockpitService, 'onBuild');
      tapFirstButton(fixture);
      expect(blockedText(fixture)).toContain('limit reached');
      expect(fixture.componentInstance.itemCockpitService.onBuild).not.toHaveBeenCalled();
    });

    it('does not tell a player to level up past a limit of zero', () => {
      // "0 of 0" is not an exhausted limit, it is a type this level cannot build at all - saying
      // "limit reached" would send the player looking for something to destroy.
      const item = buildupItem(4, false);
      item.itemLimit = 0;
      const fixture = createFixture([item]);
      tapFirstButton(fixture);
      expect(blockedText(fixture)).toContain('not unlocked yet');
      expect(blockedText(fixture)).not.toContain('limit reached');
    });

    it('names the full factory queue, which disables an otherwise buildable type', () => {
      const fixture = createFixture([buildupItem(2, true)], true);
      tapFirstButton(fixture);
      expect(blockedText(fixture)).toContain('queue is full');
    });

    it('builds and shows nothing when the button is free', () => {
      const fixture = createFixture([buildupItem(4, true)]);
      spyOn(fixture.componentInstance.itemCockpitService, 'onBuild');
      tapFirstButton(fixture);
      expect(fixture.componentInstance.itemCockpitService.onBuild).toHaveBeenCalledWith(4);
      expect(blockedText(fixture)).toBe('');
    });
  });

  describe('selling', () => {
    function sellButton(fixture: ComponentFixture<ItemCockpitComponent>): HTMLElement {
      return fixture.nativeElement.querySelector('p-button button') as HTMLElement;
    }

    it('needs a second tap, because it cannot be undone', () => {
      const fixture = createFixture([buildupItem(4, true)], false, compactLayoutStub(), true);
      const onSell = spyOn(fixture.componentInstance.itemCockpitService, 'onSell');
      sellButton(fixture).click();
      fixture.detectChanges();
      expect(onSell).not.toHaveBeenCalled();
      expect(sellButton(fixture).textContent).toContain('Sell?');

      sellButton(fixture).click();
      fixture.detectChanges();
      expect(onSell).toHaveBeenCalled();
      expect(sellButton(fixture).textContent).not.toContain('Sell?');
    });
  });

  describe('with the item panel collapsed on a phone', () => {
    /** Stands in for the compact icon bar's wrench, which lives in the game component. */
    function addBuildIcon(): HTMLElement {
      const icon = document.createElement('button');
      icon.id = 'compact-build-icon';
      spyOn(icon, 'getBoundingClientRect').and.returnValue({left: 10, top: 800, width: 44, height: 44} as DOMRect);
      document.body.appendChild(icon);
      return icon;
    }

    afterEach(() => document.getElementById('compact-build-icon')?.remove());

    it('points the tip at the icon that opens the panel', () => {
      addBuildIcon();
      const fixture = createFixture([buildupItem(4, true)], false, compactLayoutStub(true, false));
      const component = fixture.componentInstance;
      // False, not true: the player is one tap short of the button, so the task keeps waiting -
      // but with a prompt on the icon instead of on a button nobody can see.
      expect(component.showBuildupTip(4)).toBe(false);
      expect(component.buildTip).toEqual({x: 32, y: 800, mode: 'open-menu'});
      expect(component.getBuildupTipBlockReason(4)).toBe(TipStallReason.ITEM_PANEL_CLOSED);
    });

    it('keeps the box on screen when the icon sits at the edge', () => {
      addBuildIcon();
      const fixture = createFixture([buildupItem(4, true)], false, compactLayoutStub(true, false));
      fixture.componentInstance.showBuildupTip(4);
      // Centred on the icon the box would start well left of the screen; the arrow stays put.
      expect(fixture.componentInstance.buildTipShift).toBeGreaterThan(0);
    });

    it('reports the real blocker rather than the closed panel', () => {
      addBuildIcon();
      const fixture = createFixture([buildupItem(4, false)], false, compactLayoutStub(true, false));
      expect(fixture.componentInstance.getBuildupTipBlockReason(4)).toBe(TipStallReason.BUTTON_DISABLED);
    });

    it('anchors to the button again once the panel is open', () => {
      const fixture = createFixture([buildupItem(4, true)], false, compactLayoutStub(true, true));
      const component = fixture.componentInstance;
      stubButtonRect(component, {left: 100, top: 200, width: 44});
      expect(component.showBuildupTip(4)).toBe(true);
      expect(component.buildTip!.mode).toBe('build');
    });
  });
});
