precision mediump float;

varying vec2 vTextureCoord;
varying float buildupZ;

uniform sampler2D uFinishTextureSampler;
uniform sampler2D uBuildupTextureSampler;
uniform float progressZ;

void main(void) {
    vec4 textureColor;
    if(buildupZ > progressZ) {
        textureColor = texture2D(uBuildupTextureSampler, vTextureCoord.st);
    } else {
       textureColor = texture2D(uFinishTextureSampler, vTextureCoord.st);
    }
    if(textureColor.a < 0.5) {
       discard;
    }

    gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
}
