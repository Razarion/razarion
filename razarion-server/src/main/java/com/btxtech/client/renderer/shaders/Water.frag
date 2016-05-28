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

uniform highp mat4 uNMatrix;
uniform float uTransparency;
uniform sampler2D uSamplerBumpMap;
uniform int uBumpMapSize;
uniform float uBumpMapDepth;
uniform float animation;
uniform float animation2;

const vec3 SPECULAR_LIGHT_COLOR = vec3(1.0, 1.0, 1.0);
const vec3 WATER_COLOR = vec3(0.0, 0.0, 0.7);

vec3 bumpMapNorm(float scale) {
    vec3 normal = normalize(vVertexNormal);
    vec3 tangent = normalize(vVertexTangent);
    vec3 binormal = cross(normal, tangent);

//     float bm0 = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(animation / 100.0, 0)).r;
//     float bmUp = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(0.0, 1.0/scale)+ vec2(animation/ 100.0, 0)).r;
//     float bmRight = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(1.0/scale, 0.0)+ vec2(animation/ 100.0, 0)).r;

    float bm0 = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale).r;
    float bm0Up = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(0.0, 1.0/scale)).r;
    float bm0Right = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(1.0/scale, 0.0)).r;

    float bm1 = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(0.1, 0.1)).r;
    float bm1Up = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(0.1, 0.1) + vec2(0.0, 1.0/scale)).r;
    float bm1Right = texture2D(uSamplerBumpMap, vWorldVertexPosition.xy / scale + vec2(0.1, 0.1) + vec2(1.0/scale, 0.0)).r;

    vec3 bump0Vector = (bm0Right - bm0)*tangent + (bm0Up - bm0)*binormal;
    vec3 bump1Vector = (bm1Right - bm1)*tangent + (bm1Up - bm1)*binormal;
    normal -= uBumpMapDepth * (bump0Vector * animation + bump1Vector * animation2);

    //normal -= uBumpMapDepth * bumpVector;
    return normalize(normal);
}

vec3 setupSpecularLight(vec3 correctedLightDirection, vec3 correctedNorm, float intensity, float hardness) {
     vec3 reflectionDirection = normalize(reflect(correctedLightDirection, normalize(correctedNorm)));
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     float factor = max(pow(dot(reflectionDirection, eyeDirection), hardness), 0.0) * intensity;
     return SPECULAR_LIGHT_COLOR * factor;
}

void main(void) {
    vec3 norm = bumpMapNorm(float(uBumpMapSize));
    vec3 correctedLigtDirection = (uNMatrix * vec4(uLightDirection, 1.0)).xyz;

    vec3 ambient = uLightAmbient * WATER_COLOR;
    vec3 diffuse = max(dot(normalize(norm), normalize(-correctedLigtDirection)), 0.0)* uLightDiffuse * WATER_COLOR /* * shadowFactor */ ;
    vec3 specular = setupSpecularLight(correctedLigtDirection, norm, uLightSpecularHardness, uLightSpecularIntensity) /* * shadowFactor */;
    gl_FragColor = vec4(ambient + diffuse + specular, uTransparency);
}

