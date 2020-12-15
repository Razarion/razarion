//-$$$-CHUNK varyings BEGIN
varying vec2 vAlphaTextureCoordinate;
//-$$$-CHUNK varyings END

// --------------------------------------------------------------------------

//-$$$-CHUNK attributes BEGIN
attribute vec2 aAlphaTextureCoordinate;
//-$$$-CHUNK attributes END

//-$$$-CHUNK main-code-vertex BEGIN
vAlphaTextureCoordinate = aAlphaTextureCoordinate;
//-$$$-CHUNK main-code-vertex END

// --------------------------------------------------------------------------

//-$$$-CHUNK uniforms-fragment BEGIN
uniform float uProgress;
uniform float uXColorRampOffset;
uniform float uTextureOffsetScope;
uniform sampler2D uAlphaOffsetSampler;
uniform sampler2D uColorRampSampler;
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
vec4 alphaOffset = texture2D(uAlphaOffsetSampler, vAlphaTextureCoordinate);
float greenOffset = uTextureOffsetScope * 2.0 * alphaOffset.g - uTextureOffsetScope;
float yOffset = clamp(uProgress + greenOffset, 0.001, 0.999);
vec4 colorRamp = texture2D(uColorRampSampler, vec2(uXColorRampOffset, yOffset));
gl_FragColor = vec4(colorRamp.r, colorRamp.g, colorRamp.b, colorRamp.a * alphaOffset.r);
//-$$$-CHUNK main-code-fragment END
