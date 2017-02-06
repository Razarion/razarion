precision mediump float;

varying vec2 vAlphaTextureCoordinate;

uniform float uProgress;
uniform sampler2D uAlphaOffsetSampler;
uniform sampler2D uColorRampSampler;

const float LOOKUP_OFFSET = 0.1;

void main(void) {
    // vec4 colorRamp = texture2D(uColorRampSampler, vec2(0.05, yOffset));
    // vec4 colorRamp = texture2D(uColorRampSampler, vec2(0.22, yOffset));
    // vec4 colorRamp = texture2D(uColorRampSampler, vec2(0.35, yOffset));
    vec4 uAlphaOffset = texture2D(uAlphaOffsetSampler, vAlphaTextureCoordinate);
    float greenOffset = LOOKUP_OFFSET * 2.0 * uAlphaOffset.g - LOOKUP_OFFSET;
    float yOffset = clamp(uProgress + greenOffset, 0.001, 0.999);
    vec4 colorRamp = texture2D(uColorRampSampler, vec2(0.57, yOffset));
    gl_FragColor = vec4(colorRamp.r, colorRamp.g, colorRamp.b, colorRamp.a * uAlphaOffset.r);
}
