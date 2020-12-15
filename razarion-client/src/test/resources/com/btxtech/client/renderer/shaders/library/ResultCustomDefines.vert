#extension GL_OES_standard_derivatives : enable
#define RENDER_BOTTOM_1
#define RENDER_BOTTOM_2

attribute vec3 position;
attribute float slopeFactor;

uniform highp mat4 shadowMatrix;


varying vec3 vWorldVertexPosition;
varying float vSlopeFactor;

void main(void) {
    vNormal = (normalMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz;

vSlopeFactor = slopeFactor;

    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}