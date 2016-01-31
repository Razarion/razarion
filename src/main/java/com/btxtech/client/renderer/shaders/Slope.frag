precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;

uniform highp mat4 uNMatrix;
uniform vec3 uLightingDirection;
uniform float diffuseWeightFactor;
uniform vec3 uAmbientColor;

void main(void) {
    vec4 ambient = vec4(uAmbientColor, 1.0) * vec4(0.5, 0.5, 0.5, 1.0);
    vec4 diffuse = vec4(max(dot(normalize(vVertexNormal), normalize(uLightingDirection)), 0.0) * diffuseWeightFactor * vec3(0.5, 0.5, 0.5), 1.0);
    gl_FragColor = ambient + diffuse;
}