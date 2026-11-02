// Layout of the Noob Island rebuilt as a band along the south edge of planet 117 (2026-10-28, "variant B").
//
// Phase 1 is a strip of land x 0-1260 along the south planet edge, split into four cells of 315 m. Every cell has
// its own set of Phase-1 bots and its own spawn points, so the paths stay short however many cells are open. North
// of the strip a channel of ~130 m, then the south coast of Phase 2 region 1. The camera looks north, so every cell
// looks across the channel at the mainland it will cross to.
//
// Phase 2 keeps its content where it is, only its regions become rectangles:
//   region 1 (L9-13)  x 0-1260,    y 300-1150
//   region 2 (L14-16) x 0-1260,    y 1150-2100
//   region 3 (L17+)   x 1260-2200, y 0-2100
//
// Shared by noob-band-terrain.mjs (height map) and noob-band-config.mjs (bots, regions, quests). Game coordinates,
// origin bottom-left, y up, 1 node = 1 m.

export const BAND_X = 1260;          // east end of Phase 1 and of regions 1 and 2
export const CELL_W = 315;
export const CELLS = 4;
export const STRIP_END = 1235;       // where the land of the strip ends in the east
// The sea south of region 1 is filled with land up to this line, so every cell, the east one too, crosses straight
// north. East of it the lake of region 3 stays; the line wanders so that shore does not run dead straight.
export const r1FillX = (y) => BAND_X + 14 * Math.sin(y / 41) + 6 * Math.sin(y / 17);
export const P2_X = 2200;
export const P2_Y = 2100;
export const R1_Y = 300;             // region 1 starts here (its south coast is at r1CoastY)
export const R2_Y = 1150;

// North coast of the strip and south coast of region 1: gentle bays, so the shore does not read as a ruler line.
export const stripCoastY = (x) => 160 + 9 * Math.sin(x / 57) + 5 * Math.sin(x / 23 + 1);
export const r1CoastY = (x) => 300 + 11 * Math.sin(x / 71 + 2) + 5 * Math.sin(x / 29);

// The water bot of each cell stands in the open channel, in the middle of the cell: there it is the nearest water bot
// for every base of the cell (quest 388), and no base sits in front of it, because its units fire at whatever passes
// within their range, passive or not. Its ground is a U of bot-ground plates open to the north, the dockyard and the
// hydras in the water inside. The islet it stood on until 2026-11-02 is gone: the hydras' spread area lay on its land
// and they piled up in front of the dockyard, and the plates that stuck out over its square were walls.
export const waterBotCenter = (cell) => ({ x: cell * CELL_W + 163, y: 222 });   // on the U's axis
/** Middles of the water bot's plates (8 x 8 m, BotConfig.groundBoxPositions), relative x, absolute y. */
export const WATER_U_PLATES = [
  [-16, 210], [-8, 210], [0, 210], [8, 210], [16, 210],   // south row, y 206-214
  [-16, 218], [-16, 226], [-16, 234],                     // west leg, x 143-151 (cell 1)
  [16, 218], [16, 226], [16, 234],                        // east leg, x 175-183
];

export const cellX = (cell) => cell * CELL_W;

// A bot ground is a plate at an absolute height (BotConfig.groundBoxHeight), and the terrain under it takes the higher
// of the two. Above the land the plate's edge is a wall a unit cannot climb (a cell blocks from 0.5 m), and A* then
// searches the whole map for a way up: a viper sent at an extractor took 606 ms and jumped back (play test, 2026-10-28).
// So the land under every Phase-1 bot is levelled to just below this height and the plates sit on it.
export const BOT_PLATEAU = 1.2;
// The land under a plate, a little lower than its top: at the same height the two flickered (z-fighting, local play
// test, 2026-11-01). 0.2 m is no wall.
export const BOT_GROUND_LAND = BOT_PLATEAU - 0.2;
/** Where the land is levelled for a cell's bots: [x, y, half width] squares. */
export const botPlateaus = (cell) => {
  const l = cellLayout(cell);
  return [[l.garrison.x, l.garrison.y, 24], ...l.extractors.map((e) => [e.x, e.y, 7])];
};

/** Everything one cell holds, in game coordinates. */
export function cellLayout(cell) {
  const x0 = cellX(cell);
  const spawns = [];
  // Five west and five east of the water bot: the gap keeps every transporter lane clear of it.
  for (let k = 0; k < 5; k++) spawns.push({ x: x0 + 25 + 21 * k, y: 120 });
  for (let k = 0; k < 5; k++) spawns.push({ x: x0 + 206 + 21 * k, y: 120 });
  return {
    x0,
    // The strip rounds off at its east end: no spawn point where it is already narrow.
    spawns: spawns.filter((s) => s.x <= STRIP_END - 25),
    // (Bot1) Garrison: realm 40 x 40 in the middle of the cell, south of the base row - away from the way base ->
    // harbour (north), and no base of the cell further than ~160 m from it (quest 379). At the east end of the cell
    // it was 235 m from the west end (local play test, 2026-10-28).
    garrison: { x: x0 + 165, y: 38 },
    // (Bot1) Extractors, passive
    extractors: [{ x: x0 + 45, y: 40 }, { x: x0 + 100, y: 32 }, { x: x0 + 265, y: 40 }],
    // Razarion field for the first harvests, between the base row and the bots, out of the Teslas' reach (y <= 69)
    resource: { x0: x0 + 20, y0: 77, x1: x0 + 290, y1: 100 },
    water: waterBotCenter(cell),
  };
}

export const rect = (x0, y0, x1, y1) => [{ x: x0, y: y0 }, { x: x1, y: y0 }, { x: x1, y: y1 }, { x: x0, y: y1 }];
