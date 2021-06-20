//-$$$-CHUNK uniforms-fragment BEGIN
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
#ifdef  RENDER_SHALLOW_WATER
uniform sampler2D uShallowWater;
uniform float uShallowWaterScale;
uniform sampler2D uShallowDistortionMap;
uniform float uShallowDistortionStrength;
uniform float uShallowAnimation;
uniform sampler2D uWaterStencil;
#endif
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK code-fragment BEGIN
vec2 dHdxy_fwd_animation() {
    vec2 dHdxy1 = dHdxy_fwd(uBumpMap, uBumpMapDepth, uBumpDistortionScale, vWorldVertexPosition.xy + vec2(uBumpDistortionAnimation, 0.5) * uBumpDistortionScale);
    vec2 dHdxy2 = dHdxy_fwd(uBumpMap, uBumpMapDepth, uBumpDistortionScale, vWorldVertexPosition.xy + vec2(-uBumpDistortionAnimation, uBumpDistortionAnimation) * uBumpDistortionScale);
    return (dHdxy1 + dHdxy2) / 2.0;
}
//-$$$-CHUNK code-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
// Reflection diffuse
vec2 distortion1 = texture2D(uDistortionMap, vWorldVertexPosition.xy / uBumpDistortionScale + vec2(uBumpDistortionAnimation, 0.5)).rg * 2.0 - 1.0;
vec2 distortion2 = texture2D(uDistortionMap, vWorldVertexPosition.xy / uBumpDistortionScale + vec2(-uBumpDistortionAnimation, uBumpDistortionAnimation)).rg * 2.0 - 1.0;
vec2 totalDistortion = distortion1 + distortion2;
vec2 reflectionCoord = (vWorldVertexPosition.xy) / uReflectionScale + totalDistortion * uDistortionStrength;
vec3 reflection = texture2D(uReflection, reflectionCoord).rgb;

// Specular
vec3 normal = normFromBumpMap(-vViewPosition, normalize(vNormal), dHdxy_fwd_animation());
vec3 viewDir = normalize(vViewPosition);
vec3 halfwayDir = normalize(correctedDirectLightDirection + viewDir);
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
//-$$$-CHUNK main-code-fragment END
