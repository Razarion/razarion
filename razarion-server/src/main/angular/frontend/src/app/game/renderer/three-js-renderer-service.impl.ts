import { Injectable } from "@angular/core";
import {
    MathUtils,
    PerspectiveCamera,
    Clock,
    Camera,
    Raycaster,
    Vector2,
    AmbientLight,
    DirectionalLight,
    ObjectLoader
} from "three";
import { WebGLRenderer } from "three/src/renderers/WebGLRenderer";
import { Scene } from "three/src/scenes/Scene";
import { TerrainTile, ThreeJsRendererServiceAccess, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
import { ThreeJsTerrainTileImpl } from "./three-js-terrain-tile.impl";
import { GwtAngularService } from "src/app/gwtangular/GwtAngularService";


@Injectable()
export class ThreeJsRendererServiceImpl implements ThreeJsRendererServiceAccess {
    scene = new Scene();
    private keyPressed: Map<string, number> = new Map();
    camera: PerspectiveCamera = new PerspectiveCamera;
    private canvasDiv!: HTMLDivElement;
    private renderer!: WebGLRenderer

    constructor(private gwtAngularService: GwtAngularService) {
        this.scene.name = "Main Scene"
    }

    createTerrainTile(terrainTile: TerrainTile): ThreeJsTerrainTile {
        try {
            return new ThreeJsTerrainTileImpl(terrainTile, this.scene);
        } catch (e) {
            console.error(e);
            throw e;
        }
    }

    setViewFieldCenter(x: number, y: number): void {
        let currentViewFieldCenter = this.setupGroundPosition(0, 0);
        let newFiledCenter = new Vector2(x, y);
        let delta = newFiledCenter.sub(currentViewFieldCenter);
        this.camera.position.x += delta.x;
        this.camera.position.y += delta.y;
        this.onViewFieldChanged();
    }

    onResize() {
        this.renderer.setSize(this.canvasDiv.offsetWidth - 100, this.canvasDiv.offsetHeight - 100); // TODO -> -100 prevent starnge loop
        this.camera.aspect = this.canvasDiv.offsetWidth / this.canvasDiv.offsetHeight;
        this.camera.updateProjectionMatrix();
        this.onViewFieldChanged();
    }

    setup(canvasHolder: HTMLDivElement) {
        this.canvasDiv = canvasHolder;
        try {
            this.internalSetup(canvasHolder);
        } catch (err) {
            console.error(err);
        }
    }

    internalSetup(canvasHolder: HTMLDivElement) {
        let clock = new Clock();

        this.camera = new PerspectiveCamera(75, canvasHolder.offsetWidth / canvasHolder.offsetHeight, 0.1, 1000);
        this.camera.name = "Camera";

        this.renderer = new WebGLRenderer({ antialias: true });
        this.renderer.setSize(canvasHolder.offsetWidth, canvasHolder.offsetHeight);
        canvasHolder.appendChild(this.renderer.domElement);

        // ----- Scroll -----
        const self = this;
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
                delta += self.camera.position.z * 0.1;
            } else {
                delta -= self.camera.position.z * 0.1;
            }
            if (self.camera.position.z + delta > 1 && self.camera.position.z + delta < 200) {
                self.camera.translateZ(delta);
                this.onViewFieldChanged();
            }
        }, true);

        // -----  Camera -----
        this.camera.position.x = 0;
        this.camera.position.y = 0;
        this.camera.position.z = 40;
        this.camera.rotation.x = MathUtils.degToRad(30);

        // ----- Light -----
        let ambientLight = new AmbientLight(0x808080, 0.05);
        ambientLight.name = "Ambient Light"
        this.scene.add(ambientLight);

        let directionalLight = new DirectionalLight(0xffffff, 0.25);
        directionalLight.name = "Directional Light"
        directionalLight.position.set(70, 30, 50);
        directionalLight.target.position.set(50, 50, 0);
        directionalLight.castShadow = true;
        directionalLight.shadow.mapSize.width = 1024;
        directionalLight.shadow.mapSize.height = 1024;
        directionalLight.shadow.camera.left = -50;
        directionalLight.shadow.camera.bottom = -50;
        directionalLight.shadow.camera.top = 50;
        directionalLight.shadow.camera.right = 50;
        directionalLight.shadow.camera.near = 0.5;
        directionalLight.shadow.camera.far = 100;
        this.scene.add(directionalLight);

        var loader = new ObjectLoader();

        loader.load("/rest/model",
            function (scene) {
                self.scene.add(scene);
            });


        // ----- Render loop -----
        function animate() {
            const delta = clock.getDelta();

            requestAnimationFrame(animate);
            self.scrollCamera(delta, self.camera);
            self.renderer.render(self.scene, self.camera);
        }
        animate();
    }

    scrollCamera(delta: number, camera: Camera) {
        let hasChanged = false;
        for (let [key, start] of this.keyPressed) {
            const duration = new Date().getTime() - start;

            let distance = Math.sqrt(duration + 200) * 0.01 + 0.05;

            distance = distance * delta / 0.016;

            distance = distance + camera.position.z * 0.02;

            switch (key) {
                case 'ArrowUp': {
                    hasChanged = true;
                    camera.position.y += distance;
                    break;
                }
                case 'ArrowDown': {
                    hasChanged = true;
                    camera.position.y -= distance;
                    break;
                }
                case 'ArrowRight': {
                    hasChanged = true;
                    camera.position.x += distance;
                    break;
                }
                case 'ArrowLeft': {
                    hasChanged = true;
                    camera.position.x -= distance;
                    break;
                }
                default:
            }
        };
        if (hasChanged) {
            this.onViewFieldChanged();
        }
    }

    public addToSceneEditor(scene: Scene) {
        this.scene.add(scene);
    }

    private onViewFieldChanged() {
        let bottomLeft = this.setupGroundPosition(-1, -1);
        let bottomRight = this.setupGroundPosition(1, -1);
        let topRight = this.setupGroundPosition(1, 1);
        let topLeft = this.setupGroundPosition(-1, 1);

        this.gwtAngularService.gwtAngularFacade.inputService.onViewFieldChanged(
            bottomLeft.x, bottomLeft.y,
            bottomRight.x, bottomRight.y,
            topRight.x, topRight.y,
            topLeft.x, topLeft.y
        );
    }

    private setupGroundPosition(ndcX: number, ndcY: number): Vector2 {
        let raycaster = new Raycaster();
        raycaster.setFromCamera({ x: ndcX, y: ndcY }, this.camera);
        let factor = this.camera.position.z / -raycaster.ray.direction.z;
        let pointOnGround = raycaster.ray.direction.clone().setLength(factor);
        pointOnGround.add(this.camera.position);
        return new Vector2(pointOnGround.x, pointOnGround.y);
    }

}

