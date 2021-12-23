//-$$$-CHUNK attributes BEGIN
#ifdef TWO_PHONGS
attribute float vertexColorAttribute;
#endif
//-$$$-CHUNK attributes END

//-$$$-CHUNK varyings BEGIN
#ifdef BUILDUP_STATE
varying float buildupZ;
#endif
#ifdef TWO_PHONGS
varying float vertexColorVarying;
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
#ifdef TWO_PHONGS
vertexColorVarying = vertexColorAttribute;
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
#ifdef HEALTH_STATE
uniform sampler2D uDemolitionSampler;
uniform float uHealth;
const float DELTA_HEALTH = 0.75;
#endif
#ifdef CHARACTER_REPRESENTING
uniform vec3 characterRepresentingColor;
#endif
#ifdef TWO_PHONGS
uniform PhongMaterial material2;
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
    #ifdef TWO_PHONGS
    rgba = mix(phongAlpha(material2, vUv), rgba, vertexColorVarying);
    #endif

    #ifdef HEALTH_STATE
    vec4 cuttingColor = texture2D(uDemolitionSampler, vUv);
    float healthFactor = uHealth * (DELTA_HEALTH -1.0) + 1.0 - DELTA_HEALTH;
    float demoltionTextureFactor = cuttingColor.r * -2.0 + 1.0;
    float burned = clamp(healthFactor + demoltionTextureFactor, 0.0, 1.0);
    rgba = clamp(rgba - burned, 0.0, 1.0);
    #endif

    float sharpenAlpha = 1.0;
    #ifdef ALPHA_TO_COVERAGE
    sharpenAlpha = (rgba.a - alphaToCoverage) / max(fwidth(rgba.a), 0.0001) + 0.5;
    #endif

    vec3 rgb = rgba.rgb;
    #ifdef CHARACTER_REPRESENTING
    rgb = mix(characterRepresentingColor, rgba.rgb, rgba.a);
    #endif

    gl_FragColor = vec4(rgb, sharpenAlpha);
//-$$$-CHUNK main-code-fragment END
