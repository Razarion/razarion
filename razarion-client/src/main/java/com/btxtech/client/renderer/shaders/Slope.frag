precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vSlopeFactor;
varying float vGroundSplatting;

uniform highp mat4 uNVMatrix;

// Light
uniform float uLightSpecularIntensity;
uniform float uLightSpecularHardness;
uniform vec3 uLightDirection;
uniform vec3 uLightDiffuse;
uniform vec3 uLightAmbient;
// Shadow
varying vec4 vShadowCoord;
uniform float uShadowAlpha;
uniform sampler2D uShadowTexture;
//Slope
uniform float uLightSpecularIntensitySlope;
uniform float uLightSpecularHardnessSlope;
uniform sampler2D uSlopeTexture;
uniform float uSlopeTextureScale;
uniform sampler2D uSlopeBm;
uniform float uSlopeBmScale;
uniform float uSlopeBmOnePixel;
uniform float uSlopeBmDepth;
uniform sampler2D uSlopeWaterSplatting;
uniform float uSlopeWaterSplattingScale;
uniform float uSlopeWaterSplattingFactor;
uniform float uSlopeWaterSplattingFadeThreshold;
uniform float uSlopeWaterSplattingHeight;

// Ground
uniform float uLightSpecularIntensityGround;
uniform float uLightSpecularHardnessGround;
uniform sampler2D uGroundTopTexture;
uniform float uGroundTopTextureScale;
uniform sampler2D uGroundBottomTexture;
uniform float uGroundBottomTextureScale;
uniform sampler2D uGroundBottomBm;
uniform float uGroundBottomBmScale;
uniform float uGroundBottomBmOnePixel;
uniform float uGroundBottomBmDepth;
uniform sampler2D uGroundSplatting;
uniform float uGroundSplattingScale;
uniform float uGroundSplattingFadeThreshold;
uniform float uGroundSplattingOffset;
uniform float uGroundSplattingGroundBmMultiplicator;
// Water
uniform bool uHasWater;
uniform float uWaterLevel;
uniform float uWaterGround;
uniform float uWaterTransparency;
uniform float uWaterLightSpecularIntensity;
uniform float uWaterLightSpecularHardness;
uniform sampler2D uWaterNormMap;
uniform sampler2D uWaterDistortionMap;
uniform float uWaterReflectionScale;
uniform sampler2D uWaterReflection;
uniform float uWaterDistortionScale;
uniform float uWaterNormMapDepth;
uniform float uWaterDistortionStrength;
uniform float uWaterAnimation;
// Terrain marker
uniform sampler2D uTerrainMarkerTexture;
uniform vec4 uTerrainMarker2DPoints;
uniform float uTerrainMarkerAnimation;

const vec3 SPECULAR_LIGHT_COLOR = vec3(1.0, 1.0, 1.0);
const float UNDER_WATER_BIAS = 0.05;
const float SLOPE_WATER_STRIPE_FADEOUT = 2.0;
const vec3 UNDER_WATER_COLOR = vec3(1.0, 1.0, 1.0);

const vec3 underWaterTopColor = vec3(0.0, 0.7, 0.8);
const vec3 underWaterBottomColor = vec3(0.0, 0.1, 0.2);


// Vector to RGB -> normVector * 0.5 + 0.5
// Interpolate x (MIN, MAX) to 0..1: 1.0/(MAX-MIN) * x + MIN/(MAX-MIN)

// http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
vec4 triPlanarTextureMapping(sampler2D sampler, float scale, vec2 addCoord) {
    vec3 blending = abs(vVertexNormCoord);

    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(sampler, vVertexPositionCoord.yz / scale + addCoord); // TODO scale calculation correct -> copy from here
    vec4 yAxisTop = texture2D(sampler, vVertexPositionCoord.xz / scale + addCoord);
    vec4 zAxisTop = texture2D(sampler, vVertexPositionCoord.xy / scale + addCoord);
    return xAxisTop * blending.x + yAxisTop * blending.y + zAxisTop * blending.z;
}

