import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {MessageService} from 'primeng/api';

import {InventoryComponent} from './inventory.component';
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';

/**
 * The generated smoke test, made to run. It had never executed once: Karma could not load anything
 * reaching GameComponent, so nobody saw that it declared a standalone component and provided none
 * of its three dependencies.
 */
describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let fixture: ComponentFixture<InventoryComponent>;

  /** Only the corner of the WASM facade this component reads on the way up. */
  const gwtAngularServiceStub = {
    gwtAngularFacade: {
      inventoryTypeService: {
        getInventoryItems: () => [],
        getInventoryArtifacts: () => []
      }
    }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // Standalone components are imported, not declared.
      imports: [InventoryComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: MessageService, useValue: {add: () => {}}},
        {provide: GwtAngularService, useValue: gwtAngularServiceStub}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
