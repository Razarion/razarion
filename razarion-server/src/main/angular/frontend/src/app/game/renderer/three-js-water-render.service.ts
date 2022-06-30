import {Injectable} from "@angular/core";
import {TerrainWaterTile, WaterConfig} from "../../gwtangular/GwtAngularFacade";
import {
  BufferAttribute,
  BufferGeometry,
  Group,
  Mesh,
  MeshBasicMaterial,
  RepeatWrapping,
  ShaderLib,
  ShaderMaterial,
  TextureLoader,
  UniformsUtils,
  Vector3
} from "three";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {getGwtMockImageUrl} from "./game-mock.service";
import {SignalGenerator} from "../signal-generator";

export const vertex = /* glsl */`
#define STANDARD

varying vec3 vViewPosition;

#ifdef USE_TRANSMISSION

varying vec3 vWorldPosition;

#endif

#include <common>
#include <uv_pars_vertex>
#include <uv2_pars_vertex>
#include <displacementmap_pars_vertex>
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
	#include <shadowmap_vertex>
	#include <fog_vertex>

#ifdef USE_TRANSMISSION

	vWorldPosition = worldPosition.xyz;

#endif
}
`;

export const fragment = /* glsl */`
#define STANDARD

#ifdef PHYSICAL
	#define IOR
	#define SPECULAR
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

#ifdef IOR
	uniform float ior;
#endif

#ifdef SPECULAR
	uniform float specularIntensity;
	uniform vec3 specularColor;

	#ifdef USE_SPECULARINTENSITYMAP
		uniform sampler2D specularIntensityMap;
	#endif

	#ifdef USE_SPECULARCOLORMAP
		uniform sampler2D specularColorMap;
	#endif
#endif

#ifdef USE_CLEARCOAT
	uniform float clearcoat;
	uniform float clearcoatRoughness;
#endif

#ifdef USE_SHEEN
	uniform vec3 sheenColor;
	uniform float sheenRoughness;

	#ifdef USE_SHEENCOLORMAP
		uniform sampler2D sheenColorMap;
	#endif

	#ifdef USE_SHEENROUGHNESSMAP
		uniform sampler2D sheenRoughnessMap;
	#endif
#endif

varying vec3 vViewPosition;

uniform sampler2D uDistortionMap;
uniform float uDistortionScale;
uniform float uDistortionAnimation;
uniform float uDistortionStrength;
uniform float uReflectionScale;

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
#include <bsdfs>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_physical_pars_fragment>
#include <transmission_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>

void main() {

	#include <clipping_planes_fragment>

	vec4 diffuseColor = vec4( diffuse, opacity );
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;

	#include <logdepthbuf_fragment>

  vec2 distortion1 = texture2D(uDistortionMap, vUv / uDistortionScale + vec2(uDistortionAnimation, 0.5)).rg;
  vec2 distortion2 = texture2D(uDistortionMap, vUv / uDistortionScale + vec2(-uDistortionAnimation, uDistortionAnimation)).rg;
  vec2 totalDistortion = (distortion1 + distortion2) / 2.0 - 1.0;
  vec2 reflectionCoord = vUv / uReflectionScale + totalDistortion * uDistortionStrength;
  vec4 sampledDiffuseColor = vec4(texture2D(map, reflectionCoord).rgb, 1.0);
	diffuseColor *= sampledDiffuseColor;

	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <roughnessmap_fragment>
	#include <metalnessmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <clearcoat_normal_fragment_begin>
	#include <clearcoat_normal_fragment_maps>
	#include <emissivemap_fragment>

	// accumulation
	#include <lights_physical_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>

	// modulation
	#include <aomap_fragment>

	vec3 totalDiffuse = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse;
	vec3 totalSpecular = reflectedLight.directSpecular + reflectedLight.indirectSpecular;

	#include <transmission_fragment>

	vec3 outgoingLight = totalDiffuse + totalSpecular + totalEmissiveRadiance;

	#ifdef USE_SHEEN

		// Sheen energy compensation approximation calculation can be found at the end of
		// https://drive.google.com/file/d/1T0D1VSyR4AllqIJTQAraEIzjlb5h4FKH/view?usp=sharing
		float sheenEnergyComp = 1.0 - 0.157 * max3( material.sheenColor );

		outgoingLight = outgoingLight * sheenEnergyComp + sheenSpecular;

	#endif

	#ifdef USE_CLEARCOAT

		float dotNVcc = saturate( dot( geometry.clearcoatNormal, geometry.viewDir ) );

		vec3 Fcc = F_Schlick( material.clearcoatF0, material.clearcoatF90, dotNVcc );

		outgoingLight = outgoingLight * ( 1.0 - material.clearcoat * Fcc ) + clearcoatSpecular * material.clearcoat;

	#endif

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
  private materials: { material: ShaderMaterial, waterConfig: WaterConfig | null }[] = []; // TODO is not cleanup after scene is removed

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
        this.setupShallowWater(terrainWaterTile.shallowPositions, terrainWaterTile.shallowUvs, group);
      }
    });
  }

  public update() {
    for (const material of this.materials) {
      material.material.uniforms.uDistortionAnimation.value = this.getWaterAnimation(Date.now(), 30);
    }
  }

  private setupWater(positions: Float32Array, group: Group) {
    let geometry = new BufferGeometry();
    geometry.setAttribute('position', new BufferAttribute(positions, 3));
    geometry.setAttribute('normal', ThreeJsTerrainTileImpl.fillVec3(new Vector3(0, 0, 1), positions.length));
    geometry.setAttribute('uv', ThreeJsTerrainTileImpl.uvFromPosition(positions));

    let waterMaterial = new ShaderMaterial({
      lights: true,
      defines: {
        'STANDARD': '',
        'USE_MAP': '',
        'USE_UV': ''
      },
      vertexShader: vertex,
      fragmentShader: fragment,
      uniforms: UniformsUtils.merge([
        ShaderLib.standard.uniforms,
        {
          uReflectionScale: {value: 50.0},
          uDistortionStrength: {value: 0.5},
          uDistortionScale: {value: 5.0},
          uDistortionAnimation: {value: 1.0},
          uDistortionMap: {value: null},
        }
      ])
    });

    waterMaterial.uniforms.map.value = new TextureLoader().load(getGwtMockImageUrl('WaterCloudReflection.png'));
    // waterMaterial.uniforms.map.value = new TextureLoader().load(getGwtMockImageUrl('chess32.jpg'));
    waterMaterial.uniforms.map.value.wrapS = RepeatWrapping;
    waterMaterial.uniforms.map.value.wrapT = RepeatWrapping;

    waterMaterial.uniforms.uDistortionMap.value = new TextureLoader().load(getGwtMockImageUrl('WaterDistortion.png'));
    waterMaterial.uniforms.uDistortionMap.value.wrapS = RepeatWrapping;
    waterMaterial.uniforms.uDistortionMap.value.wrapT = RepeatWrapping;

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

    // const meshBasicMaterial = new MeshBasicMaterial({ color: 0x1188ff });
    // const mesh = new Mesh(geometry, meshBasicMaterial);

    mesh.name = "Water";
    group.add(mesh);

    // const normHelper = new VertexNormalsHelper(mesh, 2, 0x111111);
    // group.add(normHelper);

  }

  private setupShallowWater(shallowPositions: Float32Array, shallowUvs: Float32Array, group: Group) {
    let geometry = new BufferGeometry();
    geometry.setAttribute('position', new BufferAttribute(shallowPositions, 3));
    geometry.setAttribute('uvs', new BufferAttribute(shallowUvs, 3));
    const material = new MeshBasicMaterial({color: 0x5555ff});
    material.wireframe = true;
    const cube = new Mesh(geometry, material);
    cube.name = "Shallow Water";
    group.add(cube);
  }

  private getWaterAnimation(millis: number, durationSeconds: number): number {
    return SignalGenerator.sawtooth(millis, durationSeconds * 1000.0, 0);
  }
}
