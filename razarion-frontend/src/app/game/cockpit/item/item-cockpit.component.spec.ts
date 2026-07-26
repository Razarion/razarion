import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ItemCockpitComponent} from './item-cockpit.component';
import {BuildupItemModel, ItemCockpitService, OtherItemCockpitModel, OwnItemCockpitModel} from './item-cockpit.service';
import {UserService} from '../../../auth/user.service';
import {TipService} from '../../tip/tip.service';
import {TipStallReason} from '../../tip/tip-stall';

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
    // The title is the first div under the other-item panel (font-size 1.2rem).
    const title = fixture.nativeElement.querySelector('div[style*="font-size"]') as HTMLElement;
    expect(title).toBeTruthy();
    expect(title.textContent).toContain('Razarion: 5000');
  });

  it('falls back to baseName in the title for non-resource items', () => {
    const other = baseResource();
    other.resource = false;
    other.resourceAmount = null;
    other.baseName = 'Enemy Base';
    const fixture = createFixture(other);
    const title = fixture.nativeElement.querySelector('div[style*="font-size"]') as HTMLElement;
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

  function createFixture(buildupItems: BuildupItemModel[], factoryQueueFull = false): ComponentFixture<ItemCockpitComponent> {
    const ownItemCockpit: OwnItemCockpitModel = {
      imageUrl: '',
      itemTypeName: 'Builder',
      itemTypeDescr: '',
      buildupItems,
      containerCount: null,
      containerId: null,
      canSell: false,
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
      count: 1
    };
    TestBed.configureTestingModule({
      imports: [ItemCockpitComponent],
      providers: [
        {provide: ItemCockpitService, useValue: itemCockpitServiceStub},
        {provide: UserService, useValue: {isAdmin: () => false}},
        {provide: TipService, useValue: {setItemCockpit: () => {}}},
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
    expect(component.buildTip).toEqual({x: 130, y: 200});
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
    expect(component.buildTip).toEqual({x: 130, y: 200});
    expect(component.showBuildupTip(4)).toBe(true);
    expect(component.buildTip).toEqual({x: 330, y: 400});
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
});
