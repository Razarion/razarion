precision mediump float;

varying vec3 vVertexNormal;
varying vec2 vTextureCoord;
varying float height;

uniform highp mat4 uNVMatrix;
uniform sampler2D uSampler;
uniform sampler2D uCuttingSampler;
uniform sampler2D uLookUpSampler;
uniform vec3 uLightingAmbient;
uniform vec3 uLightingDiffuse;
uniform vec3 uLightingDirection;
uniform float uHealth;
uniform float uMaxHeight;

// Shadow
varying vec4 vShadowCoord;
uniform float uShadowAlpha;
uniform sampler2D uShadowTexture;

const float DEMOLITION_ZONE = 2.0;
const float GROUND_DEMOLITION_START = 0.2;

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
        vec4 cuttingColor = texture2D(uCuttingSampler, vTextureCoord.st);
        float referenceZ = uHealth * uMaxHeight;

        if(height > 0.0) {
            float current = height + (cuttingColor.r - 0.5) * 2.0;
            if(current > referenceZ) {
                discard;
            } else {
                if(current > referenceZ - DEMOLITION_ZONE) {
                    float scope = clamp((current - referenceZ + DEMOLITION_ZONE) / DEMOLITION_ZONE, 0.0, 1.0);
                    vec4 lookup = texture2D(uLookUpSampler, vec2(0.05, clamp(1.0 - scope, 0.1, 0.99)));
                    textureColor = mix(textureColor, lookup, scope);
                }
            }
        } else {
            if(uHealth < GROUND_DEMOLITION_START) {
                float scope = clamp(1.0 - (uHealth / GROUND_DEMOLITION_START), 0.0, 1.0);
                textureColor = mix(textureColor, vec4(0.0, 0.0, 0.0, 1.0), scope);
            }
        }
        vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightingDirection, 1.0)).xyz);
        float shadowFactor = calculateShadowFactor();

        vec3 ambient = uLightingAmbient * textureColor.rgb;
        vec3 diffuse = max(dot(vVertexNormal, -correctedLightDirection), 0.0) * uLightingDiffuse * textureColor.rgb;
        gl_FragColor = vec4(ambient + diffuse * shadowFactor, 1.0);
    }
}


