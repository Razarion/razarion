precision mediump float;

//-$$$-INCLUDE-DEFINES

attribute vec3 position;
#ifndef FIX_PERPENDICULAR_NORMAL
attribute vec3 objectNormal;
#endif
#ifdef UV
attribute vec2 uv;
#endif
//-$$$-INCLUDE-CHUNK attributes

#ifdef MODEL_MATRIX
uniform highp mat4 modelMatrix;
uniform highp mat4 modelNormMatrix;
#endif
uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;
uniform highp mat4 viewNormMatrix;
uniform highp mat4 shadowMatrix;
//-$$$-INCLUDE-CHUNK uniforms-vertex

varying vec3 vNormal;
varying vec3 vViewPosition;
#ifdef UV
varying vec2 vUv;
#endif
varying vec4 shadowPosition;
#ifdef WORLD_VERTEX_POSITION
varying vec3 vWorldVertexPosition;
#endif
//-$$$-INCLUDE-CHUNK varyings

//-$$$-INCLUDE-CHUNK code-vertex

void main(void) {
    vec4 norm;
    #ifdef FIX_PERPENDICULAR_NORMAL
    norm = vec4(0.0, 0.0, 1.0, 1.0);
    #else
    norm = vec4(objectNormal, 1.0);
    #endif

    #ifdef UV
    vUv = uv;
    #endif

    #ifdef MODEL_MATRIX
    shadowPosition = shadowMatrix * modelMatrix * vec4(position, 1.0);
    #else
    shadowPosition = shadowMatrix * vec4(position, 1.0);
    #endif

    #ifdef WORLD_VERTEX_POSITION
    vWorldVertexPosition = position.xyz;
    #endif

    //-$$$-INCLUDE-CHUNK main-code-vertex

    #ifdef MODEL_MATRIX
    vNormal = (viewNormMatrix * modelNormMatrix * norm).xyz;
    vViewPosition = - (viewMatrix * modelMatrix * vec4(position, 1.0)).xyz;
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    #else
    vNormal = (viewNormMatrix * norm).xyz;
    vViewPosition = - (viewMatrix * vec4(position, 1.0)).xyz;
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    #endif
}
