import {GameUiControl} from 'src/app/gwtangular/GwtAngularFacade';
import {BabylonRenderServiceAccessImpl} from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {AbstractGameCoordinates} from './abstract-game-coordinates';

/**
 * Dark clouds over the part of the map the player has not reached yet (2026-09-28).
 *
 * Before the radar only Noob Island is open - the quests up to then all stay on it - and from the
 * radar on the whole planet is. Either way everything beyond the planet's edge stays clouded. The
 * map stops at the edge since the dock is square, so little of that is ever on screen - a sliver
 * while the zoom animates, or a planet smaller than the map's window.
 *
 * Its own layer, over the ground and under the units, the view field and the quest marker: what
 * the player owns and where the quest points must never disappear into it.
 */
export class MiniFog extends AbstractGameCoordinates {
  /**
   * Phase 1, "Noob Island", on planet 117 - the terrain-derived boundary from
   * docs/game-design/progression.md ("Phase-1 boundary"), accurate to about 15 m. Only there in the
   * documentation so far; the next step moves it into the planet's configuration together with the
   * two map images.
   */
  static readonly NOOB_AREA: ReadonlyArray<{ x: number, y: number }> = [
    {x: 0, y: 0}, {x: 810, y: 0}, {x: 804, y: 162}, {x: 630, y: 350},
    {x: 402, y: 589}, {x: 117, y: 740}, {x: 0, y: 756}
  ];
  /**
   * World metres one cloud tile covers. Four times what the map shows at its default zoom (about
   * 400 m): at 420 m, as first built, a whole repeat was always on the map and read as tiles.
   */
  private static readonly TILE_METRES = 1600;
  private static readonly TILE_PX = 256;
  /** The soft edge, as a share of the map's width, so the small phone map gets a smaller one. */
  private static readonly EDGE_BLUR_SHARE = 0.08;
  private static cloudTile: HTMLCanvasElement | null = null;

  private revealed = false;

  constructor(gameUiControl: GameUiControl, renderService: BabylonRenderServiceAccessImpl) {
    super(gameUiControl, renderService);
  }

  setRevealed(revealed: boolean): void {
    this.revealed = revealed;
  }

  protected draw(ctx: CanvasRenderingContext2D): void {
    const width = this.getWidth();
    const height = this.getHeight();
    // In canvas pixels from here: the pattern and the blur are placed by hand, since neither the
    // pattern's tiling nor the shadow blur follow the world transform on their own.
    ctx.setTransform(1, 0, 0, 1, 0, 0);

    const scale = this.setupGameScale();
    const origin = this.realToCanvas(0, 0);
    const pattern = ctx.createPattern(MiniFog.tile(), 'repeat');
    if (pattern) {
      // Anchored to the world, so the clouds move with the ground when the camera pans.
      pattern.setTransform(new DOMMatrix()
        .translate(origin.x, origin.y)
        .scale(MiniFog.TILE_METRES * scale / MiniFog.TILE_PX));
      ctx.fillStyle = pattern;
    } else {
      ctx.fillStyle = '#0d1116';
    }
    ctx.fillRect(0, 0, width, height);

    // Cut the open area out with a soft edge: the shape is drawn far off the canvas and only its
    // blurred shadow lands here, and destination-out turns that shadow into a hole. Blur is the one
    // edge that looks like cloud rather than like a stencil.
    const area = this.revealed ? this.planetArea() : MiniFog.NOOB_AREA;
    const away = width + height + 1000;
    ctx.globalCompositeOperation = 'destination-out';
    ctx.shadowColor = '#000';
    ctx.shadowBlur = Math.max(4, width * MiniFog.EDGE_BLUR_SHARE);
    ctx.shadowOffsetX = -away;
    ctx.beginPath();
    area.forEach((corner, index) => {
      const point = this.realToCanvas(corner.x, corner.y);
      if (index === 0) {
        ctx.moveTo(point.x + away, point.y);
      } else {
        ctx.lineTo(point.x + away, point.y);
      }
    });
    ctx.closePath();
    ctx.fillStyle = '#000';
    ctx.fill();
  }

  private planetArea(): { x: number, y: number }[] {
    const size = this.gameUiControl.getPlanetConfig().getSize();
    return [{x: 0, y: 0}, {x: size.getX(), y: 0}, {x: size.getX(), y: size.getY()}, {x: 0, y: size.getY()}];
  }

  /**
   * One tile of dark cloud, made once per page and shared by every map. Soft blobs of near-black
   * and dark slate, each also drawn across the opposite edges so the tile repeats without a seam.
   * Seeded, so it is the same sky on every visit.
   */
  private static tile(): HTMLCanvasElement {
    if (MiniFog.cloudTile) {
      return MiniFog.cloudTile;
    }
    const size = MiniFog.TILE_PX;
    const tile = document.createElement('canvas');
    tile.width = size;
    tile.height = size;
    const ctx = tile.getContext('2d')!;
    ctx.fillStyle = '#10151b';
    ctx.fillRect(0, 0, size, size);
    let seed = 20260928;
    const random = () => {
      seed = (seed * 1664525 + 1013904223) >>> 0;
      return seed / 4294967296;
    };
    // Coarse banks first, then a few smaller puffs on them, so the clouds are big without being mush.
    for (let i = 0; i < 64; i++) {
      const coarse = i < 40;
      const x = random() * size;
      const y = random() * size;
      const radius = size * (coarse ? 0.12 + random() * 0.24 : 0.04 + random() * 0.07);
      const light = random() < 0.5;
      const color = light ? '78, 90, 106' : '0, 0, 0';
      const alpha = light ? 0.22 + random() * 0.3 : 0.3 + random() * 0.35;
      for (const dx of [-size, 0, size]) {
        for (const dy of [-size, 0, size]) {
          const gradient = ctx.createRadialGradient(x + dx, y + dy, 0, x + dx, y + dy, radius);
          gradient.addColorStop(0, `rgba(${color}, ${alpha})`);
          gradient.addColorStop(1, `rgba(${color}, 0)`);
          ctx.fillStyle = gradient;
          ctx.fillRect(x + dx - radius, y + dy - radius, radius * 2, radius * 2);
        }
      }
    }
    MiniFog.cloudTile = tile;
    return tile;
  }
}
