import {isTouchDevice} from './prompt-geometry';

/**
 * "Tap to go there", at the tip of the out-of-view arrow.
 *
 * The arrow says where the target is but not how far, and on PROD 42 % of the players stuck with
 * the target out of view never found it again although nearly all of them had swiped before
 * (2026-09-23). The chip does what the minimap does: it takes the camera to the target in one tap.
 *
 * Plain DOM over the canvas, not Babylon GUI: a finger and a mouse both reach it the same way, and
 * a tap on it never falls through to the ground below as a move order.
 */
export class OutOfViewJump {
  /**
   * How far from the picture's centre the chip sits, as a share of its shorter side: just past the
   * tip of the arrow, which the material draws around the centre - not out at the edge, where the
   * minimap, the chat and the cockpit are.
   */
  private static readonly RADIUS_SHARE = 0.3;
  /** Never closer to an edge than this, so the chip stays whole on a narrow phone. */
  private static readonly EDGE_MARGIN_PX = 64;
  private element: HTMLButtonElement | null = null;
  private target: { x: number, y: number } | null = null;
  private onJump: ((x: number, y: number) => void) | null = null;

  constructor(private readonly canvas: () => HTMLCanvasElement | null) {
  }

  /**
   * @param angle the arrow's angle - 0 is east, counter-clockwise, north up the picture
   */
  show(target: { x: number, y: number }, angle: number, onJump: (x: number, y: number) => void): void {
    const canvas = this.canvas();
    if (!canvas) {
      return;
    }
    this.target = target;
    this.onJump = onJump;
    if (!this.element) {
      this.element = this.create();
      // Fixed to the window rather than inside the canvas' container, which is not positioned:
      // the place is computed from the canvas' own rectangle either way.
      document.body.appendChild(this.element);
    }
    this.place(canvas, angle);
  }

  hide(): void {
    this.element?.remove();
    this.element = null;
    this.target = null;
    this.onJump = null;
  }

  isShown(): boolean {
    return this.element !== null;
  }

  private create(): HTMLButtonElement {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = isTouchDevice() ? 'Tap to go there' : 'Click to go there';
    button.setAttribute('aria-label', 'Move the camera to the target');
    Object.assign(button.style, {
      position: 'fixed',
      zIndex: '5',
      transform: 'translate(-50%, -50%)',
      padding: '8px 14px',
      minHeight: '40px',
      border: '2px solid #e8b400',
      borderRadius: '10px',
      background: '#0b7a1a',
      color: '#ffffff',
      font: '600 14px sans-serif',
      whiteSpace: 'nowrap',
      cursor: 'pointer',
      touchAction: 'manipulation',
      boxShadow: '0 2px 6px rgba(0,0,0,0.45)',
    } as Partial<CSSStyleDeclaration>);
    // pointerdown, not click: stopping it here is what keeps the canvas from seeing a press and
    // starting a pan or a terrain tap under the chip.
    button.addEventListener('pointerdown', event => event.stopPropagation());
    button.addEventListener('click', event => {
      event.stopPropagation();
      const target = this.target;
      const onJump = this.onJump;
      if (target && onJump) {
        onJump(target.x, target.y);
      }
    });
    return button;
  }

  /** On the ray from the picture's centre along the arrow, past its tip, kept inside the picture. */
  private place(canvas: HTMLCanvasElement, angle: number): void {
    const canvasRect = canvas.getBoundingClientRect();
    const halfWidth = Math.max(0, canvasRect.width / 2 - OutOfViewJump.EDGE_MARGIN_PX * 1.6);
    const halfHeight = Math.max(0, canvasRect.height / 2 - OutOfViewJump.EDGE_MARGIN_PX);
    const dx = Math.cos(angle);
    const dy = -Math.sin(angle); // north is up the picture
    const scale = Math.min(
      Math.min(canvasRect.width, canvasRect.height) * OutOfViewJump.RADIUS_SHARE,
      Math.abs(dx) > 1e-6 ? halfWidth / Math.abs(dx) : Number.POSITIVE_INFINITY,
      Math.abs(dy) > 1e-6 ? halfHeight / Math.abs(dy) : Number.POSITIVE_INFINITY);
    const left = canvasRect.left + canvasRect.width / 2 + dx * scale;
    const top = canvasRect.top + canvasRect.height / 2 + dy * scale;
    this.element!.style.left = `${left}px`;
    this.element!.style.top = `${top}px`;
  }
}
