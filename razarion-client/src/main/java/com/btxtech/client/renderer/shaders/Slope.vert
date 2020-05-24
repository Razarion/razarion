precision mediump float;

attribute vec3 position;
attribute vec3 objectNormal;
attribute vec2 uv;

uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;
uniform highp mat4 normalMatrix;
uniform highp mat4 shadowMatrix;

varying vec3 vNormal;
varying vec3 vViewPosition;
varying vec3 vWorldVertexPosition;
varying vec4 vShadowCoord;
varying vec2 vUv;

void main(void) {
    vWorldVertexPosition = position.xyz;
    vNormal = (normalMatrix * vec4(objectNormal, 1.0)).xyz;
    vViewPosition = - (viewMatrix * vec4(position, 1.0)).xyz;
    vShadowCoord = shadowMatrix * vec4(position, 1.0);
    vUv = uv;

    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}

