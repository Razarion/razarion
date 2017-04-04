com = {
    btxtech: {
        uiservice: {
            nativejs: {
                NativeMatrixFactory: function () {
                    this.createFromColumnMajorArray = function (array) {
                        return new com.btxtech.uiservice.nativejs.NativeMatrix(new Float32Array(array), this);
                    };

                    /**
                     * Column-major order
                     *
                     * @return array: C0R0, C0R1, C0R2 ... C3R3
                     */
                    this.createTranslation = function (x, y, z) {
                        var float32Array = new Float32Array(16);
                        // Col 1
                        float32Array[0] = 1;
                        float32Array[1] = 0;
                        float32Array[2] = 0;
                        float32Array[3] = 0;
                        // Col 2
                        float32Array[4] = 0;
                        float32Array[5] = 1;
                        float32Array[6] = 0;
                        float32Array[7] = 0;
                        // Col 3
                        float32Array[8] = 0;
                        float32Array[9] = 0;
                        float32Array[10] = 1;
                        float32Array[11] = 0;
                        // Col 4
                        float32Array[12] = x;
                        float32Array[13] = y;
                        float32Array[14] = z;
                        float32Array[15] = 1;

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(float32Array, this);
                    };

                    /**
                     * Column-major order
                     *
                     * @return array: C0R0, C0R1, C0R2 ... C3R3
                     */
                    this.createScale = function (x, y, z) {
                        var float32Array = new Float32Array(16);
                        // Col 1
                        float32Array[0] = x;
                        float32Array[1] = 0;
                        float32Array[2] = 0;
                        float32Array[3] = 0;
                        // Col 2
                        float32Array[4] = 0;
                        float32Array[5] = y;
                        float32Array[6] = 0;
                        float32Array[7] = 0;
                        // Col 3
                        float32Array[8] = 0;
                        float32Array[9] = 0;
                        float32Array[10] = z;
                        float32Array[11] = 0;
                        // Col 4
                        float32Array[12] = 0;
                        float32Array[13] = 0;
                        float32Array[14] = 0;
                        float32Array[15] = 1;

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(float32Array, this);
                    };

                    /**
                     * Column-major order
                     *
                     * @return array: C0R0, C0R1, C0R2 ... C3R3
                     */
                    this.createXRotation = function (rad) {
                        var s = Math.sin(rad);
                        var c = Math.cos(rad);
                        var float32Array = new Float32Array(16);

                        float32Array[0] = 1;
                        float32Array[1] = 0;
                        float32Array[2] = 0;
                        float32Array[3] = 0;
                        float32Array[4] = 0;
                        float32Array[5] = c;
                        float32Array[6] = s;
                        float32Array[7] = 0;
                        float32Array[8] = 0;
                        float32Array[9] = -s;
                        float32Array[10] = c;
                        float32Array[11] = 0;
                        float32Array[12] = 0;
                        float32Array[13] = 0;
                        float32Array[14] = 0;
                        float32Array[15] = 1;

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(float32Array, this);
                    };

                    /**
                     * Column-major order
                     *
                     * @return array: C0R0, C0R1, C0R2 ... C3R3
                     */
                    this.createYRotation = function (rad) {
                        var s = Math.sin(rad);
                        var c = Math.cos(rad);
                        var float32Array = new Float32Array(16);

                        float32Array[0] = c;
                        float32Array[1] = 0;
                        float32Array[2] = -s;
                        float32Array[3] = 0;
                        float32Array[4] = 0;
                        float32Array[5] = 1;
                        float32Array[6] = 0;
                        float32Array[7] = 0;
                        float32Array[8] = s;
                        float32Array[9] = 0;
                        float32Array[10] = c;
                        float32Array[11] = 0;
                        float32Array[12] = 0;
                        float32Array[13] = 0;
                        float32Array[14] = 0;
                        float32Array[15] = 1;

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(float32Array, this);
                    };

                    /**
                     * Column-major order
                     *
                     * @return array: C0R0, C0R1, C0R2 ... C3R3
                     */
                    this.createZRotation = function (rad) {
                        var s = Math.sin(rad);
                        var c = Math.cos(rad);
                        var float32Array = new Float32Array(16);

                        float32Array[0] = c;
                        float32Array[1] = s;
                        float32Array[2] = 0;
                        float32Array[3] = 0;
                        float32Array[4] = -s;
                        float32Array[5] = c;
                        float32Array[6] = 0;
                        float32Array[7] = 0;
                        float32Array[8] = 0;
                        float32Array[9] = 0;
                        float32Array[10] = 1;
                        float32Array[11] = 0;
                        float32Array[12] = 0;
                        float32Array[13] = 0;
                        float32Array[14] = 0;
                        float32Array[15] = 1;

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(float32Array, this);
                    };
                },


                NativeMatrix: function (float32Array, nativeMatrixFactory) {
                    this.float32Array = float32Array;
                    this.nativeMatrixFactory = nativeMatrixFactory;

                    this.multiply = function (other) {
                        var out = new Float32Array(16);

                        var a00 = this.float32Array[0], a01 = this.float32Array[1], a02 = this.float32Array[2], a03 = this.float32Array[3],
                            a10 = this.float32Array[4], a11 = this.float32Array[5], a12 = this.float32Array[6], a13 = this.float32Array[7],
                            a20 = this.float32Array[8], a21 = this.float32Array[9], a22 = this.float32Array[10], a23 = this.float32Array[11],
                            a30 = this.float32Array[12], a31 = this.float32Array[13], a32 = this.float32Array[14], a33 = this.float32Array[15];

                        // Cache only the current line of the second matrix
                        var b0 = other.float32Array[0], b1 = other.float32Array[1], b2 = other.float32Array[2], b3 = other.float32Array[3];
                        out[0] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
                        out[1] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
                        out[2] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
                        out[3] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

                        b0 = other.float32Array[4];
                        b1 = other.float32Array[5];
                        b2 = other.float32Array[6];
                        b3 = other.float32Array[7];
                        out[4] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
                        out[5] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
                        out[6] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
                        out[7] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

                        b0 = other.float32Array[8];
                        b1 = other.float32Array[9];
                        b2 = other.float32Array[10];
                        b3 = other.float32Array[11];
                        out[8] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
                        out[9] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
                        out[10] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
                        out[11] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

                        b0 = other.float32Array[12];
                        b1 = other.float32Array[13];
                        b2 = other.float32Array[14];
                        b3 = other.float32Array[15];
                        out[12] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
                        out[13] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
                        out[14] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
                        out[15] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(out, this.nativeMatrixFactory);
                    };

                    this.invert = function () {
                        var out = new Float32Array(16);

                        var a00 = this.float32Array[0], a01 = this.float32Array[1], a02 = this.float32Array[2], a03 = this.float32Array[3],
                            a10 = this.float32Array[4], a11 = this.float32Array[5], a12 = this.float32Array[6], a13 = this.float32Array[7],
                            a20 = this.float32Array[8], a21 = this.float32Array[9], a22 = this.float32Array[10], a23 = this.float32Array[11],
                            a30 = this.float32Array[12], a31 = this.float32Array[13], a32 = this.float32Array[14], a33 = this.float32Array[15],

                            b00 = a00 * a11 - a01 * a10,
                            b01 = a00 * a12 - a02 * a10,
                            b02 = a00 * a13 - a03 * a10,
                            b03 = a01 * a12 - a02 * a11,
                            b04 = a01 * a13 - a03 * a11,
                            b05 = a02 * a13 - a03 * a12,
                            b06 = a20 * a31 - a21 * a30,
                            b07 = a20 * a32 - a22 * a30,
                            b08 = a20 * a33 - a23 * a30,
                            b09 = a21 * a32 - a22 * a31,
                            b10 = a21 * a33 - a23 * a31,
                            b11 = a22 * a33 - a23 * a32,

                            // Calculate the determinant
                            det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;

                        if (!det) {
                            return null;
                        }
                        det = 1.0 / det;

                        out[0] = (a11 * b11 - a12 * b10 + a13 * b09) * det;
                        out[1] = (a02 * b10 - a01 * b11 - a03 * b09) * det;
                        out[2] = (a31 * b05 - a32 * b04 + a33 * b03) * det;
                        out[3] = (a22 * b04 - a21 * b05 - a23 * b03) * det;
                        out[4] = (a12 * b08 - a10 * b11 - a13 * b07) * det;
                        out[5] = (a00 * b11 - a02 * b08 + a03 * b07) * det;
                        out[6] = (a32 * b02 - a30 * b05 - a33 * b01) * det;
                        out[7] = (a20 * b05 - a22 * b02 + a23 * b01) * det;
                        out[8] = (a10 * b10 - a11 * b08 + a13 * b06) * det;
                        out[9] = (a01 * b08 - a00 * b10 - a03 * b06) * det;
                        out[10] = (a30 * b04 - a31 * b02 + a33 * b00) * det;
                        out[11] = (a21 * b02 - a20 * b04 - a23 * b00) * det;
                        out[12] = (a11 * b07 - a10 * b09 - a12 * b06) * det;
                        out[13] = (a00 * b09 - a01 * b07 + a02 * b06) * det;
                        out[14] = (a31 * b01 - a30 * b03 - a32 * b00) * det;
                        out[15] = (a20 * b03 - a21 * b01 + a22 * b00) * det;

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(out, this.nativeMatrixFactory);
                    };

                    this.transpose = function () {
                        var out = new Float32Array(16);

                        out[0] = this.float32Array[0];
                        out[1] = this.float32Array[4];
                        out[2] = this.float32Array[8];
                        out[3] = this.float32Array[12];
                        out[4] = this.float32Array[1];
                        out[5] = this.float32Array[5];
                        out[6] = this.float32Array[9];
                        out[7] = this.float32Array[13];
                        out[8] = this.float32Array[2];
                        out[9] = this.float32Array[6];
                        out[10] = this.float32Array[10];
                        out[11] = this.float32Array[14];
                        out[12] = this.float32Array[3];
                        out[13] = this.float32Array[7];
                        out[14] = this.float32Array[11];
                        out[15] = this.float32Array[15];

                        return new com.btxtech.uiservice.nativejs.NativeMatrix(out, this.nativeMatrixFactory);
                    };

                    this.toColumnMajorArray = function () {
                        var jsArray = [];
                        for (var i = 0; i < 16; i++) {
                            jsArray[i] = this.float32Array[i];
                        }
                        return jsArray;
                    };

                    this.getNativeMatrixFactory = function () {
                        return this.nativeMatrixFactory;
                    };

                    this.toString = function () {
                        var s = "NativeMatrix.float32Array: " + typeof this.float32Array + ": ";
                        this.float32Array.forEach(function (entry) {
                            s += entry + ", ";
                        });
                        return s;
                    }

                }
            }
        },
        shared: {
            nativejs: {
                TerrainTile: function () {
                    this.init = function (indexX, indexY) {
                        this.indexX = indexX;
                        this.indexY = indexY;
                    };

                    this.initGroundArrays = function (groundSizeVec, groundSizeScalar) {
                        this.groundVertices = new Float32Array(groundSizeVec);
                        this.groundNorms = new Float32Array(groundSizeVec);
                        this.groundTangents = new Float32Array(groundSizeVec);
                        this.groundSplattings = new Float32Array(groundSizeScalar);
                    };

                    this.setGroundTriangleCorner = function (triangleCornerIndex, vertexX, vertexY, vertexZ, normX, normY, normZ, tangentX, tangentY, tangentZ, splatting) {
                        var cornerScalarIndex = triangleCornerIndex * 3;
                        this.groundVertices[cornerScalarIndex] = vertexX;
                        this.groundVertices[cornerScalarIndex + 1] = vertexY;
                        this.groundVertices[cornerScalarIndex + 2] = vertexZ;
                        this.groundNorms[cornerScalarIndex] = normX;
                        this.groundNorms[cornerScalarIndex + 1] = normY;
                        this.groundNorms[cornerScalarIndex + 2] = normZ;
                        this.groundTangents[cornerScalarIndex] = tangentX;
                        this.groundTangents[cornerScalarIndex + 1] = tangentY;
                        this.groundTangents[cornerScalarIndex + 2] = tangentZ;
                        this.groundSplattings[triangleCornerIndex] = splatting;
                    };

                    this.getIndexX = function () {
                        return this.indexX;
                    };

                    this.getIndexY = function () {
                        return this.indexY;
                    };

                    this.getGroundVertices = function () {
                        return this.groundVertices;
                    };

                    this.getGroundNorms = function () {
                        return this.groundNorms;
                    };

                    this.getGroundTangents = function () {
                        return this.groundTangents;
                    };

                    this.getGroundSplattings = function () {
                        return this.groundSplattings;
                    };

                    this.setGroundVertexCount = function (groundVertexCount) {
                        this.groundVertexCount = groundVertexCount;
                    };

                    this.getGroundVertexCount = function () {
                        return this.groundVertexCount;
                    };

                    this.addTerrainSlopeTile = function (terrainSlopeTile) {
                        if (typeof this.terrainSlopeTiles == 'undefined') {
                            this.terrainSlopeTiles = [];
                        }
                        this.terrainSlopeTiles.push(terrainSlopeTile)
                    };

                    this.getTerrainSlopeTile = function () {
                        return this.terrainSlopeTiles;
                    };

                    this.toArray = function () {
                        var terrainSlopeTilesArray = [];
                        if (typeof this.terrainSlopeTiles != 'undefined') {
                            for (var i = 0; i < this.terrainSlopeTiles.length; i++) {
                                terrainSlopeTilesArray.push(this.terrainSlopeTiles[i].toArray());
                            }
                        }

                        return [this.indexX, this.indexY, this.groundVertexCount, this.groundVertices, this.groundNorms, this.groundTangents, this.groundSplattings, terrainSlopeTilesArray];
                    };

                    this.fromArray = function (array) {
                        this.indexX = array[0];
                        this.indexY = array[1];
                        this.groundVertexCount = array[2];
                        this.groundVertices = array[3];
                        this.groundNorms = array[4];
                        this.groundTangents = array[5];
                        this.groundSplattings = array[6];
                        var terrainSlopeTilesArray = array[7];
                        if (typeof terrainSlopeTilesArray != 'undefined') {
                            for (var i = 0; i < terrainSlopeTilesArray.length; i++) {
                                var terrainSlopeTile = new com.btxtech.shared.nativejs.TerrainSlopeTile();
                                terrainSlopeTile.fromArray(terrainSlopeTilesArray[i]);
                                this.addTerrainSlopeTile(terrainSlopeTile);
                            }
                        }
                    };
                },

                TerrainSlopeTile: function () {
                    this.init = function (slopeSkeletonConfigId, vertexSize, scalarSize) {
                        this.slopeSkeletonConfigId = slopeSkeletonConfigId;
                        this.vertices = new Float32Array(vertexSize);
                        this.norms = new Float32Array(vertexSize);
                        this.tangents = new Float32Array(vertexSize);
                        this.slopeFactors = new Float32Array(scalarSize);
                        this.groundSplattings = new Float32Array(scalarSize);
                    };

                    this.setTriangleCorner = function (triangleCornerIndex, vertexX, vertexY, vertexZ, normX, normY, normZ, tangentX, tangentY, tangentZ, slopeFactor, splatting) {
                        var cornerScalarIndex = triangleCornerIndex * 3;
                        this.vertices[cornerScalarIndex] = vertexX;
                        this.vertices[cornerScalarIndex + 1] = vertexY;
                        this.vertices[cornerScalarIndex + 2] = vertexZ;
                        this.norms[cornerScalarIndex] = normX;
                        this.norms[cornerScalarIndex + 1] = normY;
                        this.norms[cornerScalarIndex + 2] = normZ;
                        this.tangents[cornerScalarIndex] = tangentX;
                        this.tangents[cornerScalarIndex + 1] = tangentY;
                        this.tangents[cornerScalarIndex + 2] = tangentZ;
                        this.slopeFactors[triangleCornerIndex] = slopeFactor;
                        this.groundSplattings[triangleCornerIndex] = splatting;
                    };

                    this.getSlopeSkeletonConfigId = function () {
                        return this.slopeSkeletonConfigId;
                    };

                    this.setSlopeVertexCount = function (slopeVertexCount) {
                        this.slopeVertexCount = slopeVertexCount;
                    };

                    this.getSlopeVertexCount = function () {
                        return this.slopeVertexCount;
                    };

                    this.getVertices = function () {
                        return this.vertices;
                    };

                    this.getNorms = function () {
                        return this.norms;
                    };

                    this.getTangents = function () {
                        return this.tangents;
                    };

                    this.getSlopeFactors = function () {
                        return this.slopeFactors;
                    };

                    this.getGroundSplattings = function () {
                        return this.groundSplattings;
                    };

                    this.toArray = function () {
                        return [this.slopeSkeletonConfigId, this.slopeVertexCount, this.vertices, this.norms, this.tangents, this.slopeFactors, this.groundSplattings];
                    };

                    this.fromArray = function (array) {
                        this.slopeSkeletonConfigId = array[0];
                        this.slopeVertexCount = array[1];
                        this.vertices = array[2];
                        this.norms = array[3];
                        this.tangents = array[4];
                        this.slopeFactors = array[5];
                        this.groundSplattings = array[6];
                    };
                }
            }
        }
    }
};
