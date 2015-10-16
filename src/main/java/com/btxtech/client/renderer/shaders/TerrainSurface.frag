#extension GL_OES_standard_derivatives : enable

precision mediump float;

varying vec2 vTextureCoord;
varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec4 vVertexPosition;
varying float vEdgePosition;

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
    textureColorBlend = smoothstep(vEdgePosition - uEdgeDistance, vEdgePosition + uEdgeDistance, textureColorBlend);
    vec4 textureColorBottom = texture2D(uSamplerBottom, vec2(vTextureCoord.s, vTextureCoord.t));
    vec4 texture = mix(textureColorTop, textureColorBottom, textureColorBlend);
    // TODO gl_FragColor = vec4(texture.rgb * vLightWeighting * shadowFactor, 1.0);
//////////////////////////

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
    gl_FragColor = vec4(color, 1.0);
    // gl_FragColor = textureColorBottom;




    // gl_FragColor = vec4(dPositiondx.xyz * 0.5 + 0.5, 1.0);
    // gl_FragColor = vec4(dDepthdx, 1.0);
   // gl_FragColor = vec4(vVertexNormal, 1.0);

    // gl_FragColor = vec4(depth, depth, depth, 1.0);


  // The normal is the cross product of the differentials
  // return normalize(cross(dPositiondx, dPositiondy));

 //   float lightWeight = max(dot(normalize(vVertexNormal), uLightingDirection), 0.0);
 //   gl_FragColor = vec4(normalize(vVertexNormal), 1.0);
}

