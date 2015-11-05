#extension GL_OES_standard_derivatives : enable

precision mediump float;

varying vec2 vTextureCoord;
varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec4 vVertexPosition;
varying float vEdgePosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;

uniform sampler2D uSamplerGround;
uniform sampler2D uSamplerSlope;
uniform sampler2D uSamplerBottom;
uniform sampler2D uSamplerShadow;
uniform sampler2D uSamplerSlopePumpMap;
uniform float uEdgeDistance;
uniform float uShadowAlpha;
uniform vec3 uLightingDirection;
uniform vec3 uDirectionalColor;
uniform vec3 uAmbientColor;
uniform highp mat4 uNMatrix;
uniform float bumpMapDepth;

//vec3 getNumpMapNormal() {
//  // Differentiate the position vector
//  vec3 dPositiondx = dFdx(vVertexPosition);
//  vec3 dPositiondy = dFdy(vVertexPosition);
//  float depth = texture2D(uSamplerBottom, vTextureCoord.st).a;
//  float dDepthdx = dFdx(depth);
//  float dDepthdy = dFdy(depth);
//  dPositiondx -= 10.0 * dDepthdx * vVertexNormal;
//  dPositiondy -= 10.0 * dDepthdy * vVertexNormal;
//
//  // The normal is the cross product of the differentials
//  return normalize(cross(dPositiondx, dPositiondy));
//}

vec4 triPlanarTextureMapping(sampler2D sampler, float scale) {
    vec3 blending = abs(vVertexNormCoord);
    blending = normalize(max(blending, 0.00001)); // Force weights to sum to 1.0
    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(sampler, vVertexPositionCoord.yz / scale);
    vec4 yAxisTop = texture2D(sampler, vVertexPositionCoord.xz / scale);
    vec4 zAxisTop = texture2D(sampler, vVertexPositionCoord.xy / scale);
    // blend the results of the 3 planar projections.
    return xAxisTop * blending.x + yAxisTop * blending.y + zAxisTop * blending.z;
}

vec3 bumpMapNorm(sampler2D sampler, float scale) {
    vec3 dPositiondx = dFdx(vVertexPosition.xyz);
    vec3 dPositiondy = dFdy(vVertexPosition.xyz);
    float depth = triPlanarTextureMapping(sampler, scale).r;
    float dDepthdx = dFdx(depth);
    float dDepthdy = dFdy(depth);
    dPositiondx -= bumpMapDepth * dDepthdx * vVertexNormal;
    dPositiondy -= bumpMapDepth * dDepthdy * vVertexNormal;
    return normalize(cross(dPositiondx, dPositiondy));
}

