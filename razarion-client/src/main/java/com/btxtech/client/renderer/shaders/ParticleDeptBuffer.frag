precision mediump float;

varying vec2 vAlphaTextureCoordinate;

uniform float uProgress;
uniform float uXColorRampOffset;
uniform sampler2D uAlphaOffsetSampler;
uniform sampler2D uColorRampSampler;


void main(void) {
    vec4 alphaOffset = texture2D(uAlphaOffsetSampler, vAlphaTextureCoordinate);
    vec4 colorRamp = texture2D(uColorRampSampler, vec2(uXColorRampOffset, uProgress));

    if(colorRamp.a * alphaOffset.r > 0.1) {
        gl_FragColor = vec4(0.2, 0.2, 0.2, 1.0);
    } else {
        discard;
    }
}
