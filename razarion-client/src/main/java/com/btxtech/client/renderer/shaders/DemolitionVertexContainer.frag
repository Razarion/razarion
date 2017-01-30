precision mediump float;

varying vec3 vVertexNormal;
varying vec2 vTextureCoord;

uniform highp mat4 uNVMatrix;
uniform sampler2D uSampler;
uniform sampler2D uDemolitionSampler;
uniform vec3 uLightingAmbient;
uniform vec3 uLightingDiffuse;
uniform vec3 uLightingDirection;
uniform float uHealth;

// Shadow
varying vec4 vShadowCoord;
uniform float uShadowAlpha;
uniform sampler2D uShadowTexture;

const float DELTA_HEALTH = 0.75;

float calculateShadowFactor() {
    float zMap = texture2D(uShadowTexture, vShadowCoord.st).r;

    if(zMap > vShadowCoord.z - 0.001) {
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
        vec4 cuttingColor = texture2D(uDemolitionSampler, vTextureCoord.st);
        float healthFactor = uHealth * (DELTA_HEALTH -1.0) + 1.0 - DELTA_HEALTH;
        float demoltionTextureFactor = cuttingColor.r * -2.0 + 1.0;
        float burned = clamp(healthFactor + demoltionTextureFactor, 0.0, 1.0);
        textureColor = clamp(textureColor - burned, 0.0, 1.0);

        vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightingDirection, 1.0)).xyz);
        float shadowFactor = calculateShadowFactor();

        vec3 ambient = uLightingAmbient * textureColor.rgb;
        vec3 diffuse = max(dot(vVertexNormal, -correctedLightDirection), 0.0) * uLightingDiffuse * textureColor.rgb;
        gl_FragColor = vec4(ambient + diffuse * shadowFactor, 1.0);
    }
}