void main(void) {
    // Shadow
    float zNdc = vShadowCoord.z / vShadowCoord.w;
    mat4 coordCorrectionMatrix = mat4(0.5, 0.0, 0.0, 0.0,
                                 0.0, 0.5, 0.0, 0.0,
                                 0.0, 0.0, 0.5, 0.0,
                                 0.5, 0.5, 0.5, 1.0);
    vec4 coordShadowMap = coordCorrectionMatrix * vShadowCoord;
    float zMap = texture2D(uSamplerShadow, coordShadowMap.st / coordShadowMap.w).r;
    float shadowFactor;
    zNdc = zNdc * 0.5 + 0.5;
    if(zMap > zNdc - 0.01) {
        shadowFactor = 1.0;
    } else {
        shadowFactor = uShadowAlpha;
    }

    // Surface
    vec4 textureColorTop = texture2D(uSamplerGround, vec2(vTextureCoord.s, vTextureCoord.t));
    vec4 textureColorBlend = texture2D(uSamplerSlope, vec2(vTextureCoord.s, vTextureCoord.t));
    float tmUEdgeDistance = uEdgeDistance;
    // textureColorBlend = smoothstep(vEdgePosition - uEdgeDistance, vEdgePosition + uEdgeDistance, textureColorBlend);
    vec4 textureColorBottom = texture2D(uSamplerBottom, vec2(vTextureCoord.s, vTextureCoord.t));
    vec4 texture = mix(textureColorTop, textureColorBottom, textureColorBlend);
    // TODO gl_FragColor = vec4(texture.rgb * vLightWeighting * shadowFactor, 1.0);
//////////////////////////
    vec3 correctedLigtDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;

    // float directionalLightWeighting = max(dot(bumpVertexNormal, correctedLigtDirection), 0.0);
    // gl_FragColor = vec4(vec3(directionalLightWeighting), 1.0);
    // gl_FragColor = vec4(dDepthdx, dDepthdx, dDepthdx, 1.0);
    // vec3 color = textureColorTop.rgb * directionalLightWeighting;
    // gl_FragColor = vec4(color, 1.0);
    // gl_FragColor = textureColorBottom;

    // Tri-Planar Texture Mapping
    // http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
    vec4 colorGround = triPlanarTextureMapping(uSamplerGround, 512.0);
    vec4 colorSlope = triPlanarTextureMapping(uSamplerSlope, 512.0);


    vec3 correctedZenith = (uNMatrix * vec4(vec3(0,0,1), 1.0)).xyz;
    vec4 textureColor;
    vec3 correctedNorm;
    float slope = dot(vVertexNormal.xyz, correctedZenith);
    if(slope >= 0.8) {
      textureColor = colorGround;
      correctedNorm = vVertexNormal;
      // gl_FragColor = vec4(0.0, 0.7, 0.0, 1.0);
    } else if(slope < 0.8 && slope > 0.4) {
      textureColor = colorSlope;
      correctedNorm = bumpMapNorm(uSamplerSlopePumpMap, 512.0);
      // gl_FragColor = vec4(0.1, 0.1, 0.1, 1.0);
      // gl_FragColor = colorTop;
   } else {
      textureColor = colorSlope;
      correctedNorm = bumpMapNorm(uSamplerSlopePumpMap, 512.0);
      // gl_FragColor = vec4(0.2, 0.2, 0.2, 1.0);
   }


    // Diffuse light
    vec4 diffuseFactor = vec4(max(dot(normalize(correctedNorm), normalize(correctedLigtDirection)), 0.0) * shadowFactor * uDirectionalColor , 1.0);
    // vec3 uDirectionalColor_ = uDirectionalColor;
    // vec4 diffuseFactor = vec4(vec3(max(dot(correctedLigtDirection, correctedNorm), 0.0)), 1.0);
    vec4 ambientDiffuseFactor = diffuseFactor + vec4(uAmbientColor, 1.0);
    gl_FragColor = textureColor * ambientDiffuseFactor;
    // gl_FragColor = vec4(vec3(correctedNorm * 0.5 + 0.5), 1.0);


    // float depth = triPlanarTextureMapping(sampler, scale).r;
    // gl_FragColor = vec4(vec3(correctedNorm * 0.5 + 0.5), 1.0);


    // gl_FragColor = vec4(correctedNorm * 0.5 + 0.5, 1.0);
    // gl_FragColor = vec4(shadowFactor * phongShading.rgb, phongShading.a);

    // gl_FragColor = vec4(vVertexNormCoord * 0.5 + 0.5, 1.0);

   //gl_FragColor =  texture2D(uSamplerTop, vVertexPositionCoord.xz / 512.0);
    // gl_FragColor = vec4(slope, slope, slope, 1.0);


    // gl_FragColor = vec4(vVertexPositionCoord.xyz * 0.001, 1.0);
    // gl_FragColor = vec4(dDepthdx, 1.0);
   // gl_FragColor = vec4(vVertexNormal, 1.0);

    // gl_FragColor = vec4(depth, depth, depth, 1.0);


  // The normal is the cross product of the differentials
  // return normalize(cross(dPositiondx, dPositiondy));
}