vec3 bumpMapNorm(sampler2D sampler, float bumpMapDepth, float scale, float onePixel) {
    vec3 normal = normalize(vVertexNormal);
    vec3 tangent = normalize(vVertexTangent);
    vec3 binormal = cross(normal, tangent);

    float bm0 = triPlanarTextureMapping(sampler, scale, vec2(0, 0)).r;
    float bmUp = triPlanarTextureMapping(sampler, scale, vec2(0.0, onePixel)).r;
    float bmRight = triPlanarTextureMapping(sampler, scale, vec2(onePixel, 0.0)).r;

    vec3 bumpVector = (bm0 - bmRight) * tangent + (bm0 - bmUp) * binormal;
    return normalize(normal + bumpMapDepth * bumpVector);
}

vec3 setupSpecularLight(vec3 correctedLightDirection, vec3 correctedNorm, float intensity, float hardness) {
     vec3 reflectionDirection = normalize(reflect(correctedLightDirection, normalize(correctedNorm)));
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     float factor = pow(max(dot(reflectionDirection, eyeDirection), 0.0), hardness) * intensity;
     return SPECULAR_LIGHT_COLOR * factor;
}

float calculateShadowFactor() {
    float zMap = texture2D(uShadowTexture, vShadowCoord.st).r;

    if(zMap > vShadowCoord.z - 0.01) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}

vec4 setupTerrainMarker() {
    vec4 terrainMarkerColor = vec4(0.0, 0.0, 0.0, 0.0);
    if(uTerrainMarker2DPoints != vec4(0.0, 0.0, 0.0, 0.0)) {
        if(vVertexPositionCoord.x > uTerrainMarker2DPoints.x && vVertexPositionCoord.y > uTerrainMarker2DPoints.y && vVertexPositionCoord.x < uTerrainMarker2DPoints.z && vVertexPositionCoord.y < uTerrainMarker2DPoints.w) {
            float xLookup = (vVertexPositionCoord.x - uTerrainMarker2DPoints.x) / (uTerrainMarker2DPoints.z - uTerrainMarker2DPoints.x);
            float yLookup = (vVertexPositionCoord.y - uTerrainMarker2DPoints.y) / (uTerrainMarker2DPoints.w - uTerrainMarker2DPoints.y);
            vec4 lookupMarker = texture2D(uTerrainMarkerTexture, vec2(xLookup, yLookup));
            if(lookupMarker.r > 0.5) {
                terrainMarkerColor = vec4(0.0, uTerrainMarkerAnimation * 0.3, 0.0, 0.0);
            }
        }
    }
    return terrainMarkerColor;
}

vec4 setupColor(vec3 ambient, vec3 diffuse, vec3 specular, bool shadow, bool terrainMarker) {
    float shadowFactor = 1.0;
    if(shadow) {
        shadowFactor = calculateShadowFactor();
    }
    vec4 terrainMarkerColor = vec4(0.0, 0.0, 0.0, 0.0);
    if(terrainMarker) {
        terrainMarkerColor = setupTerrainMarker();
    }
    return vec4(ambient + diffuse * shadowFactor + specular * shadowFactor, 1.0) + terrainMarkerColor;
}

void setupSlope(inout vec3 ambient, inout vec3 diffuse, inout vec3 specular) {
    vec3 correctedLight = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);
    vec3 textureColor = triPlanarTextureMapping(uSlopeTexture, uSlopeTextureScale, vec2(0,0)).rgb;

    vec3 norm = texture2D(uSlopeBm, vVertexPositionCoord.xy * uSlopeBmScale).xyz;
    norm = normalize(vec3(norm.x - 1.0, norm.y - 1.0, norm.z / 2.0));
    norm = mix(normalize((uNVMatrix * vec4(norm, 1.0)).xyz), vVertexNormal, uSlopeBmDepth);

    ambient = uLightAmbient * textureColor;
    diffuse = max(dot(normalize(norm), -correctedLight), 0.0) * uLightDiffuse * textureColor;
    specular = setupSpecularLight(correctedLight, norm, uLightSpecularIntensitySlope, uLightSpecularHardnessSlope);
}

