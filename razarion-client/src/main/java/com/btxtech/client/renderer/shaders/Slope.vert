precision mediump float;

attribute vec3 position;
attribute vec3 objectNormal;
attribute vec2 uv;
attribute float slopeFactor;

uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;
uniform highp mat4 normalMatrix;
uniform highp mat4 shadowMatrix;

varying vec3 vNormal;
varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec2 vUv;
varying float vSlopeFactor;
varying vec4 shadowPosition;

void main(void) {
    vWorldVertexPosition = position.xyz;
    vNormal = (normalMatrix * vec4(objectNormal, 1.0)).xyz;
    vViewPosition = - (viewMatrix * vec4(position, 1.0)).xyz;
    vUv = uv;
    vSlopeFactor = slopeFactor;
    shadowPosition = shadowMatrix * vec4(position, 1.0);

    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}
