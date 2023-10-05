const terrainSlopePositions = require('./assets/terrain-slope-positions.json');
import {TerrainEditor} from "./terrain-editor";

const terrainEditor = new TerrainEditor();
terrainEditor.setTerrainSlopePositions(terrainSlopePositions);