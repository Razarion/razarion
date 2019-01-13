precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexPosition;
varying vec3 vWorldVertexPosition;

// Light
uniform vec3 uLightDirection;
uniform float uLightSpecularIntensity;
uniform float uLightSpecularHardness;

uniform highp mat4 uNVMatrix;
uniform float uTransparency;
uniform sampler2D uReflection;
uniform sampler2D uNormMap;
uniform sampler2D uDistortionMap;
uniform float uReflectionScale;
uniform float uDistortionScale;
uniform float uDistortionStrength;
uniform float animation;
// Terrain marker
uniform sampler2D uTerrainMarkerTexture;
uniform vec4 uTerrainMarker2DPoints;
uniform float uTerrainMarkerAnimation;

const vec3 SPECULAR_LIGHT_COLOR = vec3(1.0, 1.0, 1.0);

vec3 setupSpecularLight(vec3 correctedLightDirection, vec3 correctedNorm, float intensity, float hardness) {
     vec3 reflectionDirection = normalize(reflect(correctedLightDirection, normalize(correctedNorm)));
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     float factor = pow(max(dot(reflectionDirection, eyeDirection), 0.0), hardness) * intensity;
     return SPECULAR_LIGHT_COLOR * factor;
}

vec4 setupTerrainMarker() {
    vec4 terrainMarkerColor = vec4(0.0, 0.0, 0.0, 0.0);
    if(uTerrainMarker2DPoints != vec4(0.0, 0.0, 0.0, 0.0)) {
        if(vWorldVertexPosition.x > uTerrainMarker2DPoints.x && vWorldVertexPosition.y > uTerrainMarker2DPoints.y && vWorldVertexPosition.x < uTerrainMarker2DPoints.z && vWorldVertexPosition.y < uTerrainMarker2DPoints.w) {
            float xLookup = (vWorldVertexPosition.x - uTerrainMarker2DPoints.x) / (uTerrainMarker2DPoints.z - uTerrainMarker2DPoints.x);
            float yLookup = (vWorldVertexPosition.y - uTerrainMarker2DPoints.y) / (uTerrainMarker2DPoints.w - uTerrainMarker2DPoints.y);
            vec4 lookupMarker = texture2D(uTerrainMarkerTexture, vec2(xLookup, yLookup));
            if(lookupMarker.r > 0.5) {
                terrainMarkerColor = vec4(0.0, uTerrainMarkerAnimation * 0.3, 0.0, 0.0);
            }
        }
    }
    return terrainMarkerColor;
}

void main(void) {
    vec2 distortion1 = texture2D(uDistortionMap, vWorldVertexPosition.xy * uDistortionScale + vec2(animation, 0)).rg * 2.0 - 1.0;
    vec2 distortion2 = texture2D(uDistortionMap, vWorldVertexPosition.xy * uDistortionScale + vec2(-animation, animation)).rg * 2.0 - 1.0;
    vec2 totalDistortion = distortion1 + distortion2;

    vec2 reflectionCoord = (vWorldVertexPosition.xy) * uReflectionScale + totalDistortion * uDistortionStrength/*  + vec2(0, animation)*/;
    vec3 refelectionColor = texture2D(uReflection, reflectionCoord).rgb;

    vec3 correctedLigtDirection = (uNVMatrix * vec4(uLightDirection, 1.0)).xyz;
     vec3 normal = normalize(vVertexNormal);
     vec3 normMapColor = texture2D(uNormMap, reflectionCoord).rgb;
     vec3 specularColor = setupSpecularLight(correctedLigtDirection,  vec3(normMapColor.r * 2.0 - 1.0, normMapColor.g * 2.0 - 1.0, normMapColor.b), uLightSpecularIntensity, uLightSpecularHardness);

    gl_FragColor = vec4(refelectionColor + specularColor, uTransparency) + setupTerrainMarker();
}

