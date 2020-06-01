precision mediump float;

//-$$$-INCLUDE-DEFINES

varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec3 vNormal;
varying vec2 vUv;
varying float vSlopeFactor;
uniform highp mat4 normalMatrix;


void main(void) {
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}