#extension GL_OES_standard_derivatives : enable

precision mediump float;

varying vec2 vTextureCoord;
varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec4 vVertexPosition;
varying float vEdgePosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;

uniform sampler2D uSamplerTop;
uniform sampler2D uSamplerBlend;
uniform sampler2D uSamplerBottom;
uniform sampler2D uSamplerShadow;
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
    if(zMap > zNdc - 0.001) {
        shadowFactor = 1.0;
    } else {
        shadowFactor = uShadowAlpha;
    }

    vec3 _uAmbientColor_ = uAmbientColor;
    vec3 _uDirectionalColor_ = uDirectionalColor;

    // Surface
    vec4 textureColorTop = texture2D(uSamplerTop, vec2(vTextureCoord.s, vTextureCoord.t));
    vec4 textureColorBlend = texture2D(uSamplerBlend, vec2(vTextureCoord.s, vTextureCoord.t));
    float tmUEdgeDistance = uEdgeDistance;
    // textureColorBlend = smoothstep(vEdgePosition - uEdgeDistance, vEdgePosition + uEdgeDistance, textureColorBlend);
    vec4 textureColorBottom = texture2D(uSamplerBottom, vec2(vTextureCoord.s, vTextureCoord.t));
    vec4 texture = mix(textureColorTop, textureColorBottom, textureColorBlend);
    // TODO gl_FragColor = vec4(texture.rgb * vLightWeighting * shadowFactor, 1.0);
//////////////////////////

    // Bump map
    // Differentiate the position vector
    vec3 dPositiondx = dFdx(vVertexPosition.xyz);
    vec3 dPositiondy = dFdy(vVertexPosition.xyz);
    float depth = texture2D(uSamplerBottom, vTextureCoord.st).b;
    float dDepthdx = dFdx(depth);
    float dDepthdy = dFdy(depth);
    dPositiondx -= bumpMapDepth * dDepthdx * vVertexNormal;
    dPositiondy -= bumpMapDepth * dDepthdy * vVertexNormal;
    vec3 bumpVertexNormal = normalize(cross(dPositiondx, dPositiondy));

    vec3 correctedLigtDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;

    float directionalLightWeighting = max(dot(bumpVertexNormal, correctedLigtDirection), 0.0);
    // gl_FragColor = vec4(vec3(directionalLightWeighting), 1.0);
    // gl_FragColor = vec4(dDepthdx, dDepthdx, dDepthdx, 1.0);
    vec3 color = textureColorTop.rgb * directionalLightWeighting;
    // gl_FragColor = vec4(color, 1.0);
    // gl_FragColor = textureColorBottom;

    // Tri-Planar Texture Mapping
    // http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
    vec3 blending = abs(vVertexNormCoord);
    blending = normalize(max(blending, 0.00001)); // Force weights to sum to 1.0
    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(uSamplerTop, vVertexPositionCoord.yz / 512.0);
    vec4 yAxisTop = texture2D(uSamplerTop, vVertexPositionCoord.xz / 512.0);
    vec4 zAxisTop = texture2D(uSamplerTop, vVertexPositionCoord.xy / 512.0);
    // blend the results of the 3 planar projections.
    vec4 colorTop = xAxisTop * blending.x + yAxisTop * blending.y + zAxisTop * blending.z;

    vec4 xAxisBottom = texture2D(uSamplerBottom, vVertexPositionCoord.yz / 512.0);
    vec4 yAxisBottom = texture2D(uSamplerBottom, vVertexPositionCoord.xz / 512.0);
    vec4 zAxisBottom = texture2D(uSamplerBottom, vVertexPositionCoord.xy / 512.0);
    vec4 colorBottom = xAxisBottom * blending.x + yAxisBottom * blending.y + zAxisBottom * blending.z;

    vec4 xAxisPassage = texture2D(uSamplerBlend, vVertexPositionCoord.yz / 512.0);
    vec4 yAxisPassage = texture2D(uSamplerBlend, vVertexPositionCoord.xz / 512.0);
    vec4 zAxisPassage = texture2D(uSamplerBlend, vVertexPositionCoord.xy / 512.0);
    vec4 colorPassage = xAxisPassage * blending.x + yAxisPassage * blending.y + zAxisPassage * blending.z;


    vec3 correctedZenith = (uNMatrix * vec4(vec3(0,0,1), 1.0)).xyz;
    vec4 textureColor;
    float slope = dot(vVertexNormal.xyz, correctedZenith);
    if(slope >= 0.8) {
      textureColor = colorTop;
      // gl_FragColor = vec4(0.0, 0.7, 0.0, 1.0);
    } else if(slope < 0.8 && slope > 0.4) {
      textureColor = colorPassage;
      // gl_FragColor = vec4(0.1, 0.1, 0.1, 1.0);
      // gl_FragColor = colorTop;
   } else {
      textureColor = colorBottom;
      // gl_FragColor = vec4(0.2, 0.2, 0.2, 1.0);
   }


    // Diffuse light
    float diffuselightWeight = max(dot(normalize(vVertexNormal), correctedLigtDirection), 0.0);
    vec4 ambientDiffuseFactor = vec4(uDirectionalColor * diffuselightWeight + uAmbientColor, 1.0);
    gl_FragColor = textureColor * ambientDiffuseFactor, 1.0;

    // gl_FragColor = vec4(vVertexNormCoord * 0.5 + 0.5, 1.0);

   //gl_FragColor =  texture2D(uSamplerTop, vVertexPositionCoord.xz / 512.0);
    // gl_FragColor = vec4(slope, slope, slope, 1.0);


    // gl_FragColor = vec4(vVertexPositionCoord.xyz * 0.001, 1.0);
    // gl_FragColor = vec4(dDepthdx, 1.0);
   // gl_FragColor = vec4(vVertexNormal, 1.0);

    // gl_FragColor = vec4(depth, depth, depth, 1.0);


  // The normal is the cross product of the differentials
  // return normalize(cross(dPositiondx, dPositiondy));

 //   gl_FragColor = vec4(normalize(vVertexNormal), 1.0);
}

