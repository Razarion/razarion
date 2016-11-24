attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec2 aTextureCoord;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 buildupMatrix;

varying vec2 vTextureCoord;
varying float buildupZ;

void main(void) {
    buildupZ = (buildupMatrix * vec4(aVertexPosition, 1.0)).z;
    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
    vTextureCoord = aTextureCoord;
}
