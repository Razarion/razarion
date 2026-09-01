import {PagePointerProbe} from './page-pointer-probe';

/**
 * The probe exists to tell two opposite failures apart in the Meta in-app browser: a touch that
 * reaches the page and is taken by something covering the canvas, versus a touch the webview never
 * delivers at all. Everything asserted here is one of those two answers being legible.
 */
describe('Page pointer probe', () => {
  let canvas: HTMLCanvasElement;
  let reported: { kind: string, detail?: string }[];
  let probe: PagePointerProbe;

  beforeEach(() => {
    canvas = document.createElement('canvas');
    document.body.appendChild(canvas);
    reported = [];
    const tracker: any = {report: (kind: string, detail?: string) => reported.push({kind, detail})};
    probe = new PagePointerProbe(canvas, tracker);
    probe.install();
  });

  afterEach(() => {
    probe.uninstall();
    canvas.remove();
  });

  function touchThePage(): void {
    window.dispatchEvent(new PointerEvent('pointerdown',
      {clientX: 10, clientY: 20, pointerType: 'touch', bubbles: true}));
  }

  function pairs(): Record<string, string> {
    const out: Record<string, string> = {};
    for (const pair of (reported[0]?.detail ?? '').split(',')) {
      const at = pair.indexOf('=');
      out[pair.slice(0, at)] = pair.slice(at + 1);
    }
    return out;
  }

  it('reports that a touch reached the page', () => {
    spyOn(document, 'elementFromPoint').and.returnValue(canvas);

    touchThePage();

    expect(reported.length).toBe(1);
    expect(reported[0].kind).toBe('POINTER_DOWN_PAGE');
    expect(pairs()['type']).toBe('touch');
  });

  it('says so when the finger landed on the canvas', () => {
    spyOn(document, 'elementFromPoint').and.returnValue(canvas);

    touchThePage();

    expect(pairs()['onCanvas']).toBe('1');
    expect(pairs()['hit']).toBe('CANVAS');
  });

  it('names whatever took the touch instead of the canvas', () => {
    const overlay = document.createElement('div');
    overlay.id = 'splash';
    overlay.classList.add('mask');
    spyOn(document, 'elementFromPoint').and.returnValue(overlay);

    touchThePage();

    // This is the finding the probe was built for: the touch arrived, something else has it.
    expect(pairs()['onCanvas']).toBe('0');
    expect(pairs()['hit']).toBe('DIV#splash.mask');
  });

  it('survives a browser that says nothing was hit', () => {
    spyOn(document, 'elementFromPoint').and.returnValue(null);

    touchThePage();

    expect(pairs()['hit']).toBe('none');
    expect(pairs()['onCanvas']).toBe('0');
  });

  it('carries the geometry the screenshot made suspicious', () => {
    spyOn(document, 'elementFromPoint').and.returnValue(canvas);

    touchThePage();

    expect(pairs()['canvas']).toMatch(/^\d+x\d+@-?\d+[/]-?\d+$/);
    expect(pairs()['win']).toMatch(/^\d+x\d+$/);
    expect(pairs()['dpr']).toBeDefined();
  });

  it('names the ancestry, because a bare tag name is not an answer', () => {
    // The first reading from the webview said hit=LI, and the game has several sources of one:
    // the tips in the loading cover, a carousel's indicators, a data view. They need entirely
    // different repairs, so the chain has to say which.
    const cover = document.createElement('div');
    cover.classList.add('cover-panel');
    const list = document.createElement('ul');
    list.classList.add('info-font');
    const tip = document.createElement('li');
    list.appendChild(tip);
    cover.appendChild(list);
    document.body.appendChild(cover);
    spyOn(document, 'elementFromPoint').and.returnValue(tip);

    touchThePage();

    expect(pairs()['hit']).toBe('LI<UL.info-font<DIV.cover-panel');
    cover.remove();
  });

  it('reports once and then stops listening', () => {
    spyOn(document, 'elementFromPoint').and.returnValue(canvas);

    touchThePage();
    touchThePage();
    touchThePage();

    expect(reported.length).toBe(1);
  });

  it('never lets a failing probe reach the gesture', () => {
    const throwing: any = {
      report: () => {
        throw new Error('tracker down');
      }
    };
    probe.uninstall();
    probe = new PagePointerProbe(canvas, throwing);
    probe.install();

    expect(() => touchThePage()).not.toThrow();
  });

  it('keeps the pairs parseable when an id contains a separator', () => {
    const odd = document.createElement('div');
    odd.id = 'a=b,c';
    spyOn(document, 'elementFromPoint').and.returnValue(odd);

    touchThePage();

    expect(pairs()['hit']).toBe('DIV#a_b_c');
  });
});
