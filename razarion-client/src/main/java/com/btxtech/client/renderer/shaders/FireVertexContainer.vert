attribute vec3 aVertexPosition;
attribute vec2 aTextureCoord;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 heightMatrix;

uniform float minHeight;
uniform float maxHeight;

varying vec2 vTextureCoord;
varying float height;

void main(void) {
    float z = (heightMatrix * vec4(aVertexPosition, 1.0)).z;
    height = z / (maxHeight - minHeight) + minHeight / (minHeight - maxHeight);
    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
    vTextureCoord = aTextureCoord;
}
