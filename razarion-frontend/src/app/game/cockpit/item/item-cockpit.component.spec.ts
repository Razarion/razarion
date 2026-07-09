import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ItemCockpitComponent} from './item-cockpit.component';
import {ItemCockpitService, OtherItemCockpitModel} from './item-cockpit.service';
import {UserService} from '../../../auth/user.service';
import {TipService} from '../../tip/tip.service';

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
