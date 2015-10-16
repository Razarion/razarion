precision mediump float;

attribute vec3 aVertexPosition;
attribute vec2 aTextureCoord;
attribute vec3 aVertexNormal;
attribute float aEdgePosition;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uMVPDepthBias;
uniform highp mat4 uNMatrix;
uniform vec3 uAmbientColor;

varying vec2 vTextureCoord;
varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec4 vVertexPosition;
varying float vEdgePosition;

void main(void) {
    vVertexNormal = (uNMatrix * vec4(aVertexNormal, 1.0)).xyz;
    vVertexPosition = uVMatrix * vec4(aVertexPosition, 1.0);
    gl_Position = uPMatrix * vVertexPosition;
    vShadowCoord = uMVPDepthBias * vec4(aVertexPosition, 1.0);
    vTextureCoord = aTextureCoord;
    vEdgePosition = aEdgePosition;
}

