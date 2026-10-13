/// <reference lib="webworker" />

import {computeTileGround, transferablesOf} from "./tile-ground-compute";
import {computeBotGroundMarkings} from "./bot-ground-markings-compute";
import type {GroundPath} from "./ground-paths";
import type {TerrainHelperRequest, TerrainHelperResponse} from "./terrain-helper";

// The terrain arithmetic off the main thread - see terrain-helper.ts.

let paths: GroundPath[] = [];

addEventListener('message', (event: MessageEvent<TerrainHelperRequest>) => {
  const request = event.data;
  if (request.paths) {
    paths = request.paths;
  }
  try {
    if (request.kind === 'ground') {
      const output = computeTileGround(request.input, paths);
      const response: TerrainHelperResponse = {id: request.id, output};
      postMessage(response, transferablesOf(output));
    } else {
      const output = computeBotGroundMarkings(request.boxes);
      const response: TerrainHelperResponse = {id: request.id, output};
      postMessage(response, [output.paint.buffer, output.gravel.buffer]);
    }
  } catch (error) {
    const response: TerrainHelperResponse = {id: request.id, error: String(error)};
    postMessage(response);
  }
});
