import { Injectable } from "@angular/core";
import {
    MathUtils,
    PerspectiveCamera,
    BufferGeometry,
    BufferAttribute,
    Clock,
    Camera
} from "three";
import { WebGLRenderer } from "three/src/renderers/WebGLRenderer";
import { MeshBasicMaterial } from "three/src/materials/MeshBasicMaterial";
import { Mesh } from "three/src/objects/Mesh";
import { Scene } from "three/src/scenes/Scene";
import terrainTileJsonArray from "./razarion_generated/terrain-tiles.json";
import { TerrainTile } from "src/app/gwtangular/GwtAngularFacade";


@Injectable()
export class RenderService {
    private keyPressed: Map<string, number> = new Map();

    init(htmlCanvasElement: HTMLCanvasElement) {
        try {
            this.internalInit(htmlCanvasElement);
        } catch (err) {
            console.error(err);
        }
    }

    internalInit(htmlCanvasElement: HTMLCanvasElement) {
        let scene = new Scene();
        let clock = new Clock();

        const camera = new PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);

        let renderer = new WebGLRenderer({
            antialias: true,
            canvas: htmlCanvasElement
        });
        renderer.setSize(window.innerWidth, window.innerHeight);
        // terrain-tiles
        for (let i in terrainTileJsonArray) {
            let terrainTile: TerrainTile = terrainTileJsonArray[i];
            for (const [key, value] of Object.entries(terrainTile.groundPositions)) {
                let geometry = new BufferGeometry();
                geometry.setAttribute('position', new BufferAttribute(new Float32Array((<any>value).doubles), 3));
                const material = new MeshBasicMaterial({ color: 0x00ff00 });
                material.wireframe = true;
                const cube = new Mesh(geometry, material);
                scene.add(cube);

            }
        }

        const self = this;
        // Scroll
        window.addEventListener("keydown", e => {
            if (!self.keyPressed.has(e.key)) {
                self.keyPressed.set(e.key, Date.now());
            }
        }, true);
        window.addEventListener("keyup", e => {
            self.keyPressed.delete(e.key);
        }, true);
        window.addEventListener('wheel', e => {
            let delta = e.deltaY;
            delta = delta / 240;
            delta = -delta;
            if (delta <= 0) {
                delta -= camera.position.z * 0.1;
            } else {
                delta += camera.position.z * 0.1;
            }
            if (camera.position.z + delta > 1 && camera.position.z + delta < 200) {
                camera.translateZ(delta);
            }
            }, true);
        // Camera
        camera.position.x = 0;
        camera.position.y = 0;
        camera.position.z = 40;
        camera.rotation.x = MathUtils.degToRad(30);

        function animate() {
            const delta = clock.getDelta();

            requestAnimationFrame(animate);
            self.scrollCamera(delta, camera);
            renderer.render(scene, camera);
        }
        animate();
    }

    scrollCamera(delta: number, camera: Camera) {
        for (let [key, start] of this.keyPressed) {
            const duration = new Date().getTime() - start;

            let distance = Math.sqrt(duration + 200) * 0.01 + 0.05;

            distance = distance * delta / 0.016;

            distance = distance + camera.position.z * 0.02;

            switch (key) {
                case 'ArrowUp': camera.position.y += distance; break;
                case 'ArrowDown': camera.position.y -= distance; break;
                case 'ArrowRight': camera.position.x += distance; break;
                case 'ArrowLeft': camera.position.x -= distance; break;
                default:
            }
        };
    }

}

