attribute vec3 aVertexPosition;
attribute vec2 aAlphaTextureCoordinate;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;

varying vec2 vAlphaTextureCoordinate;

void main(void) {
    vAlphaTextureCoordinate = aAlphaTextureCoordinate;
    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
}
