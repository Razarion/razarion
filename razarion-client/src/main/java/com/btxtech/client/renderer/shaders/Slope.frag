precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vSlopeFactor;
varying float vGroundSplatting;

uniform highp mat4 uNVMatrix;
// Light Slope
uniform vec3 uLightDirectionSlope;
uniform vec3 uLightDiffuseSlope;
uniform vec3 uLightAmbientSlope;
uniform float uLightSpecularIntensitySlope;
uniform float uLightSpecularHardnessSlope;
// Light Ground
uniform vec3 uLightDirectionGround;
uniform vec3 uLightDiffuseGround;
uniform vec3 uLightAmbientGround;
uniform float uLightSpecularIntensityGround;
uniform float uLightSpecularHardnessGround;
// Shadow
varying vec4 vShadowCoord;
uniform float uShadowAlpha;
uniform sampler2D uShadowTexture;

uniform bool slopeOriented;

//Slope
uniform sampler2D uSlopeTexture;
uniform float uSlopeTextureScale;
uniform sampler2D uSlopeBm;
uniform float uSlopeBmScale;
uniform float uSlopeBmOnePixel;
uniform float uSlopeBmDepth;
// Ground
uniform sampler2D uGroundTopTexture;
uniform float uGroundTopTextureScale;
uniform sampler2D uGroundTopBm;
uniform float uGroundTopBmDepth;
uniform float uGroundTopBmScale;
uniform float uGroundTopBmOnePixel;
uniform sampler2D uGroundBottomTexture;
uniform float uGroundBottomTextureScale;
uniform sampler2D uGroundBottomBm;
uniform float uGroundBottomBmScale;
uniform float uGroundBottomBmOnePixel;
uniform float uGroundBottomBmDepth;
uniform sampler2D uGroundSplatting;
uniform float uGroundSplattingScale;
// Water
uniform bool uHasWater;
uniform float uWaterLevel;
uniform float uWaterGround;

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

