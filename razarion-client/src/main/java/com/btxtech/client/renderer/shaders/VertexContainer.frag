#extension GL_OES_standard_derivatives : enable
precision mediump float;

//-$$$-INCLUDE-DEFINES

varying vec3 vViewPosition;
varying vec3 vNormal;
varying vec2 vUv;
uniform highp mat4 normalMatrix;

// Light
uniform vec3 directLightDirection;
uniform vec3 directLightColor;
uniform vec3 ambientLightColor;

void main(void) {
        gl_FragColor = vec4(0.8, 0.8, 0.8, 1.0);
}
