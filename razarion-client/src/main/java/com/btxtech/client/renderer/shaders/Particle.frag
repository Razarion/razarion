precision mediump float;

varying vec2 vAlphaTextureCoordinate;

uniform float uProgress;
uniform sampler2D uAlphaTextureSampler;
uniform sampler2D uColorRampSampler;

void main(void) {
    float yOffset = clamp(uProgress, 0.001, 0.999);
    vec4 colorRamp = texture2D(uColorRampSampler, vec2(0.05, yOffset));
    vec4 alphaTexture = texture2D(uAlphaTextureSampler, vAlphaTextureCoordinate);
    gl_FragColor = vec4(colorRamp.r, colorRamp.g, colorRamp.b, colorRamp.a * alphaTexture.a);
}
