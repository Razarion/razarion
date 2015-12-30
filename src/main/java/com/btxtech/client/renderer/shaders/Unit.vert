attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
//attribute vec3 aVertexTangent;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNMatrix;

varying vec3 vVertexNormal;
//varying vec3 vVertexTangent;
varying vec3 vVertexPosition;
//varying vec3 vWorldVertexPosition;

void main(void) {
  vVertexNormal = (uNMatrix * vec4(aVertexNormal, 1.0)).xyz;
  //  vVertexTangent = (uNMatrix * vec4(aVertexTangent, 1.0)).xyz;
  vVertexPosition = (uVMatrix * vec4(aVertexPosition, 1.0)).xyz;
  //  vWorldVertexPosition = aVertexPosition.xyz;

    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
}
