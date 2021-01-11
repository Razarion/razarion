
//-$$$-CHUNK uniforms-vertex BEGIN
uniform float uEdgeLength;
uniform float uWidthInPixels;
//-$$$-CHUNK uniforms-vertex END


//-$$$-CHUNK main-code-vertex BEGIN
vec4 vectorX1  = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
vec4 vectorX2  = projectionMatrix * viewMatrix * modelMatrix * vec4(position.x + uEdgeLength, position.y, position.z, 1.0);
gl_PointSize = (uWidthInPixels / 2.0) * (abs(vectorX1.x - vectorX2.x) / vectorX1.w);
//-$$$-CHUNK main-code-vertex END

// --------------------------------------------------------------------------

//-$$$-CHUNK uniforms-fragment BEGIN
uniform float uProgress;
uniform float uXColorRampOffset;
uniform float uTextureOffsetScope;
uniform float uShadowAlphaCutOff;
uniform sampler2D uAlphaOffsetSampler;
uniform sampler2D uColorRampSampler;
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
vec4 alphaOffset = texture2D(uAlphaOffsetSampler, vec2(gl_PointCoord.x, 1.0 - gl_PointCoord.y));
float greenOffset = uTextureOffsetScope * 2.0 * alphaOffset.g - uTextureOffsetScope;
float yOffset = clamp(uProgress + greenOffset, 0.001, 0.999);
vec4 colorRamp = texture2D(uColorRampSampler, vec2(uXColorRampOffset, yOffset));
float alpha = colorRamp.a * alphaOffset.r;
if(alpha < uShadowAlphaCutOff) {
    discard;
}
gl_FragColor = vec4(colorRamp.r, colorRamp.g, colorRamp.b, alpha);
//-$$$-CHUNK main-code-fragment END
