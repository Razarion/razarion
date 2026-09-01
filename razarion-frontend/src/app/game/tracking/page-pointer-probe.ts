import {FirstInteractionTrackerService} from './first-interaction-tracker.service';

/**
 * Whether a finger reaches the page at all, and what it lands on.
 *
 * POINTER_DOWN is reported from the canvas' own handler, so its absence says only that the canvas
 * was not touched - not why. In the Meta in-app browser that absence is total: 27 sessions with a
 * running game, no POINTER_DOWN, no base. Yet the same browser plainly receives touches, because it
 * pinch-zooms the page while `touch-action: none` on the canvas exists precisely to stop it from
 * doing that. One of two things is true, and they need opposite repairs:
 *
 *   - the touch reaches the page but never the canvas, because something sits on top of it, or
 *   - the touch does not reach the page either, because the webview consumes the gesture first.
 *
 * This listens on the window in the capture phase, which runs before any handler on the way down
 * can stop propagation, so it sees what the page sees. Together with what `elementFromPoint` says
 * was actually hit, that separates the two cases in a single session.
 *
 * Passive: a probe must never be the reason a gesture feels slow. It also unsubscribes after the
 * first report - the question is "did a touch ever arrive", and the answer does not improve with
 * repetition.
 */
export class PagePointerProbe {
  private listener: ((event: PointerEvent) => void) | null = null;

  constructor(private canvas: HTMLCanvasElement,
              private tracker: FirstInteractionTrackerService) {
  }

  install(): void {
    if (this.listener) {
      return;
    }
    this.listener = (event: PointerEvent) => {
      this.uninstall();
      try {
        this.tracker.report('POINTER_DOWN_PAGE', this.describeLanding(event));
      } catch (ignored) {
        // See the class comment: never at the expense of the gesture.
      }
    };
    window.addEventListener('pointerdown', this.listener, {capture: true, passive: true});
  }

  uninstall(): void {
    if (this.listener) {
      window.removeEventListener('pointerdown', this.listener, {capture: true});
      this.listener = null;
    }
  }

  /**
   * Where the finger landed and how the page was laid out underneath it, as `name=value` pairs.
   * <p>
   * `onCanvas` is the answer the whole probe exists for. The geometry is here because the one
   * screenshot from the webview shows the canvas taking less than half the height it should, and a
   * canvas that is laid out wrongly is a candidate for being covered wrongly too.
   */
  private describeLanding(event: PointerEvent): string {
    const parts: string[] = [];
    const add = (name: string, value: string | number | boolean) =>
      parts.push(name + '=' + (value === true ? 1 : value === false ? 0 : value));

    add('type', this.clean(event.pointerType || 'unknown'));
    const hit = document.elementFromPoint(event.clientX, event.clientY);
    add('hit', this.describe(hit));
    add('onCanvas', hit === this.canvas);

    const rect = this.canvas.getBoundingClientRect();
    // Slash, not comma: the pairs are split on commas, so no value may contain one - the same
    // rule clean() enforces for element names.
    add('canvas', `${Math.round(rect.width)}x${Math.round(rect.height)}@${Math.round(rect.left)}/${Math.round(rect.top)}`);
    add('win', `${window.innerWidth}x${window.innerHeight}`);
    const visual = window.visualViewport;
    if (visual) {
      // A webview whose visual viewport differs from the layout viewport is one that is scrolled or
      // pinch-zoomed, which is exactly what a stray browser gesture leaves behind.
      add('vv', `${Math.round(visual.width)}x${Math.round(visual.height)}`);
      add('vvScale', visual.scale.toFixed(2));
    }
    add('dpr', window.devicePixelRatio || 0);
    return parts.join(',');
  }

  /**
   * The element and its first few ancestors, innermost first, joined with '<'.
   * <p>
   * A bare tag name is not an answer. The first reading from the webview said hit=LI, and the game
   * has several sources of one - the tips in the loading cover, a carousel's indicators, a data
   * view - which need entirely different repairs. The chain says which.
   */
  private describe(element: Element | null): string {
    if (!element) {
      return 'none';
    }
    const chain: string[] = [];
    let current: Element | null = element;
    for (let depth = 0; current && depth < 4 && current !== document.body; depth++) {
      chain.push(this.name(current));
      current = current.parentElement;
    }
    return chain.join('<');
  }

  private name(element: Element): string {
    const id = element.id ? '#' + element.id : '';
    const className = element.classList.length ? '.' + element.classList[0] : '';
    return this.clean(element.tagName + id + className);
  }

  /** The pairs are read back split on commas and equals signs, so neither may appear in a value. */
  private clean(value: string): string {
    return value.replace(/[,=]/g, '_');
  }
}
