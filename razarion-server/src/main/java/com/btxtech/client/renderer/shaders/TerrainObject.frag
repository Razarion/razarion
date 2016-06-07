precision mediump float;

varying vec3 vVertexNormal;
varying vec2 vTextureCoord;

uniform highp mat4 uNVMatrix;
uniform sampler2D uTexture;
uniform vec3 uLightAmbient;
uniform vec3 uLightDiffuse;
uniform vec3 uLightDirection;
// Shadow
varying vec4 vShadowCoord;
uniform float uShadowAlpha;
uniform sampler2D uShadowTexture;


float calculateShadowFactor() {
    float zMap = texture2D(uShadowTexture, vShadowCoord.st).r;

    if(zMap > vShadowCoord.z - 0.01) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}


void main(void) {
    vec4 textureColor = texture2D(uTexture, vTextureCoord.st);
    vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);
    float shadowFactor = calculateShadowFactor();

    vec3 ambient = uLightAmbient * textureColor.rgb;
    vec3 diffuse = max(dot(vVertexNormal, -correctedLightDirection), 0.0) * uLightDiffuse * textureColor.rgb;
    gl_FragColor = vec4(ambient + diffuse * shadowFactor, textureColor.a);
}