void main(void) {
    vec3 correctedLightSlope = normalize((uNVMatrix * vec4(uLightDirectionSlope, 1.0)).xyz);
    vec3 correctedLightGround = normalize((uNVMatrix * vec4(uLightDirectionGround, 1.0)).xyz);

    float shadowFactor = calculateShadowFactor();

    vec4 ambient;
    vec4 diffuse;

    vec4 textureColor;
    vec3 correctedNorm;
    vec4 specular;

    if(vSlopeFactor < SLOPE_FACTOR_BIAS) {
        // Ground
        vec4 colorTop = triPlanarTextureMapping(uGroundTopTexture, uGroundTopTextureScale, vec2(0,0));
        vec3 normTop = bumpMapNorm(uGroundTopBm, uGroundTopBmDepth, uGroundTopBmScale, uGroundTopBmOnePixel);
        vec4 colorBottom = triPlanarTextureMapping(uGroundBottomTexture, uGroundBottomTextureScale, vec2(0,0));
        vec3 normBottom = bumpMapNorm(uGroundBottomBm, uGroundBottomBmDepth, uGroundBottomBmScale, uGroundBottomBmOnePixel);
        float splatting = triPlanarTextureMapping(uGroundSplatting, uGroundSplattingScale, vec2(0,0)).r;

        // Bottom top splatting
        if(vGroundSplatting + GROUND_FACTOR_BIAS >= 1.0) {
            correctedNorm = normTop;
            textureColor = colorTop;
        } else if(vGroundSplatting <= GROUND_FACTOR_BIAS) {
            correctedNorm = normBottom;
            textureColor = colorBottom;
        } else {
            float topBmValue = triPlanarTextureMapping(uGroundTopBm, uGroundTopBmScale, vec2(0,0)).r;

            if(topBmValue + splatting < vGroundSplatting) {
                correctedNorm = normTop;
                textureColor = colorTop;
            } else {
                correctedNorm = normBottom;
                textureColor = colorBottom;
            }
        }
        specular = setupSpecularLight(correctedLightGround, correctedNorm, uLightSpecularIntensityGround, uLightSpecularHardnessGround);
        ambient = vec4(uLightAmbientGround, 1.0) * textureColor;
        diffuse = vec4(max(dot(normalize(correctedNorm), -correctedLightGround), 0.0) * uLightDiffuseGround * textureColor.rgb, 1.0);
    } else if(vSlopeFactor + SLOPE_FACTOR_BIAS > 1.0) {
        // Slope
        if(uHasWater) {
            float z = vVertexPositionCoord.z;
            if(z > uWaterLevel) {
                // Over water level: render normal slope
                textureColor = triPlanarTextureMapping(uSlopeTexture, uSlopeTextureScale, vec2(0,0));
                correctedNorm = bumpMapNorm(uSlopeBm, uSlopeBmDepth, uSlopeBmScale, uSlopeBmOnePixel);
                specular = setupSpecularLight(correctedLightSlope, correctedNorm, uLightSpecularIntensitySlope, uLightSpecularHardnessSlope);
            } else {
                // Under water level: render slope fadeout
                float underWaterFactor = (z - uWaterGround) / (uWaterLevel - uWaterGround);
                vec3 slopeNorm = bumpMapNorm(uSlopeBm, uSlopeBmDepth, uSlopeBmScale, uSlopeBmOnePixel);
                vec3 ambient = uLightAmbientSlope * UNDER_WATER_COLOR * underWaterFactor;
                vec3 diffuse = vec3(max(dot(normalize(slopeNorm), normalize(-correctedLightSlope)), 0.0) * underWaterFactor * uLightDiffuseSlope * UNDER_WATER_COLOR);
                gl_FragColor = vec4(vec3(ambient + diffuse), 1.0) * shadowFactor;
                return;
            }
        } else {
            textureColor = triPlanarTextureMapping(uSlopeTexture, uSlopeTextureScale, vec2(0,0));
            correctedNorm = bumpMapNorm(uSlopeBm, uSlopeBmDepth, uSlopeBmScale, uSlopeBmOnePixel);
            specular = setupSpecularLight(correctedLightSlope, correctedNorm, uLightSpecularIntensitySlope, uLightSpecularHardnessSlope);
        }
        ambient = vec4(uLightAmbientSlope, 1.0) * textureColor;
        diffuse = vec4(max(dot(normalize(correctedNorm), -correctedLightSlope), 0.0) * uLightDiffuseSlope * textureColor.rgb, 1.0);
  } else {
       // Transition

       // Ground
       vec4 colorTop = triPlanarTextureMapping(uGroundTopTexture, uGroundTopTextureScale, vec2(0,0));
       vec3 normTop = bumpMapNorm(uGroundTopBm, uGroundTopBmDepth, uGroundTopBmScale, uGroundTopBmOnePixel);
       vec4 colorBottom = triPlanarTextureMapping(uGroundBottomTexture, uGroundBottomTextureScale, vec2(0,0));
       vec3 normBottom = bumpMapNorm(uGroundBottomBm, uGroundBottomBmDepth, uGroundBottomBmScale, uGroundBottomBmOnePixel);
       float splatting = triPlanarTextureMapping(uGroundSplatting, uGroundSplattingScale, vec2(0,0)).r;
       vec4 groundSpecular = setupSpecularLight(correctedLightGround, correctedNorm, uLightSpecularIntensityGround, uLightSpecularHardnessGround);
       // Bottom top splatting
       vec3 groundNorm;
       vec4 groundColor;
       if(vGroundSplatting + GROUND_FACTOR_BIAS >= 1.0) {
           groundNorm = normTop;
           groundColor = colorTop;
       } else if(vGroundSplatting <= GROUND_FACTOR_BIAS) {
           groundNorm = normBottom;
           groundColor = colorBottom;
       } else {
           float topBmValue = triPlanarTextureMapping(uGroundTopBm, uGroundTopBmScale, vec2(0,0)).r;

           if(topBmValue + splatting < vGroundSplatting) {
               groundNorm = normTop;
               groundColor = colorTop;
           } else {
               groundNorm = normBottom;
               groundColor = colorBottom;
           }
       }

       // Slope
       vec3 slopeNorm = bumpMapNorm(uSlopeBm, uSlopeBmDepth, uSlopeBmScale, uSlopeBmOnePixel);
       vec4 slopeColor = triPlanarTextureMapping(uSlopeTexture, uSlopeTextureScale, vec2(0,0));
       float slopeBmFactor = triPlanarTextureMapping(uSlopeBm, uSlopeBmScale, vec2(0,0)).r;

       bool renderSlope;
       if(slopeOriented) {
           vec3 perpendicular = normalize((uNVMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz);
           float flatFactor = max(dot(slopeNorm, perpendicular), 0.0) - slopeBmFactor;
           renderSlope = flatFactor < vSlopeFactor;
       } else {
           float groundTopBmValue = triPlanarTextureMapping(uGroundTopBm, uGroundTopBmScale, vec2(0,0)).r;
           float groundBottomBmValue = triPlanarTextureMapping(uGroundBottomBm, uGroundBottomBmScale, vec2(0,0)).r;
           float correctedSlopeFactor = vSlopeFactor - 0.5;
           renderSlope = (slopeBmFactor + correctedSlopeFactor > groundBottomBmValue) && (slopeBmFactor + correctedSlopeFactor > groundTopBmValue);
       }

       // Caluclate transition
       if(renderSlope) {
           ambient = vec4(uLightAmbientSlope, 1.0) * slopeColor;
           diffuse = vec4(max(dot(normalize(slopeNorm), -correctedLightSlope), 0.0) * uLightDiffuseSlope * slopeColor.rgb, 1.0);
           specular = setupSpecularLight(correctedLightSlope, slopeNorm, uLightSpecularIntensitySlope, uLightSpecularHardnessSlope);
       } else  {
           ambient = vec4(uLightAmbientGround, 1.0) * groundColor;
           diffuse = vec4(max(dot(normalize(groundNorm), -correctedLightGround), 0.0) * uLightDiffuseGround * groundColor.rgb, 1.0);
           specular = setupSpecularLight(correctedLightGround, groundNorm, uLightSpecularIntensityGround, uLightSpecularHardnessGround);
       }
   }

   // Light
    gl_FragColor = ambient + diffuse * shadowFactor + specular * shadowFactor;
}