// import terrainSlopePositions from "./assets/terrain-slope-positions.txt";
const terrainSlopePositions = require('./terrain-slope-positions.json');
import {TerrainEditor} from "./terrain-editor";

const terrainEditor = new TerrainEditor();
terrainEditor.setTerrainSlopePositions(terrainSlopePositions);