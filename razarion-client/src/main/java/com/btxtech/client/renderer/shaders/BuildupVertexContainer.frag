precision mediump float;

varying vec3 vVertexNormal;
varying vec2 vTextureCoord;
varying float buildupZ;

uniform highp mat4 uNVMatrix;
uniform sampler2D uFinishTextureSampler;
uniform sampler2D uBuildupTextureSampler;
uniform vec3 uLightingAmbient;
uniform vec3 uLightingDiffuse;
uniform vec3 uLightingDirection;
uniform float progressZ;

// Shadow
varying vec4 vShadowCoord;
uniform float uShadowAlpha;
uniform sampler2D uShadowTexture;
uniform bool characterRepresenting;
uniform vec3 characterRepresentingColor;

float calculateShadowFactor() {
    float zMap = texture2D(uShadowTexture, vShadowCoord.st).r;

    if(zMap > vShadowCoord.z - 0.001) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}

void main(void) {
    vec4 textureColor;
    if(buildupZ > progressZ) {
        textureColor = texture2D(uBuildupTextureSampler, vTextureCoord.st);
        if(textureColor.a < 0.5) {
            discard;
        }
    } else {
         vec4 finishedColor = texture2D(uFinishTextureSampler, vTextureCoord.st);
         if(characterRepresenting) {
             textureColor = mix(vec4(characterRepresentingColor, 1.0), finishedColor, finishedColor.a);
         } else {
             textureColor = finishedColor;
             if(textureColor.a < 0.5) {
                 discard;
             }
         }
    }

    vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightingDirection, 1.0)).xyz);
    float shadowFactor = calculateShadowFactor();

    vec3 ambient = uLightingAmbient * textureColor.rgb;
    vec3 diffuse = max(dot(vVertexNormal, -correctedLightDirection), 0.0) * uLightingDiffuse * textureColor.rgb;
    gl_FragColor = vec4(ambient + diffuse * shadowFactor, 1.0);
}
