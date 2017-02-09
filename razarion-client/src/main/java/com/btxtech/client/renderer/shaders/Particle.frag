precision mediump float;

varying vec2 vAlphaTextureCoordinate;

uniform float uProgress;
uniform float uXColorRampOffset;
uniform float uTextureOffsetScope;
uniform sampler2D uAlphaOffsetSampler;
uniform sampler2D uColorRampSampler;

void main(void) {
    vec4 alphaOffset = texture2D(uAlphaOffsetSampler, vAlphaTextureCoordinate);
    float greenOffset = uTextureOffsetScope * 2.0 * alphaOffset.g - uTextureOffsetScope;
    float yOffset = clamp(uProgress + greenOffset, 0.001, 0.999);
    vec4 colorRamp = texture2D(uColorRampSampler, vec2(uXColorRampOffset, yOffset));
    gl_FragColor = vec4(colorRamp.r, colorRamp.g, colorRamp.b, colorRamp.a * alphaOffset.r);
    // gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
