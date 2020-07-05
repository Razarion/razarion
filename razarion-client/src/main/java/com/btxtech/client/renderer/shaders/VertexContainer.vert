precision mediump float;

attribute vec3 position;
attribute vec3 objectNormal;
attribute vec2 uv;

uniform highp mat4 modelMatrix;
uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;
uniform highp mat4 normalMatrix;
uniform highp mat4 shadowMatrix;

varying vec3 vNormal;
varying vec3 vViewPosition;
varying vec2 vUv;
varying vec4 vShadowCoord;

void main(void) {
    vNormal = (normalMatrix * vec4(objectNormal, 1.0)).xyz;
    vViewPosition = - (viewMatrix * modelMatrix * vec4(position, 1.0)).xyz;
    vUv = uv;
    vShadowCoord = shadowMatrix * vec4(position, 1.0);

    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
}