void setupUnderWater(inout vec3 ambient, inout vec3 diffuse, float underWaterFactor) {
    vec3 correctedLight = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);
    vec3 color = mix(underWaterBottomColor, underWaterTopColor, underWaterFactor);
    vec3 slopeNorm = bumpMapNorm(uSlopeBm, uSlopeBmDepth, uSlopeBmScale, uSlopeBmOnePixel);
    ambient = uLightAmbient * color;
    diffuse = max(dot(normalize(slopeNorm), normalize(-correctedLight)), 0.0) * color * uLightDiffuse;
}

void setupGround(inout vec3 ambient, inout vec3 diffuse, inout vec3 specular) {
    // Copied from Ground Shader and variable renamed (Ground added) ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    float shadowFactor = calculateShadowFactor();
    vec3 correctedLight = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);

    vec3 colorTop = triPlanarTextureMapping(uGroundTopTexture, uGroundTopTextureScale, vec2(0,0)).rgb;
    vec3 colorBottom = triPlanarTextureMapping(uGroundBottomTexture, uGroundBottomTextureScale, vec2(0,0)).rgb;
    vec3 normBottom = bumpMapNorm(uGroundBottomBm, uGroundBottomBmDepth, uGroundBottomBmScale, uGroundBottomBmOnePixel);
    float splatting = triPlanarTextureMapping(uGroundSplatting, uGroundSplattingScale, vec2(0,0)).r;

    float bottomBmValue = triPlanarTextureMapping(uGroundBottomBm, uGroundBottomBmScale, vec2(0,0)).r;

    float splattingValue = vGroundSplatting - (splatting + bottomBmValue * uGroundSplattingGroundBmMultiplicator) / 2.0 + uGroundSplattingOffset;
    vec3 textureColor;
    vec3 norm;
    if(splattingValue > uGroundSplattingFadeThreshold) {
        norm = vVertexNormal;
        textureColor = colorTop;
    } else if(splattingValue < -uGroundSplattingFadeThreshold) {
        norm = normBottom;
        textureColor = colorBottom;
    } else {
        float groundTopFactor = splattingValue / (2.0 * uGroundSplattingFadeThreshold) + 0.5;
        textureColor = mix(colorBottom, colorTop, groundTopFactor);
        norm = mix(normBottom, vVertexNormal, groundTopFactor);
    }
    ambient = uLightAmbient * textureColor;
    diffuse = max(dot(normalize(norm), -correctedLight), 0.0) * uLightDiffuse * textureColor;
    specular = setupSpecularLight(correctedLight, norm, uLightSpecularIntensityGround, uLightSpecularHardnessGround);
    // Copied from Ground Shader ends +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}

// Copied from Water Shader ends +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
void setupWater(inout vec3 ambient, inout vec3 specular) {
    // Setup ambient
    vec2 distortion1 = texture2D(uWaterDistortionMap, vVertexPositionCoord.xy / uWaterDistortionScale + vec2(uWaterAnimation, 0.5)).rg * 2.0 - 1.0;
    vec2 distortion2 = texture2D(uWaterDistortionMap, vVertexPositionCoord.xy / uWaterDistortionScale + vec2(-uWaterAnimation, uWaterAnimation)).rg * 2.0 - 1.0;
    vec2 totalDistortion = distortion1 + distortion2;
    vec2 reflectionCoord = (vVertexPositionCoord.xy) / uWaterReflectionScale + totalDistortion * uWaterDistortionStrength;
    ambient =  texture2D(uWaterReflection, reflectionCoord).rgb;
    // Setup specular
    vec3 normMap1 = texture2D(uWaterNormMap, vVertexPositionCoord.xy / uWaterDistortionScale + vec2(uWaterAnimation, 0.5)).xyz;
    vec3 normMap2 = texture2D(uWaterNormMap, vVertexPositionCoord.xy / uWaterDistortionScale + vec2(-uWaterAnimation, uWaterAnimation)).xyz;
    vec3 normMap = normMap1 + normMap2;
    normMap = normalize(vec3(normMap.x - 1.0, normMap.y - 1.0, normMap.z / 2.0));
    normMap = mix(vec3(0.0, 0.0, 1.0), normMap, uWaterNormMapDepth);
    vec3 correctedNorm = normalize((uNVMatrix * vec4(normMap, 1.0)).xyz);
    vec3 correctedLightDirection = (uNVMatrix * vec4(uLightDirection, 1.0)).xyz;
    specular = setupSpecularLight(correctedLightDirection, correctedNorm, uWaterLightSpecularIntensity, uWaterLightSpecularHardness);
}
// Copied from Water Shader ends +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

