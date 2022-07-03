import {Injectable} from "@angular/core";
import {TerrainWaterTile, WaterConfig} from "../../gwtangular/GwtAngularFacade";
import {
  BufferAttribute,
  BufferGeometry,
  Color,
  CubeTextureLoader,
  Group,
  Mesh,
  MeshBasicMaterial,
  MultiplyOperation,
  RepeatWrapping,
  ShaderLib,
  ShaderMaterial,
  TangentSpaceNormalMap,
  TextureLoader,
  UniformsUtils,
  Vector2,
  Vector3
} from "three";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {getGwtMockImageUrl} from "./game-mock.service";
import {SignalGenerator} from "../signal-generator";

export const vertex = /* glsl */`
#define PHONG

varying vec3 vViewPosition;

#include <common>
#include <uv_pars_vertex>
#include <uv2_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>

void main() {

	#include <uv_vertex>
	#include <uv2_vertex>
	#include <color_vertex>

	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>

	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>

	vViewPosition = - mvPosition.xyz;

	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}
`;

export const fragment = /* glsl */`
#define PHONG

uniform vec3 diffuse;
uniform vec3 emissive;
uniform vec3 specular;
uniform float shininess;
uniform float opacity;

uniform float uDistortionScale;
uniform float uDistortionAnimation;

#include <common>
#include <packing>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <uv2_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_phong_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>

void main() {

	#include <clipping_planes_fragment>

	vec4 diffuseColor = vec4( diffuse, opacity );
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;

	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	vec3 mapN1 = texture2D( normalMap, vUv / uDistortionScale + vec2(uDistortionAnimation, 0.5)).xyz * 2.0 - 1.0;
	vec3 mapN2 = texture2D( normalMap, vUv / uDistortionScale + vec2(-uDistortionAnimation, uDistortionAnimation)).xyz * 2.0 - 1.0;
    vec3 mapN = (mapN1 + mapN2) / 2.0;
	mapN.xy *= normalScale;
	normal = perturbNormal2Arb( - vViewPosition, normal, mapN, faceDirection );
	#include <emissivemap_fragment>

	// accumulation
	#include <lights_phong_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>

	// modulation
	#include <aomap_fragment>

	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + reflectedLight.directSpecular + reflectedLight.indirectSpecular + totalEmissiveRadiance;

	#include <envmap_fragment>
	#include <output_fragment>
	#include <tonemapping_fragment>
	#include <encodings_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}
`;

@Injectable()
export class ThreeJsWaterRenderService {
  private materials: { material: ShaderMaterial, waterConfig: WaterConfig | null }[] = [];

  public setup(terrainWaterTiles: TerrainWaterTile[], group: Group): void {
    if (!terrainWaterTiles) {
      return;
    }
    terrainWaterTiles.forEach(terrainWaterTile => {
      terrainWaterTile.slopeConfigId

      if (terrainWaterTile.positions) {
        this.setupWater(terrainWaterTile.positions, group);
      }
      if (terrainWaterTile.shallowPositions) {
        ThreeJsWaterRenderService.setupShallowWater(terrainWaterTile.shallowPositions, terrainWaterTile.shallowUvs, group);
      }
    });
  }

  public update() {
    for (const material of this.materials) {
      material.material.uniforms.uDistortionAnimation.value = ThreeJsWaterRenderService.getWaterAnimation(Date.now(), 20); // TODO Take time from WaterConfig
    }
  }

  private setupWater(positions: Float32Array, group: Group) {
    let geometry = new BufferGeometry();
    geometry.setAttribute('position', new BufferAttribute(positions, 3));
    geometry.setAttribute('normal', ThreeJsTerrainTileImpl.fillVec3(new Vector3(0, 0, 1), positions.length));
    geometry.setAttribute('uv', ThreeJsTerrainTileImpl.uvFromPosition(positions));

    let cubeTexture = new CubeTextureLoader()
      .setPath('')
      .load([
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png')
      ]);

    let normalMap = new TextureLoader().load(getGwtMockImageUrl('WaterNorm.png'));
    normalMap.wrapS = RepeatWrapping;
    normalMap.wrapT = RepeatWrapping;

    let waterMaterial = new ShaderMaterial({
      lights: true,
      vertexShader: vertex,
      fragmentShader: fragment,
      transparent: true,
      uniforms: UniformsUtils.merge([
        ShaderLib.phong.uniforms,
        {
          normalScale: {value: new Vector2(0.2, 0.2)},
          uDistortionScale: {value: 7.0},
          uDistortionAnimation: {value: 1.0},
          opacity: {value: 0.8},
          shininess: {value: 50},
          specular: {value: new Color(1.0, 1.0, 1.0)},
        }
      ]),
    });

    waterMaterial.uniforms.envMap.value = cubeTexture;
    waterMaterial.uniforms.normalMap.value = normalMap;

    (<any>waterMaterial).envMap = waterMaterial.uniforms.envMap.value;
    (<any>waterMaterial).combine = MultiplyOperation;
    (<any>waterMaterial).normalMap = waterMaterial.uniforms.normalMap.value;
    (<any>waterMaterial).normalMapType = TangentSpaceNormalMap;

    const mesh = new Mesh(geometry, waterMaterial);

    mesh.addEventListener('added', () => {
      this.materials.push({material: waterMaterial, waterConfig: null});
    });
    mesh.addEventListener('removed', () => {
      this.materials.forEach((value, index) => {
        if (value.material == waterMaterial) {
          this.materials.splice(index, 1);
        }
      });
    });

    // const normalMaterial = new MeshNormalMaterial();
    // const mesh = new Mesh(geometry, normalMaterial);

    mesh.name = "Water";
    group.add(mesh);

    // const normHelper = new VertexNormalsHelper(mesh, 2, 0x111111);
    // group.add(normHelper);

  }

  private static setupShallowWater(shallowPositions: Float32Array, shallowUvs: Float32Array, group: Group) {
    let geometry = new BufferGeometry();
    geometry.setAttribute('position', new BufferAttribute(shallowPositions, 3));
    geometry.setAttribute('uvs', new BufferAttribute(shallowUvs, 3));
    const material = new MeshBasicMaterial({color: 0x5555ff});
    material.wireframe = true;
    const cube = new Mesh(geometry, material);
    cube.name = "Shallow Water";
    group.add(cube);
  }

  private static getWaterAnimation(millis: number, durationSeconds: number): number {
    return SignalGenerator.sawtooth(millis, durationSeconds * 1000.0, 0);
  }
}
