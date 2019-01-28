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
uniform bool slopeOriented;
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
// Terrain marker
uniform sampler2D uTerrainMarkerTexture;
uniform vec4 uTerrainMarker2DPoints;
uniform float uTerrainMarkerAnimation;

const vec3 SPECULAR_LIGHT_COLOR = vec3(1.0, 1.0, 1.0);
const float SLOPE_FACTOR_BIAS = 0.001;
const float GROUND_FACTOR_BIAS = 0.001;
const float SLOPE_WATER_STRIPE_FADEOUT = 2.0;
const float SLOPE_WATER_STRIPE_SPECULAR_INTENSITY_FACTOR = 5.0;
const vec3 UNDER_WATER_COLOR = vec3(1.0, 1.0, 1.0);

// Vector to RGB -> normVector * 0.5 + 0.5
// Interpolate x (MIN, MAX) to 0..1: 1.0/(MAX-MIN) * x + MIN/(MAX-MIN)

// http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
vec4 triPlanarTextureMapping(sampler2D sampler, float scale, vec2 addCoord) {
    vec3 blending = abs(vVertexNormCoord);

    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(sampler, vVertexPositionCoord.yz * scale + addCoord);
    vec4 yAxisTop = texture2D(sampler, vVertexPositionCoord.xz * scale + addCoord);
    vec4 zAxisTop = texture2D(sampler, vVertexPositionCoord.xy * scale + addCoord);
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

vec4 setupSpecularLight(vec3 correctedLightDirection, vec3 correctedNorm, float intensity, float hardness) {
     vec3 reflectionDirection = normalize(reflect(correctedLightDirection, normalize(correctedNorm)));
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     float factor = pow(max(dot(reflectionDirection, eyeDirection), 0.0), hardness) * intensity;
     return vec4(SPECULAR_LIGHT_COLOR * factor, 1.0);
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

void setupSlope(inout vec3 norm, inout vec4 textureColor) {
    textureColor = triPlanarTextureMapping(uSlopeTexture, uSlopeTextureScale, vec2(0,0));
    norm = bumpMapNorm(uSlopeBm, uSlopeBmDepth, uSlopeBmScale, uSlopeBmOnePixel);
}

void setupGround(inout vec3 norm, inout vec4 textureColor) {
        // Copied from Ground Shader and variable renamed (Ground added) ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        float shadowFactor = calculateShadowFactor();
        vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);

        vec4 colorTop = triPlanarTextureMapping(uGroundTopTexture, uGroundTopTextureScale, vec2(0,0));
        vec4 colorBottom = triPlanarTextureMapping(uGroundBottomTexture, uGroundBottomTextureScale, vec2(0,0));
        vec3 normBottom = bumpMapNorm(uGroundBottomBm, uGroundBottomBmDepth, uGroundBottomBmScale, uGroundBottomBmOnePixel);
        float splatting = triPlanarTextureMapping(uGroundSplatting, uGroundSplattingScale, vec2(0,0)).r;

        float bottomBmValue = triPlanarTextureMapping(uGroundBottomBm, uGroundBottomBmScale, vec2(0,0)).r;

        float splattingValue = vGroundSplatting - (splatting + bottomBmValue * uGroundSplattingGroundBmMultiplicator) / 2.0 + uGroundSplattingOffset;
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
        // Copied from Ground Shader ends +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}

void main(void) {
    vec3 correctedLight = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);
    float shadowFactor = calculateShadowFactor();

    vec4 ambient;
    vec4 diffuse;

    vec4 textureColor;
    vec3 correctedNorm;
    vec4 specular;

    if(vSlopeFactor < SLOPE_FACTOR_BIAS) {
        setupGround(correctedNorm, textureColor);
    } else if(vSlopeFactor + SLOPE_FACTOR_BIAS > 1.0) {
        // Slope
        if(uHasWater) {
            float z = vVertexPositionCoord.z;
            if(z > uWaterLevel) {
                // Over water level: render normal slope
                float MAX_WAVE_Z = 0.5;
                setupSlope(correctedNorm, textureColor);
                specular = setupSpecularLight(correctedLight, correctedNorm, uLightSpecularIntensitySlope, uLightSpecularHardnessSlope);
 //               if(z - uWaterLevel < MAX_WAVE_Z) {
                    float waveFactor = (z - uWaterLevel) / MAX_WAVE_Z;
                    float waterLevelFactor  = texture2D(uSlopeBm, vVertexPositionCoord.xy * uSlopeBmScale).r - 0.5;
                    waveFactor += waterLevelFactor;
                    if(waveFactor <= 1.0) {
                        // Transition water foam
                        if(waveFactor < 0.0) {
                            // Pure water
                            correctedNorm = normalize(vVertexNormal);
                            textureColor = vec4(1.0, 1.0, 1.0, 1.0);
                        } else {
                            // Transition water foam
                            correctedNorm = mix(normalize(vVertexNormal), correctedNorm, waveFactor);
                            textureColor = mix(vec4(1.0, 1.0, 1.0, 1.0), textureColor, waveFactor);
                        }
                    }
            } else {
                // Under water level: render slope fadeout
                float underWaterFactor = (z - uWaterGround) / (uWaterLevel - uWaterGround);
                vec3 slopeNorm = bumpMapNorm(uSlopeBm, uSlopeBmDepth, uSlopeBmScale, uSlopeBmOnePixel);
                vec3 ambient = uLightAmbient * UNDER_WATER_COLOR * underWaterFactor;
                vec3 diffuse = vec3(max(dot(normalize(slopeNorm), normalize(-correctedLight)), 0.0) * underWaterFactor * uLightDiffuse * UNDER_WATER_COLOR);
                gl_FragColor = vec4(vec3(ambient + diffuse), 1.0) * shadowFactor;
                return;
            }
        } else {
            setupSlope(correctedNorm, textureColor);
            specular = setupSpecularLight(correctedLight, correctedNorm, uLightSpecularIntensitySlope, uLightSpecularHardnessSlope);
        }
        ambient = vec4(uLightAmbient, 1.0) * textureColor;
        diffuse = vec4(max(dot(normalize(correctedNorm), -correctedLight), 0.0) * uLightDiffuse * textureColor.rgb, 1.0);
  } else {
       // Transition

       // Ground
       vec3 groundNorm;
       vec4 groundColor;
       setupGround(groundNorm, groundColor);

       // Slope
       vec3 slopeNorm;
       vec4 slopeColor;
       setupSlope(slopeNorm, slopeColor);
       float slopeBmFactor = triPlanarTextureMapping(uSlopeBm, uSlopeBmScale, vec2(0,0)).r;

       bool renderSlope;
       if(slopeOriented) {
           vec3 perpendicular = normalize((uNVMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz);
           float flatFactor = max(dot(slopeNorm, perpendicular), 0.0) - slopeBmFactor;
           renderSlope = flatFactor < vSlopeFactor;
       } else {
           float groundBottomBmValue = triPlanarTextureMapping(uGroundBottomBm, uGroundBottomBmScale, vec2(0,0)).r;
           float correctedSlopeFactor = vSlopeFactor - 0.5;
           renderSlope = slopeBmFactor + correctedSlopeFactor > groundBottomBmValue;
       }

       // Caluclate transition
       if(renderSlope) {
           ambient = vec4(uLightAmbient, 1.0) * slopeColor;
           diffuse = vec4(max(dot(normalize(slopeNorm), -correctedLight), 0.0) * uLightDiffuse * slopeColor.rgb, 1.0);
           specular = setupSpecularLight(correctedLight, slopeNorm, uLightSpecularIntensitySlope, uLightSpecularHardnessSlope);
       } else  {
           ambient = vec4(uLightAmbient, 1.0) * groundColor;
           diffuse = vec4(max(dot(normalize(groundNorm), -correctedLight), 0.0) * uLightDiffuse * groundColor.rgb, 1.0);
           specular = setupSpecularLight(correctedLight, groundNorm, uLightSpecularIntensityGround, uLightSpecularHardnessGround);
       }
   }

    gl_FragColor = ambient + diffuse * shadowFactor + specular * shadowFactor + setupTerrainMarker();
}