precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec3 vVertexPosition;
varying vec3 vWorldVertexPosition;

uniform highp mat4 uNMatrix;
uniform float uTransparency;
uniform sampler2D uSamplerBm;
uniform float uBumpMapDepth;
uniform vec3 uAmbientColor;
uniform vec3 uLightingDirection;
uniform vec3 uLightingColor;
uniform float uSlopeSpecularHardness;
uniform float uSlopeSpecularIntensity;
uniform float animation;
uniform float animation2;

const vec3 WATER_COLOR = vec3(0.0, 0.0, 0.7);

vec3 bumpMapNorm(float scale) {
    vec3 normal = normalize(vVertexNormal);
    vec3 tangent = normalize(vVertexTangent);
    vec3 binormal = cross(normal,tangent);

//     float bm0 = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(animation / 100.0, 0)).r;
//     float bmUp = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(0.0, 1.0/scale)+ vec2(animation/ 100.0, 0)).r;
//     float bmRight = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(1.0/scale, 0.0)+ vec2(animation/ 100.0, 0)).r;

    float bm0 = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale).r;
    float bm0Up = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(0.0, 1.0/scale)).r;
    float bm0Right = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(1.0/scale, 0.0)).r;

    float bm1 = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(0.1, 0.1)).r;
    float bm1Up = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(0.1, 0.1) + vec2(0.0, 1.0/scale)).r;
    float bm1Right = texture2D(uSamplerBm, vWorldVertexPosition.xy / scale + vec2(0.1, 0.1) + vec2(1.0/scale, 0.0)).r;

    vec3 bump0Vector = (bm0Right - bm0)*tangent + (bm0Up - bm0)*binormal;
    vec3 bump1Vector = (bm1Right - bm1)*tangent + (bm1Up - bm1)*binormal;
    normal -= uBumpMapDepth * (bump0Vector * animation + bump1Vector * animation2);

    //normal -= uBumpMapDepth * bumpVector;
    return normalize(normal);
}

float setupSpecularLight(vec3 correctedLigtDirection, vec3 correctedNorm) {
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     vec3 reflectionDirection = normalize(reflect(-correctedLigtDirection, correctedNorm));
     return pow(max(dot(reflectionDirection, eyeDirection), 0.0), uSlopeSpecularHardness) * uSlopeSpecularIntensity;
}

void main(void) {
    vec3 norm = bumpMapNorm(512.0);
    vec3 correctedLigtDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;

    vec3 ambient = uAmbientColor * WATER_COLOR;
    vec3 diffuse = max(dot(normalize(norm), normalize(correctedLigtDirection)), 0.0) /* * shadowFactor */* uLightingColor * WATER_COLOR;
    float specularIntensity = setupSpecularLight(correctedLigtDirection, norm) /* * shadowFactor */;
    vec3 specular = vec3(specularIntensity, specularIntensity, specularIntensity);
    gl_FragColor = vec4(vec3(ambient + diffuse + specular), uTransparency);
}

