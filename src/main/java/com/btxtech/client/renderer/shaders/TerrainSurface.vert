precision mediump float;

attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec3 aVertexTangent;
attribute float aEdgePosition;
attribute float aSlopeFactor;
attribute float aType;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uMVPDepthBias;
uniform highp mat4 uNMatrix;
uniform vec3 uAmbientColor;

varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vEdgePosition;
varying float vSlopeFactor;
varying float vType;


void main(void) {
    vVertexNormal = (uNMatrix * vec4(aVertexNormal, 1.0)).xyz;
    vVertexTangent = (uNMatrix * vec4(aVertexTangent, 1.0)).xyz;
    vVertexPosition = uVMatrix * vec4(aVertexPosition, 1.0);
    gl_Position = uPMatrix * vVertexPosition;
    vShadowCoord = uMVPDepthBias * vec4(aVertexPosition, 1.0);
    vEdgePosition = aEdgePosition;
    vVertexPositionCoord = aVertexPosition.xyz;
    vVertexNormCoord = aVertexNormal;
    vSlopeFactor = aSlopeFactor;
    vType = aType;
}

