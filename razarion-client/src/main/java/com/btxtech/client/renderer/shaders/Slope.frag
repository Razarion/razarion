precision mediump float;

varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec3 vNormal;
uniform highp mat4 normalMatrix;

// Light
uniform vec3 directLightDirection;
uniform vec3 directLightColor;
uniform vec3 ambientLightColor;
// Shadow
varying vec4 vShadowCoord;

void main(void) {
    gl_FragColor = vec4(0.9, 0.5, 0.2, 1.0);
}
