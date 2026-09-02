import {NgZone} from '@angular/core';
import {TestBed} from '@angular/core/testing';

import {GameComponent} from './game.component';
import {UiSettingsService} from './ui-settings.service';
import {CockpitDisplayService} from './cockpit/cockpit-display.service';
import {CompactLayoutService} from './cockpit/compact-layout.service';

/**
 * Built by hand instead of through TestBed.createComponent: the template pulls in the whole cockpit
 * and the Babylon canvas, none of which has anything to say about which panels are on offer. The
 * constructor calls effect(), so it still needs an injection context.
 */
describe('GameComponent chat visibility', () => {
  let uiSettings: UiSettingsService;
  let cockpitDisplay: CockpitDisplayService;
  let compactLayout: CompactLayoutService;
  let component: GameComponent;

  /** Whether the game offers a panel at all — protected, and the whole point of these tests. */
  function available(panel: string): boolean {
    return (component as any).isPanelAvailable(panel);
  }

  beforeEach(() => {
    uiSettings = new UiSettingsService();
    cockpitDisplay = new CockpitDisplayService();
    compactLayout = new CompactLayoutService();
    const unused: any = {};
    TestBed.configureTestingModule({});
    component = TestBed.runInInjectionContext(() => new GameComponent(
      unused, cockpitDisplay, compactLayout, unused, unused, unused, unused, unused, unused,
      unused, unused, uiSettings, unused, unused, unused, TestBed.inject(NgZone)));
    // The game has signed the player in and put its cockpit up.
    cockpitDisplay.showChatCockpit = true;
  });

  it('offers the chat while the player has not said otherwise', () => {
    expect(available('chat')).toBeTrue();
  });

  it('withholds the chat once it is switched off in the settings', () => {
    uiSettings.chatVisible = false;

    expect(available('chat')).toBeFalse();
  });

  it('still withholds a chat the game itself has not offered yet', () => {
    // Two independent reasons for the same answer: the setting must not resurrect a chat that the
    // cockpit has not put up (before login, or while an editor model is open).
    cockpitDisplay.showChatCockpit = false;

    expect(available('chat')).toBeFalse();
  });

  it('leaves the other panels alone', () => {
    cockpitDisplay.showQuestCockpit = true;
    uiSettings.chatVisible = false;

    expect(available('quest')).toBeTrue();
  });

  it('closes the open chat overlay when the chat is switched off', () => {
    // Otherwise the layout keeps believing a panel is up: the panel goes invisible, and the minimap
    // stays hidden behind nothing at all.
    compactLayout.openPanel.set('chat');

    uiSettings.chatVisible = false;

    expect(compactLayout.openPanel()).toBeNull();
  });

  it('does not disturb another open panel', () => {
    compactLayout.openPanel.set('quest');

    uiSettings.chatVisible = false;

    expect(compactLayout.openPanel()).toBe('quest');
  });

  it('brings the chat back when it is switched on again', () => {
    uiSettings.chatVisible = false;

    uiSettings.chatVisible = true;

    expect(available('chat')).toBeTrue();
  });
});
