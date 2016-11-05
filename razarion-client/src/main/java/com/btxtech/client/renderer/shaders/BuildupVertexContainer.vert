attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec2 aTextureCoord;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNMMatrix;
uniform highp mat4 uNVMatrix;
uniform highp mat4 uShadowMatrix;
uniform highp mat4 buildupMatrix;

varying vec3 vVertexNormal;
varying vec2 vTextureCoord;
varying vec4 vShadowCoord;
varying float buildupZ;

void main(void) {
    buildupZ = (buildupMatrix * vec4(aVertexPosition, 1.0)).z;
    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
    vVertexNormal = normalize((uNVMatrix * uNMMatrix * vec4(aVertexNormal, 0.0)).xyz);
    vTextureCoord = aTextureCoord;
    vShadowCoord = uShadowMatrix * vec4(aVertexPosition, 1.0);
}
