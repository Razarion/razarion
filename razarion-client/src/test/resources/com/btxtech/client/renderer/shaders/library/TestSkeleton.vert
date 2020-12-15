//-$$$-INCLUDE-EXTENSIONS
//-$$$-INCLUDE-DEFINES

attribute vec3 position;
//-$$$-INCLUDE-CHUNK attributes

uniform highp mat4 shadowMatrix;
//-$$$-INCLUDE-CHUNK uniforms

varying vec3 vWorldVertexPosition;
//-$$$-INCLUDE-CHUNK varyings

void main(void) {
    vNormal = (normalMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz;

    //-$$$-INCLUDE-CHUNK main-code-vertx

    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}
