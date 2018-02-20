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
uniform bool characterRepresenting;
uniform vec3 characterRepresentingColor;

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
    if(!characterRepresenting && textureColor.a < 0.5) {
        discard;
    } else {
        vec3 color;
        if(characterRepresenting) {
            color = mix(characterRepresentingColor, textureColor.rgb, textureColor.a);
        } else {
            color = textureColor.rgb;
        }

        vec4 cuttingColor = texture2D(uDemolitionSampler, vTextureCoord.st);
        float healthFactor = uHealth * (DELTA_HEALTH -1.0) + 1.0 - DELTA_HEALTH;
        float demoltionTextureFactor = cuttingColor.r * -2.0 + 1.0;
        float burned = clamp(healthFactor + demoltionTextureFactor, 0.0, 1.0);
        color = clamp(color - burned, 0.0, 1.0);

        vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightingDirection, 1.0)).xyz);
        float shadowFactor = calculateShadowFactor();

        vec3 ambient = uLightingAmbient * color;
        vec3 diffuse = max(dot(vVertexNormal, -correctedLightDirection), 0.0) * uLightingDiffuse * color;
        gl_FragColor = vec4(ambient + diffuse * shadowFactor, 1.0);
    }
}


