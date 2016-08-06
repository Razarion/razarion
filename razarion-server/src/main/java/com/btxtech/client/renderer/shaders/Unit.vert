attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec2 aTextureCoord;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNMMatrix;
uniform highp mat4 uNVMatrix;

varying vec3 vVertexNormal;
varying vec3 vVertexPosition;
varying vec2 vTextureCoord;

void main(void) {
    vVertexNormal = normalize((uNVMatrix * uNMMatrix * vec4(aVertexNormal, 1.0)).xyz);
    vVertexPosition = (uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0)).xyz;
    vTextureCoord = aTextureCoord;

    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
}
