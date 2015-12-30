precision mediump float;

varying vec3 vVertexNormal;
//varying vec3 vVertexTangent;
varying vec3 vVertexPosition;

uniform highp mat4 uNMatrix;
uniform vec3 uAmbientColor;
uniform vec3 uLightingDirection;
uniform vec3 uLightingColor;

const vec3 COLOR = vec3(0.5, 0.5, 0.5);
const float hardness = 2.0;
const float intensity = 0.5;

float setupSpecularLight(vec3 correctedLigtDirection) {
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     vec3 reflectionDirection = normalize(reflect(-correctedLigtDirection, vVertexNormal));
     return pow(max(dot(reflectionDirection, eyeDirection), 0.0), hardness) * intensity;
}

void main(void) {
    vec3 correctedLigtDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;

    vec3 ambient = uAmbientColor * COLOR;
    vec3 diffuse = max(dot(normalize(vVertexNormal), normalize(correctedLigtDirection)), 0.0) /* * shadowFactor */* uLightingColor * COLOR;
    float specularIntensity = setupSpecularLight(correctedLigtDirection) /* * shadowFactor */;
    vec3 specular = vec3(specularIntensity, specularIntensity, specularIntensity);
    gl_FragColor = vec4(vec3(ambient + diffuse + specular), 1.0);
}

