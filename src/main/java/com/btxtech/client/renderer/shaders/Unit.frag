precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexPosition;
varying vec2 vTextureCoord;

uniform highp mat4 uNVMatrix;
uniform vec3 uAmbientColor;
uniform vec3 uLightingDirection;
uniform vec3 uLightingColor;
uniform sampler2D uSampler;
uniform float uSpecularHardness;
uniform float uSpecularIntensity;

float setupSpecularLight(vec3 correctedLigtDirection) {
    vec3 eyeDirection = normalize(-vVertexPosition.xyz);
    vec3 reflectionDirection = normalize(reflect(-correctedLigtDirection, vVertexNormal));
    return pow(max(dot(reflectionDirection, eyeDirection), 0.0), uSpecularHardness) * uSpecularIntensity;
}

void main(void) {
    vec3 correctedLigtDirection = (uNVMatrix * vec4(uLightingDirection, 1.0)).xyz;
    vec4 texColor = texture2D(uSampler, vTextureCoord);

    vec3 ambient = uAmbientColor * texColor.rgb;
    vec3 diffuse = max(dot(normalize(vVertexNormal), normalize(correctedLigtDirection)), 0.0) /* * shadowFactor */* uLightingColor * texColor.rgb;
    float specularIntensity = setupSpecularLight(correctedLigtDirection) /* * shadowFactor */;
    vec3 specular = vec3(specularIntensity, specularIntensity, specularIntensity);
    gl_FragColor = vec4(ambient + diffuse + specular, 1.0);

//    ////
//    vec3 xxx = normalize(correctedLigtDirection) * 0.5 + 0.5;
//    gl_FragColor = vec4(xxx, 1.0);
}

