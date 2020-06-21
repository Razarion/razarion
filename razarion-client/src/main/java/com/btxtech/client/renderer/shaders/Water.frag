#extension GL_OES_standard_derivatives : enable
precision mediump float;

//-$$$-INCLUDE-DEFINES

varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec3 vNormal;
uniform highp mat4 normalMatrix;

uniform float uReflectionScale;
uniform sampler2D uReflection;
uniform float uTransparency;
uniform float uFresnelOffset;
uniform float uFresnelDelta;
uniform float uShininess;
uniform float uSpecularStrength;
uniform sampler2D uBumpMap;
uniform float uBumpMapDepth;
uniform sampler2D uDistortionMap;
uniform float uDistortionStrength;
uniform float uBumpDistortionScale;
uniform float uBumpDistortionAnimation;

// Light
uniform vec3 directLightDirection;
uniform vec3 directLightColor;
uniform vec3 ambientLightColor;

#ifdef  RENDER_SHALLOW_WATER
varying vec2 vUv;
uniform sampler2D uShallowWater;
uniform float uShallowWaterScale;
uniform sampler2D uShallowDistortionMap;
uniform float uShallowDistortionStrength;
uniform float uShallowAnimation;
uniform sampler2D uWaterStencil;
#endif

const vec3 SPECULAR_LIGHT_COLOR = vec3(1.0, 1.0, 1.0);

vec3 vec3ToRgb(vec3 normVec) {
    return normVec * 0.5 + 0.5;
}

vec2 dHdxy_fwd(vec2 vUv) {
    vec2 dSTdx = dFdx(vUv);
    vec2 dSTdy = dFdy(vUv);
    float Hll = uBumpMapDepth * texture2D(uBumpMap, vUv).x;
    float dBx = uBumpMapDepth * texture2D(uBumpMap, vUv + dSTdx).x - Hll;
    float dBy = uBumpMapDepth * texture2D(uBumpMap, vUv + dSTdy).x - Hll;
    return vec2(dBx, dBy);
}

vec2 dHdxy_fwd_animation() {
    vec2 dHdxy1 = dHdxy_fwd(vWorldVertexPosition.xy / uBumpDistortionScale + vec2(uBumpDistortionAnimation, 0.5));
    vec2 dHdxy2 = dHdxy_fwd(vWorldVertexPosition.xy / uBumpDistortionScale + vec2(-uBumpDistortionAnimation, uBumpDistortionAnimation));
    return (dHdxy1 + dHdxy2) / 2.0;
}

vec3 perturbNormalArb(vec3 surf_pos, vec3 surf_norm, vec2 dHdxy) {
    vec3 vSigmaX = vec3(dFdx(surf_pos.x), dFdx(surf_pos.y), dFdx(surf_pos.z));
    vec3 vSigmaY = vec3(dFdy(surf_pos.x), dFdy(surf_pos.y), dFdy(surf_pos.z));
    vec3 vN = surf_norm;
    vec3 R1 = cross(vSigmaY, vN);
    vec3 R2 = cross(vN, vSigmaX);
    float fDet = dot(vSigmaX, R1);
    fDet *= (float(gl_FrontFacing) * 2.0 - 1.0);
    vec3 vGrad = sign(fDet) * (dHdxy.x * R1 + dHdxy.y * R2);
    return normalize(abs(fDet) * surf_norm - vGrad);
}

void main(void) {
    // Reflection diffuse
    vec2 distortion1 = texture2D(uDistortionMap, vWorldVertexPosition.xy / uBumpDistortionScale + vec2(uBumpDistortionAnimation, 0.5)).rg * 2.0 - 1.0;
    vec2 distortion2 = texture2D(uDistortionMap, vWorldVertexPosition.xy / uBumpDistortionScale + vec2(-uBumpDistortionAnimation, uBumpDistortionAnimation)).rg * 2.0 - 1.0;
    vec2 totalDistortion = distortion1 + distortion2;
    vec2 reflectionCoord = (vWorldVertexPosition.xy) / uReflectionScale + totalDistortion * uDistortionStrength;
    vec3 reflection = texture2D(uReflection, reflectionCoord).rgb;

    // Specular
    vec3 directLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));
    vec3 normal = perturbNormalArb(-vViewPosition, vNormal, dHdxy_fwd_animation());
    vec3 viewDir = normalize(vViewPosition);
    vec3 halfwayDir = normalize(directLightDirection + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), uShininess);
    vec3 slopeSpecular = uSpecularStrength * spec * directLightColor;
    vec3 waterSurface = (ambientLightColor + vec3(0.5, 0.5, 0.5)) * reflection + slopeSpecular;

    // Fresnel
    float fresnel = dot(vNormal, viewDir);
    float fresnelTransparency = (uFresnelOffset - fresnel) / uFresnelDelta + 0.5;
    fresnelTransparency = clamp(fresnelTransparency, 0.0, 1.0);
    float waterSurfaceTransparebcy = max(uSpecularStrength * spec, fresnelTransparency)  * uTransparency;

    #ifdef  RENDER_SHALLOW_WATER
    vec2 totalShallowDistortion = uShallowDistortionStrength  * (texture2D(uShallowDistortionMap, vUv.xy / uShallowWaterScale + vec2(uShallowAnimation, 0)).rg * 2.0 - 1.0);
    vec4 shallowWater = texture2D(uShallowWater, (vUv.xy + totalShallowDistortion) / uShallowWaterScale);
    float waterStencil = texture2D(uWaterStencil, (vUv.xy + totalShallowDistortion) / uShallowWaterScale).b;
    // Porter-Duff Composition
    // https://de.wikipedia.org/wiki/Alpha_Blending
    float transparency = shallowWater.a + (1.0 - shallowWater.a) * waterStencil;
    vec3 color = 1.0 / transparency * (shallowWater.a * shallowWater.rgb + (1.0 - shallowWater.a) * waterStencil * waterSurface);
    gl_FragColor = vec4(color, max(shallowWater.a, min(transparency, waterSurfaceTransparebcy)));
    #else
    gl_FragColor = vec4(waterSurface, waterSurfaceTransparebcy);
    #endif
}
