precision mediump float;

varying vec3 vVertexPosition;
varying vec3 vWorldVertexPosition;

// Light
uniform vec3 uLightDirection;
uniform float uLightSpecularIntensity;
uniform float uLightSpecularHardness;

uniform highp mat4 uNVMatrix;
uniform float uTransparency;
uniform sampler2D uNormMap;
uniform float uNormMapDepth;
uniform sampler2D uDistortionMap;
uniform float uReflectionScale;
uniform sampler2D uReflection;
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

// +++ Also used in Slop.frag
void setupWater(inout vec3 ambient, inout vec3 specular) {
    // Setup ambient
    vec2 distortion1 = texture2D(uDistortionMap, vWorldVertexPosition.xy / uDistortionScale + vec2(animation, 0.5)).rg * 2.0 - 1.0;
    vec2 distortion2 = texture2D(uDistortionMap, vWorldVertexPosition.xy / uDistortionScale + vec2(-animation, animation)).rg * 2.0 - 1.0;
    vec2 totalDistortion = distortion1 + distortion2;
    vec2 reflectionCoord = (vWorldVertexPosition.xy) / uReflectionScale + totalDistortion * uDistortionStrength;
    ambient = texture2D(uReflection, reflectionCoord).rgb;
    // Setup norm map and light
    vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);
    vec3 normMap1 = texture2D(uNormMap, vWorldVertexPosition.xy / uDistortionScale + vec2(animation, 0.5)).xyz;
    vec3 normMap2 = texture2D(uNormMap, vWorldVertexPosition.xy / uDistortionScale + vec2(-animation, animation)).xyz;
    vec3 normMap = normMap1 + normMap2;
    normMap = normalize(vec3(normMap.x - 1.0, normMap.y - 1.0, normMap.z / 2.0));
    normMap = mix(vec3(0.0, 0.0, 1.0), normMap, uNormMapDepth);
    vec3 correctedNorm = normalize((uNVMatrix * vec4(normMap, 1.0)).xyz);
    specular = setupSpecularLight(correctedLightDirection, correctedNorm, uLightSpecularIntensity, uLightSpecularHardness);
}
// +++ Also used in Slop.frag ends

void main(void) {
    vec3 ambient;
    vec3 specular;
    setupWater(ambient, specular);
    gl_FragColor = vec4(ambient + specular, uTransparency) + setupTerrainMarker();
}

