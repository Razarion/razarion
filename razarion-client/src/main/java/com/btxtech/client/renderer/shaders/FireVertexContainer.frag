precision mediump float;

varying vec2 vTextureCoord;
varying float height;

uniform sampler2D uTexture;
uniform sampler2D uLookUpTexture;
uniform float yTextureOffset;

const float FADEOUT_FACTOR = 0.3;

void main(void) {
    vec4 textureColor = texture2D(uTexture, vec2(vTextureCoord.s, vTextureCoord.t - yTextureOffset));
    float yOffset = clamp((textureColor.r + height) / 2.0, 0.01, 0.99);
    vec4 color = texture2D(uLookUpTexture, vec2(0.05, yOffset));

    float fadeOut = 1.0;
    if(height > FADEOUT_FACTOR) {
        fadeOut = height / (FADEOUT_FACTOR - 1.0) + 1.0 / (1.0 - FADEOUT_FACTOR);
    }

    gl_FragColor = vec4(color.rgb, color.a * textureColor.a * fadeOut);
}
