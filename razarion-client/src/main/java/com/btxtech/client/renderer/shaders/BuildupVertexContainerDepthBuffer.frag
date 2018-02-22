precision mediump float;

varying vec2 vTextureCoord;
varying float buildupZ;

uniform sampler2D uFinishTextureSampler;
uniform sampler2D uBuildupTextureSampler;
uniform float progressZ;
uniform bool characterRepresenting;

void main(void) {
    vec4 textureColor;
    if(buildupZ > progressZ) {
        textureColor = texture2D(uBuildupTextureSampler, vTextureCoord.st);
        if(textureColor.a < 0.5) {
            discard;
        }
   } else {
        textureColor = texture2D(uFinishTextureSampler, vTextureCoord.st);
        if(!characterRepresenting && textureColor.a < 0.5) {
            discard;
        }
   }

    gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
}
