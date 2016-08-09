precision mediump float;

varying vec3 vVertexNormal;
varying vec2 vTextureCoord;

uniform highp mat4 uNVMatrix;
uniform sampler2D uSampler;
uniform vec3 uLightingAmbient;
uniform vec3 uLightingDiffuse;
uniform vec3 uLightingDirection;

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
    vec4 textureColor = texture2D(uSampler, vTextureCoord.st);
    if(textureColor.a < 0.5) {
        discard;
    } else {
        vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightingDirection, 1.0)).xyz);
        float shadowFactor = calculateShadowFactor();

        vec3 ambient = uLightingAmbient * textureColor.rgb;
        vec3 diffuse = max(dot(vVertexNormal, -correctedLightDirection), 0.0) * uLightingDiffuse * textureColor.rgb;
        gl_FragColor = vec4(ambient + diffuse * shadowFactor, 1.0);
    }
}
