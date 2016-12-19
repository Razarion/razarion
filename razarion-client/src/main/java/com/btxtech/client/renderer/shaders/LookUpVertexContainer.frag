precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D uSampler;
uniform sampler2D uLookUpSampler;
uniform float progress;

void main(void) {
    vec4 textureColor = texture2D(uSampler, vTextureCoord.st);
    float textureVal = textureColor.r - 0.5;
    float yOffset = clamp(textureVal + progress, 0.01, 0.99);
    vec4 color = texture2D(uLookUpSampler, vec2(0.05, yOffset));

    gl_FragColor = vec4(color.rgb, color.a * textureColor.a);
}
