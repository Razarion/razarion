precision mediump float;

attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec3 aVertexTangent;
attribute float aSlopeFactor;
attribute float aGroundSplatting;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNVMatrix;
uniform highp mat4 uShadowMatrix;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vSlopeFactor;
varying float vGroundSplatting;
varying vec4 vShadowCoord;

void main(void) {
    vVertexNormal = (uNVMatrix * vec4(aVertexNormal, 1.0)).xyz;
    vVertexTangent = (uNVMatrix * vec4(aVertexTangent, 1.0)).xyz;
    vVertexPosition = uVMatrix * vec4(aVertexPosition, 1.0);
    vShadowCoord = uShadowMatrix * vec4(aVertexPosition, 1.0);
    gl_Position = uPMatrix * vVertexPosition;
    vVertexPositionCoord = aVertexPosition.xyz;
    vVertexNormCoord = aVertexNormal;
    vSlopeFactor = aSlopeFactor;
    vGroundSplatting = aGroundSplatting;
}

