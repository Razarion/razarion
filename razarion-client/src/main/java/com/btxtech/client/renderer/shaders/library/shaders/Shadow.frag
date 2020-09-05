//-$$$-CHUNK uniforms-and-code BEGIN
varying vec4 shadowPosition;
uniform float uShadowAlpha;
uniform sampler2D uDepthTexture;

float shadowFactor;

float calculateShadowFactor() {
    float zMap = texture2D(uDepthTexture, shadowPosition.xy).r;

    if (zMap > shadowPosition.z - 0.01) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}
//-$$$-CHUNK uniforms-and-code END

//-$$$-CHUNK chunk BEGIN
shadowFactor = calculateShadowFactor();
//-$$$-CHUNK chunk END

