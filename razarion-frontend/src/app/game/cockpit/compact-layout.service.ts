import {Injectable, signal} from '@angular/core';

/** The cockpit panels, as the compact layout addresses them. */
export type CompactPanel = 'main' | 'item' | 'quest' | 'chat';

/**
 * Decides whether the cockpit runs as fixed panels (desktop) or as collapsed overlays (phone), and
 * which single overlay is open.
 * <p>
 * On a phone the four panels together are wider and taller than the screen - measured at 412px
 * portrait: main 308x529, quest 342x173, chat 502x264 - so laid out as on the desktop they cover the
 * playfield completely. Collapsed, the field stays clear and each panel is one tap away.
 * <p>
 * Only ONE overlay is open at a time. Two of them stacked would cover the field again, which is the
 * problem this exists to solve.
 */
@Injectable({providedIn: 'root'})
export class CompactLayoutService {
  /**
   * Height is part of the test, not just width. A phone held sideways is about 915x412 - wide enough
   * to pass any width-only breakpoint, while the main cockpit alone (529px) is taller than the
   * screen. Portrait is caught by the width, landscape by the height.
   */
  private static readonly QUERY = '(max-width: 640px), (max-height: 540px)';

  readonly compact = signal(false);
  readonly openPanel = signal<CompactPanel | null>(null);

  constructor() {
    if (typeof window === 'undefined' || !window.matchMedia) {
      return;
    }
    const mediaQuery = window.matchMedia(CompactLayoutService.QUERY);
    this.apply(mediaQuery.matches);
    mediaQuery.addEventListener('change', event => this.apply(event.matches));
  }

  toggle(panel: CompactPanel): void {
    this.openPanel.update(current => (current === panel ? null : panel));
  }

  close(): void {
    this.openPanel.set(null);
  }

  isOpen(panel: CompactPanel): boolean {
    return this.openPanel() === panel;
  }

  private apply(compact: boolean): void {
    this.compact.set(compact);
    if (!compact) {
      // Leaving compact mode with an overlay open would leave that panel positioned as an overlay
      // on a desktop layout that has a fixed place for it.
      this.openPanel.set(null);
    }
  }
}
