//-$$$-CHUNK varyings BEGIN
#ifdef BUILDUP_STATE
varying float buildupZ;
#endif
//-$$$-CHUNK varyings END

//----------------------------------------------------------------------

//-$$$-CHUNK uniforms-vertex BEGIN
#ifdef BUILDUP_STATE
uniform highp mat4 buildupMatrix;
#endif
//-$$$-CHUNK uniforms-vertex END

//-$$$-CHUNK main-code-vertex BEGIN
#ifdef BUILDUP_STATE
buildupZ = (buildupMatrix * vec4(position, 1.0)).z;
#endif
//-$$$-CHUNK main-code-vertex END

//----------------------------------------------------------------------

//-$$$-CHUNK uniforms-fragment BEGIN
uniform PhongMaterial material;
#ifdef  ALPHA_TO_COVERAGE
uniform float alphaToCoverage;
#endif
#ifdef BUILDUP_STATE
uniform float progressZ;
uniform sampler2D uBuildupTextureSampler;
#endif
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
    #ifdef BUILDUP_STATE
    if(buildupZ > progressZ) {
        gl_FragColor = texture2D(uBuildupTextureSampler, vUv);
        return;
    }
    #endif

    vec4 rgba = phongAlpha(material, vUv);
    float sharpenAlpha = 1.0;
    #ifdef ALPHA_TO_COVERAGE
    sharpenAlpha = (rgba.a - alphaToCoverage) / max(fwidth(rgba.a), 0.0001) + 0.5;
    #endif
    gl_FragColor = vec4(rgba.rgb, sharpenAlpha);
//-$$$-CHUNK main-code-fragment END