void main(void) {
    float shadowFactor = 1.0;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    if(vSlopeFactor <= 0.0) {
        // Render full ground
       setupGround(ambient, diffuse, specular);
    } else if(vSlopeFactor >= 1.0) {
        // Render full slope
        vec4 textureColor;
        vec3 correctedNorm;
        if(uHasWater) {
            // TODO one bugs
            // TODO -Small stripe between water and coast
            float z = vVertexPositionCoord.z;
            if(z >= uWaterLevel) {
                 shadowFactor = calculateShadowFactor();

                 vec3 slopeAmbient;
                 vec3 slopeDiffuse;
                 vec3 slopeSpecular;
                 setupSlope(slopeAmbient, slopeDiffuse, slopeSpecular);
                 vec4 slope = setupColor(slopeAmbient, slopeDiffuse, slopeSpecular, true, true);

                 vec3 waterSurfaceAmbient;
                 vec3 waterSurfaceSpecular;
                 setupWater(waterSurfaceAmbient, waterSurfaceSpecular);
                 vec3 underWaterAmbient;
                 vec3 underWaterDiffuse;
                 setupUnderWater(underWaterAmbient, underWaterDiffuse, 1.0);
                 vec4 underWater = clamp(setupColor(underWaterAmbient, underWaterDiffuse, vec3(0.0, 0.0, 0.0), false, false), 0.0, 1.0);
                 vec4 waterSurface = clamp(setupColor(waterSurfaceAmbient, vec3(0.0, 0.0, 0.0), waterSurfaceSpecular, true, true), 0.0, 1.0);
                 vec4 water = mix(underWater, waterSurface, uWaterTransparency);

                 float splattingMap = (texture2D(uSlopeWaterSplatting, vVertexPositionCoord.xy * uSlopeWaterSplattingScale).r -0.5) * uSlopeWaterSplattingFactor;
                 float splattingHeight = z + splattingMap;
                 if(splattingHeight > uSlopeWaterSplattingHeight + uSlopeWaterSplattingFadeThreshold / 2.0) {
                     // Over slope water tranittion
                     gl_FragColor = slope;
                 } else if(splattingHeight < uSlopeWaterSplattingHeight - uSlopeWaterSplattingFadeThreshold / 2.0) {
                     // Under slope water tranittion
                     // float underWaterFactor = (z - uWaterGround) / (uWaterLevel - uWaterGround);
                     gl_FragColor = water;
                 } else {
                     // In slope water tranittion
                     float mixFactor = (splattingHeight - uSlopeWaterSplattingHeight)/uSlopeWaterSplattingFadeThreshold + 0.5;
                     gl_FragColor = mix(water, slope, mixFactor);
                 }
            } else {
                // Under water
                float underWaterFactor = (z - uWaterGround) / (uWaterLevel - uWaterGround);
                setupUnderWater(ambient, diffuse, underWaterFactor);
                gl_FragColor = setupColor(ambient, diffuse, vec3(0.0, 0.0, 0.0), false, false);
            }
        } else {
             shadowFactor = calculateShadowFactor();
             setupSlope(ambient, diffuse, specular);
             gl_FragColor = setupColor(ambient, diffuse, specular, true, true);
        }
    } else {
       // Render slope-ground transition

       float slopeBmFactor = triPlanarTextureMapping(uSlopeBm, uSlopeBmScale, vec2(0,0)).r - 0.5;

       if(vSlopeFactor + slopeBmFactor > 1.0) {
            setupSlope(ambient, diffuse, specular);
       } else {
           setupGround(ambient, diffuse, specular);
       }
       gl_FragColor = setupColor(ambient, diffuse, specular, true, true);
   }

}