precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec3 vVertexPosition;
varying vec3 vWorldVertexPosition;

// Light
uniform vec3 uLightDirection;
uniform vec3 uLightDiffuse;
uniform vec3 uLightAmbient;
uniform float uLightSpecularIntensity;
uniform float uLightSpecularHardness;

uniform highp mat4 uNVMatrix;
uniform float uTransparency;
uniform sampler2D uBm;
uniform float uBmScale;
uniform float uBmDepth;
uniform float uBmOnePixel;
uniform float animation;
uniform float animation2;
// Terrain marker
uniform sampler2D uTerrainMarkerTexture;
uniform vec4 uTerrainMarker2DPoints;
uniform float uTerrainMarkerAnimation;

const vec3 SPECULAR_LIGHT_COLOR = vec3(1.0, 1.0, 1.0);
const vec3 WATER_COLOR = vec3(0.05, 0.32, 0.63);

vec3 bumpMapNorm(float scale) {
    vec3 normal = normalize(vVertexNormal);
    vec3 tangent = normalize(vVertexTangent);
    vec3 binormal = cross(normal, tangent);

    float bm0 = texture2D(uBm, vWorldVertexPosition.xy * scale).r;
    float bm0Up = texture2D(uBm, vWorldVertexPosition.xy * scale + vec2(0.0, uBmOnePixel)).r;
    float bm0Right = texture2D(uBm, vWorldVertexPosition.xy * scale + vec2(uBmOnePixel, 0.0)).r;

    float bm1 = texture2D(uBm, vWorldVertexPosition.xy * scale + vec2(0.1, 0.1)).r;
    float bm1Up = texture2D(uBm, vWorldVertexPosition.xy * scale + vec2(0.1, 0.1) + vec2(0.0, uBmOnePixel)).r;
    float bm1Right = texture2D(uBm, vWorldVertexPosition.xy * scale + vec2(0.1, 0.1) + vec2(uBmOnePixel, 0.0)).r;

    vec3 bump0Vector = (bm0 - bm0Right) * tangent + (bm0 - bm0Up) * binormal;
    vec3 bump1Vector = (bm1 - bm1Right) * tangent + (bm1 - bm1Up) * binormal;
    return normalize(normal + uBmDepth * (bump0Vector * animation + bump1Vector * animation2));
}

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
    vec3 norm = bumpMapNorm(uBmScale);
    vec3 correctedLigtDirection = (uNVMatrix * vec4(uLightDirection, 1.0)).xyz;

    vec3 ambient = uLightAmbient * WATER_COLOR;
    vec3 diffuse = max(dot(normalize(norm), normalize(-correctedLigtDirection)), 0.0)* uLightDiffuse * WATER_COLOR /* * shadowFactor */ ;
    vec3 specular = setupSpecularLight(correctedLigtDirection, norm, uLightSpecularIntensity, uLightSpecularHardness) /* * shadowFactor */;
    gl_FragColor = vec4(ambient + diffuse + specular, uTransparency) + setupTerrainMarker();
}

