precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;

uniform highp mat4 uNMatrix;
uniform vec3 uLightingDirection;
uniform float diffuseWeightFactor;
uniform vec3 uAmbientColor;
uniform sampler2D uSamplerSlopeTexture;
uniform int uSamplerSlopeTextureSize;

// http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
vec4 triPlanarTextureMapping(sampler2D sampler, float size, vec2 addCoord) {
    vec3 blending = abs(vVertexNormCoord);
    blending = normalize(max(blending, 0.00001)); // Force weights to sum to 1.0
    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(sampler, vVertexPositionCoord.yz / size + addCoord);
    vec4 yAxisTop = texture2D(sampler, vVertexPositionCoord.xz / size + addCoord);
    vec4 zAxisTop = texture2D(sampler, vVertexPositionCoord.xy / size + addCoord);
    // blend the results of the 3 planar projections.
    return xAxisTop * blending.x + yAxisTop * blending.y + zAxisTop * blending.z;
}

vec3 bumpMapNorm(sampler2D sampler, float bumpMapDepth, float size) {
      vec3 normal = normalize(vVertexNormal);
      vec3 tangent = normalize(vVertexTangent);
      vec3 binormal = cross(normal, tangent);

      float bm0 = triPlanarTextureMapping(sampler, size, vec2(0, 0)).r;
      float bmUp = triPlanarTextureMapping(sampler, size, vec2(0.0, 1.0/size)).r;
      float bmRight = triPlanarTextureMapping(sampler, size, vec2(1.0/size, 0.0)).r;

      vec3 bumpVector = (bmRight - bm0) * tangent + (bmUp - bm0)*binormal;
      normal -= bumpMapDepth * bumpVector;
      return normalize(normal);
}

void main(void) {
    vec4 colorSlope = triPlanarTextureMapping(uSamplerSlopeTexture, float(uSamplerSlopeTextureSize), vec2(0,0));

    vec4 ambient = vec4(uAmbientColor, 1.0) * colorSlope;
    vec4 diffuse = vec4(max(dot(normalize(vVertexNormal), normalize(uLightingDirection)), 0.0) * diffuseWeightFactor * colorSlope.rgb, 1.0);
    gl_FragColor = ambient + diffuse;
}