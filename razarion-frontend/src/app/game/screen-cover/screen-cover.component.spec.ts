import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {ScreenCoverComponent} from './screen-cover.component';
import {BabylonModelService} from '../renderer/babylon-model.service';

/**
 * The loading cover on its way out.
 *
 * It fades with `opacity: 0` and is only taken out of the layout by a class that lands 300ms later,
 * so between those two moments it is invisible and still hit-testable. On PROD in the Meta in-app
 * browser on 2026-08-31 that stretch never ended: a touch reached the page, landed on an LI, and
 * never reached the canvas - the game was running underneath and could not be played at all.
 *
 * An overlay nobody can see must not take what nobody aimed at it, and that has to hold from the
 * first frame of the fade, not from the end of a timer that may never fire.
 */
describe('Screen cover on its way out', () => {
  let fixture: ComponentFixture<ScreenCoverComponent>;
  let component: ScreenCoverComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScreenCoverComponent],
      providers: [
        provideNoopAnimations(),
        // The real one pulls in the whole Babylon model pipeline; the template only asks it for a
        // progress figure.
        {provide: BabylonModelService, useValue: {glbContainerProgress: null}}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ScreenCoverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function cover(): HTMLElement {
    return fixture.nativeElement.querySelector('.cover-panel') as HTMLElement;
  }

  it('takes the touches while it is the thing on screen', () => {
    expect(getComputedStyle(cover()).pointerEvents).not.toBe('none');
  });

  it('stops taking them the moment it starts to fade', fakeAsync(() => {
    component.removeLoadingCover();
    fixture.detectChanges();

    // This is the assertion the Meta sessions were missing. Not after the timer - now.
    expect(getComputedStyle(cover()).pointerEvents).toBe('none');

    tick(300);
    fixture.detectChanges();
  }));

  it('leaves the layout once the timer lands', fakeAsync(() => {
    component.removeLoadingCover();
    tick(300);
    fixture.detectChanges();

    expect(cover().classList).toContain('cover-panel-hide');
    expect(getComputedStyle(cover()).display).toBe('none');
  }));

  it('is harmless if the timer never lands', fakeAsync(() => {
    component.removeLoadingCover();
    fixture.detectChanges();
    // Deliberately not advancing the clock: the state that cost every base in the webview was the
    // cover still being in the layout long after the fade. It must be inert there anyway.
    component.removeCover = false;
    fixture.detectChanges();

    expect(getComputedStyle(cover()).pointerEvents).toBe('none');
    tick(300);
  }));
});
