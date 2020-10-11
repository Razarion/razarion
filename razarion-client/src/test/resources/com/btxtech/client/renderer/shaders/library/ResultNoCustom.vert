attribute vec3 position;

uniform highp mat4 shadowMatrix;

varying vec3 vWorldVertexPosition;

void main(void) {
    vNormal = (normalMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz;


    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}