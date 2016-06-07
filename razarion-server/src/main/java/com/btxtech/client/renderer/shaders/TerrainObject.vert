attribute vec3 aVertexPosition;
attribute vec2 aTextureCoord;
attribute vec3 aVertexNormal;

uniform highp mat4 uMMatrix;
uniform highp mat4 uNMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uNVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uShadowMatrix;

varying vec3 vVertexNormal;
varying vec2 vTextureCoord;
varying vec4 vShadowCoord;

void main(void) {
    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
    vVertexNormal = (uNVMatrix * uNMatrix * vec4(aVertexNormal, 1.0)).xyz;
    vTextureCoord = aTextureCoord;
    vShadowCoord = uShadowMatrix * vec4(aVertexPosition, 1.0);
}
