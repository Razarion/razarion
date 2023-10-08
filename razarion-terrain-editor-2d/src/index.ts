const terrainSlopePositions = require('./assets/terrain-slope-positions.json');
import {TerrainEditor} from "./terrain-editor";

const terrainEditor = new TerrainEditor({x: 100, y: 100});
terrainEditor.setTerrainSlopePositions(terrainSlopePositions);