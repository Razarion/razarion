com = {
    btxtech: {
        shared: {
            nativejs: {
                NativeMatrixFactory: function () {
                    this.createFromColumnMajorArray = function (array) {
                        return new com.btxtech.shared.nativejs.NativeMatrix(new Float32Array(array), this);
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

                        return new com.btxtech.shared.nativejs.NativeMatrix(float32Array, this);
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

                        return new com.btxtech.shared.nativejs.NativeMatrix(float32Array, this);
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

                        return new com.btxtech.shared.nativejs.NativeMatrix(float32Array, this);
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

                        return new com.btxtech.shared.nativejs.NativeMatrix(float32Array, this);
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

                        return new com.btxtech.shared.nativejs.NativeMatrix(float32Array, this);
                    };

                    this.createFromNativeMatrixDto = function (nativeMatrixDto) {
                        return new com.btxtech.shared.nativejs.NativeMatrix(nativeMatrixDto.numbers, this);
                    };

                    this.createNativeMatrixDtoColumnMajorArray = function (array) {
                        return new com.btxtech.shared.nativejs.NativeMatrixDto(new Float32Array(array), this);
                    };

                    this.intArrayConverter = function (array) {
                        var result = [];
                        for (i = 0; i < array.length; i++) {
                            result[i] = array[i];
                        }
                        return result;
                    };
                },

                NativeMatrix: function (float32Array, nativeMatrixFactory) {
                    this.float32Array = float32Array;
                    this.nativeMatrixFactory = nativeMatrixFactory;

                    this.multiply = function (other) {
                        var out = new Float32Array(16);

                        var a00 = this.float32Array[0], a01 = this.float32Array[1], a02 = this.float32Array[2],
                            a03 = this.float32Array[3],
                            a10 = this.float32Array[4], a11 = this.float32Array[5], a12 = this.float32Array[6],
                            a13 = this.float32Array[7],
                            a20 = this.float32Array[8], a21 = this.float32Array[9], a22 = this.float32Array[10],
                            a23 = this.float32Array[11],
                            a30 = this.float32Array[12], a31 = this.float32Array[13], a32 = this.float32Array[14],
                            a33 = this.float32Array[15];

                        // Cache only the current line of the second matrix
                        var b0 = other.float32Array[0], b1 = other.float32Array[1], b2 = other.float32Array[2],
                            b3 = other.float32Array[3];
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

                        return new com.btxtech.shared.nativejs.NativeMatrix(out, this.nativeMatrixFactory);
                    };

                    this.multiplyVertex = function (nativeVertexDto, w) {
                        var resultNativeVertexDto = new com.btxtech.shared.nativejs.NativeVertexDto();
                        resultNativeVertexDto.x = this.float32Array[0] * nativeVertexDto.x + this.float32Array[4] * nativeVertexDto.y + this.float32Array[8] * nativeVertexDto.z + this.float32Array[12] * w;
                        resultNativeVertexDto.y = this.float32Array[1] * nativeVertexDto.x + this.float32Array[5] * nativeVertexDto.y + this.float32Array[9] * nativeVertexDto.z + this.float32Array[13] * w;
                        resultNativeVertexDto.z = this.float32Array[2] * nativeVertexDto.x + this.float32Array[6] * nativeVertexDto.y + this.float32Array[10] * nativeVertexDto.z + this.float32Array[14] * w;
                        return resultNativeVertexDto;
                    };

                    this.invert = function () {
                        var out = new Float32Array(16);

                        var a00 = this.float32Array[0], a01 = this.float32Array[1], a02 = this.float32Array[2],
                            a03 = this.float32Array[3],
                            a10 = this.float32Array[4], a11 = this.float32Array[5], a12 = this.float32Array[6],
                            a13 = this.float32Array[7],
                            a20 = this.float32Array[8], a21 = this.float32Array[9], a22 = this.float32Array[10],
                            a23 = this.float32Array[11],
                            a30 = this.float32Array[12], a31 = this.float32Array[13], a32 = this.float32Array[14],
                            a33 = this.float32Array[15],

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

                        return new com.btxtech.shared.nativejs.NativeMatrix(out, this.nativeMatrixFactory);
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

                        return new com.btxtech.shared.nativejs.NativeMatrix(out, this.nativeMatrixFactory);
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

                },

                NativeMatrixDto: function (float32Array) {
                    this.numbers = float32Array;
                },

                NativeVertexDto: function () {
                    this.x = null;
                    this.y = null;
                    this.z = null;
                },

                TerrainTile: function () {
                    this.init = function (indexX, indexY) {
                        this.indexX = indexX;
                        this.indexY = indexY;
                    };

                    this.initGroundArrays = function (groundSizeVec, groundSizeScalar, nodes) {
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
                        if (typeof this.terrainSlopeTiles === 'undefined') {
                            this.terrainSlopeTiles = [];
                        }
                        this.terrainSlopeTiles.push(terrainSlopeTile)
                    };

                    this.getTerrainSlopeTiles = function () {
                        return this.terrainSlopeTiles;
                    };

                    this.setTerrainWaterTile = function (terrainWaterTile) {
                        this.terrainWaterTile = terrainWaterTile;
                    };

                    this.getTerrainWaterTile = function () {
                        return this.terrainWaterTile;
                    };

                    this.setLandWaterProportion = function (landWaterProportion) {
                        this.landWaterProportion = landWaterProportion;
                    };

                    this.getLandWaterProportion = function () {
                        return this.landWaterProportion;
                    };

                    this.setHeight = function (height) {
                        this.height = height;
                    };

                    this.getHeight = function () {
                        return this.height;
                    };

                    this.initTerrainNodeField = function (terrainTileNodesEdgeCount) {
                        this.terrainNodes = new Array(terrainTileNodesEdgeCount);
                        for (var i = 0; i < terrainTileNodesEdgeCount; i++) {
                            this.terrainNodes[i] = new Array(terrainTileNodesEdgeCount);
                        }
                    };

                    this.insertTerrainNode = function (x, y, terrainNodes) {
                        this.terrainNodes[x][y] = terrainNodes;
                    };

                    this.getTerrainNodes = function () {
                        return this.terrainNodes;
                    };

                    this.getTerrainTileObjectLists = function () {
                        return this.terrainTileObjectLists;
                    };

                    this.addTerrainTileObjectList = function (terrainTileObjectList) {
                        if (typeof this.terrainTileObjectLists === 'undefined') {
                            this.terrainTileObjectLists = [];
                        }
                        this.terrainTileObjectLists.push(terrainTileObjectList)
                    };

                    this.toArray = function () {
                        var terrainSlopeTilesArray = [];
                        if (typeof this.terrainSlopeTiles !== 'undefined') {
                            for (var i = 0; i < this.terrainSlopeTiles.length; i++) {
                                terrainSlopeTilesArray.push(this.terrainSlopeTiles[i].toArray());
                            }
                        }
                        var terrainWaterTile = [];
                        if (typeof this.terrainWaterTile !== 'undefined') {
                            terrainWaterTile = this.terrainWaterTile.toArray();
                        }

                        var terrainNodesField = [];
                        if (typeof this.terrainNodes !== 'undefined' && this.terrainNodes.length > 0) {
                            terrainNodesField = new Array(this.terrainNodes.length);
                            for (var x = 0; x < this.terrainNodes.length; x++) {
                                terrainNodesField[x] = new Array(this.terrainNodes[x].length);
                                for (var y = 0; y < this.terrainNodes[x].length; y++) {
                                    var terrainNode = this.terrainNodes[x][y];
                                    if (typeof terrainNode !== 'undefined') {
                                        terrainNodesField[x][y] = terrainNode.toArray();
                                    }
                                }
                            }
                        }

                        var terrainTileObjectListsField = [];
                        if (typeof this.terrainTileObjectLists !== 'undefined') {
                            for (var z = 0; z < this.terrainTileObjectLists.length; z++) {
                                terrainTileObjectListsField.push(this.terrainTileObjectLists[z].toArray());
                            }
                        }

                        return [this.indexX, this.indexY, this.groundVertexCount, this.groundVertices, this.groundNorms, this.groundTangents,
                            this.groundSplattings, terrainSlopeTilesArray, terrainWaterTile, this.landWaterProportion, this.height, terrainNodesField,
                            terrainTileObjectListsField];
                    };

                    this.fromArray = function (array, nativeMatrixFactory) {
                        this.indexX = array[0];
                        this.indexY = array[1];
                        this.groundVertexCount = array[2];
                        this.groundVertices = array[3];
                        this.groundNorms = array[4];
                        this.groundTangents = array[5];
                        this.groundSplattings = array[6];
                        var terrainSlopeTilesArray = array[7];
                        if (typeof terrainSlopeTilesArray !== 'undefined') {
                            for (var i = 0; i < terrainSlopeTilesArray.length; i++) {
                                var terrainSlopeTile = new com.btxtech.shared.nativejs.TerrainSlopeTile();
                                terrainSlopeTile.fromArray(terrainSlopeTilesArray[i]);
                                this.addTerrainSlopeTile(terrainSlopeTile);
                            }
                        }
                        var terrainWaterTile = array[8];
                        if (typeof terrainWaterTile !== 'undefined') {
                            this.terrainWaterTile = new com.btxtech.shared.nativejs.TerrainWaterTile();
                            this.terrainWaterTile.fromArray(terrainWaterTile);
                        }
                        this.landWaterProportion = array[9];

                        this.height = array[10];
                        var terrainNodesField = array[11];
                        if (typeof terrainNodesField !== 'undefined' && terrainNodesField.length > 0) {
                            this.terrainNodes = new Array(terrainNodesField.length);
                            for (var x = 0; x < terrainNodesField.length; x++) {
                                this.terrainNodes[x] = new Array(terrainNodesField[x].length);
                                for (var y = 0; y < terrainNodesField[x].length; y++) {
                                    var terrainNodeArray = terrainNodesField[x][y];
                                    if (typeof terrainNodeArray !== 'undefined') {
                                        var terrainNode = new com.btxtech.shared.nativejs.TerrainNode();
                                        terrainNode.fromArray(terrainNodeArray);
                                        this.terrainNodes[x][y] = terrainNode;
                                    }
                                }
                            }
                        }
                        var terrainTileObjectListsField = array[12];
                        if (typeof terrainTileObjectListsField !== 'undefined' && terrainTileObjectListsField.length > 0) {
                            this.terrainTileObjectLists = [];
                            for (var a = 0; a < terrainTileObjectListsField.length; a++) {
                                var tileObjectList = new com.btxtech.shared.nativejs.TerrainTileObjectList();
                                tileObjectList.fromArray(terrainTileObjectListsField[a], nativeMatrixFactory);
                                this.terrainTileObjectLists.push(tileObjectList);
                            }
                        }
                    }
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
                },

                TerrainWaterTile: function () {
                    this.initArray = function (sizeVec) {
                        this.vertices = new Float32Array(sizeVec);
                    };

                    this.setTriangleCorner = function (triangleCornerIndex, vertexX, vertexY, vertexZ) {
                        var cornerScalarIndex = triangleCornerIndex * 3;
                        this.vertices[cornerScalarIndex] = vertexX;
                        this.vertices[cornerScalarIndex + 1] = vertexY;
                        this.vertices[cornerScalarIndex + 2] = vertexZ;
                    };

                    this.getVertices = function () {
                        return this.vertices;
                    };

                    this.setVertexCount = function (vertexCount) {
                        this.vertexCount = vertexCount;
                    };

                    this.getVertexCount = function () {
                        return this.vertexCount;
                    };

                    this.toArray = function () {
                        return [this.vertexCount, this.vertices];
                    };

                    this.fromArray = function (array) {
                        this.vertexCount = array[0];
                        this.vertices = array[1];
                    };

                },

                TerrainNode: function () {
                    this.initTerrainSubNodeField = function (terrainSubNodeEdgeCount) {
                        this.terrainSubNodes = new Array(terrainSubNodeEdgeCount);
                        for (var i = 0; i < terrainSubNodeEdgeCount; i++) {
                            this.terrainSubNodes[i] = new Array(terrainSubNodeEdgeCount);
                        }
                    };

                    this.getTerrainSubNodes = function () {
                        return this.terrainSubNodes;
                    };

                    this.insertTerrainSubNode = function (x, y, terrainSubNode) {
                        this.terrainSubNodes[x][y] = terrainSubNode;
                    };

                    this.isLand = function () {
                        return this.land;
                    };

                    this.setLand = function (land) {
                        this.land = land;
                    };

                    this.getHeight = function () {
                        return this.height;
                    };

                    this.setHeight = function (height) {
                        this.height = height;
                    };

                    this.getTerrainType = function () {
                        return this.terrainType;
                    };

                    this.setTerrainType = function (terrainType) {
                        this.terrainType = terrainType;
                    };

                    this.toArray = function () {
                        var terrainSubNodesField = [];
                        if (typeof this.terrainSubNodes !== 'undefined' && this.terrainSubNodes.length > 0) {
                            terrainSubNodesField = new Array(this.terrainSubNodes.length);
                            for (var x = 0; x < this.terrainSubNodes.length; x++) {
                                terrainSubNodesField[x] = new Array(this.terrainSubNodes[x].length);
                                for (var y = 0; y < this.terrainSubNodes[x].length; y++) {
                                    var terrainSubNode = this.terrainSubNodes[x][y];
                                    if (typeof terrainSubNode !== 'undefined') {
                                        terrainSubNodesField[x][y] = terrainSubNode.toArray();
                                    }
                                }
                            }
                        }
                        return [this.land, this.height, this.terrainType, terrainSubNodesField];
                    };

                    this.fromArray = function (terrainNodeArray) {
                        this.land = terrainNodeArray[0];
                        this.height = terrainNodeArray[1];
                        this.terrainType = terrainNodeArray[2];

                        var terrainSubNodesField = terrainNodeArray[3];
                        if (typeof terrainSubNodesField !== 'undefined' && terrainSubNodesField.length > 0) {
                            this.terrainSubNodes = new Array(terrainSubNodesField.length);
                            for (var x = 0; x < terrainSubNodesField.length; x++) {
                                this.terrainSubNodes[x] = new Array(terrainSubNodesField[x].length);
                                for (var y = 0; y < terrainSubNodesField[x].length; y++) {
                                    var terrainSubNodeArray = terrainSubNodesField[x][y];
                                    if (typeof terrainSubNodeArray !== 'undefined') {
                                        var terrainSubNode = new com.btxtech.shared.nativejs.TerrainSubNode();
                                        terrainSubNode.fromArray(terrainSubNodeArray);
                                        this.terrainSubNodes[x][y] = terrainSubNode;
                                    }
                                }
                            }
                        }
                    };
                },

                TerrainSubNode: function () {
                    this.initTerrainSubNodeField = function (terrainSubNodeEdgeCount) {
                        this.terrainSubNodes = new Array(terrainSubNodeEdgeCount);
                        for (var i = 0; i < terrainSubNodeEdgeCount; i++) {
                            this.terrainSubNodes[i] = new Array(terrainSubNodeEdgeCount);
                        }
                    };

                    this.getTerrainSubNodes = function () {
                        return this.terrainSubNodes;
                    };

                    this.insertTerrainSubNode = function (x, y, terrainSubNode) {
                        this.terrainSubNodes[x][y] = terrainSubNode;
                    };

                    this.isLand = function () {
                        return this.land;
                    };

                    this.setLand = function (land) {
                        this.land = land;
                    };

                    this.getHeight = function () {
                        return this.height;
                    };

                    this.setHeight = function (height) {
                        this.height = height;
                    };

                    this.getTerrainType = function () {
                        return this.terrainType;
                    };

                    this.setTerrainType = function (terrainType) {
                        this.terrainType = terrainType;
                    };

                    this.toArray = function () {
                        var terrainSubNodesField = [];
                        if (typeof this.terrainSubNodes !== 'undefined' && this.terrainSubNodes.length > 0) {
                            terrainSubNodesField = new Array(this.terrainSubNodes);
                            for (var x = 0; x < this.terrainSubNodes.length; x++) {
                                terrainSubNodesField[x] = new Array(this.terrainSubNodes[x].length);
                                for (var y = 0; y < this.terrainSubNodes[x].length; y++) {
                                    var terrainSubNod = this.terrainSubNodes[x][y];
                                    if (typeof terrainSubNod !== 'undefined') {
                                        terrainSubNodesField[x][y] = terrainSubNod.toArray();
                                    }
                                }
                            }
                        }
                        return [this.land, this.height, this.terrainType, terrainSubNodesField];
                    };

                    this.fromArray = function (terrainNodeArray) {
                        this.land = terrainNodeArray[0];
                        this.height = terrainNodeArray[1];
                        this.terrainType = terrainNodeArray[2];

                        var terrainSubNodesField = terrainNodeArray[3];
                        if (typeof terrainSubNodesField !== 'undefined' && terrainSubNodesField.length > 0) {
                            this.terrainSubNodes = new Array(terrainSubNodesField.length);
                            for (var x = 0; x < terrainSubNodesField.length; x++) {
                                this.terrainSubNodes[x] = new Array(terrainSubNodesField[x].length);
                                for (var y = 0; y < terrainSubNodesField[x].length; y++) {
                                    var terrainSubNodeArray = terrainSubNodesField[x][y];
                                    if (typeof terrainSubNodeArray !== 'undefined') {
                                        var terrainSubNode = new com.btxtech.shared.nativejs.TerrainSubNode();
                                        terrainSubNode.fromArray(terrainSubNodeArray);
                                        this.terrainSubNodes[x][y] = terrainSubNode;
                                    }
                                }
                            }
                        }
                    };
                },

                TerrainTileObjectList: function () {
                    this.getTerrainObjectConfigId = function () {
                        return this.terrainObjectConfigId;
                    };

                    this.setTerrainObjectConfigId = function (terrainObjectConfigId) {
                        this.terrainObjectConfigId = terrainObjectConfigId;
                    };

                    this.addModel = function (nativeMatrix) {
                        if (typeof this.models === 'undefined') {
                            this.models = [];
                        }
                        this.models.push(nativeMatrix);
                    };

                    this.getModels = function () {
                        return this.models;
                    };

                    this.toArray = function() {
                        var array = [];
                        array[0] = this.terrainObjectConfigId;
                        var modelArray = [];
                        for(i = 0; i < this.models.length; i++) {
                            modelArray[i] = this.models[i].float32Array;
                        }
                        array[1] = modelArray;
                        return array;
                    };

                    this.fromArray = function(array, nativeMatrixFactory) {
                        this.terrainObjectConfigId = array[0];
                        this.models = [];
                        var nativeMatrices = array[1];
                        for(i = 0; i < nativeMatrices.length; i++) {
                            this.models.push(new com.btxtech.shared.nativejs.NativeMatrix(nativeMatrices[i], nativeMatrixFactory));
                        }
                    };
                },

                workerdto: {
                    NativeTickInfo: function () {
                        this.resources = 0;
                        this.xpFromKills = 0;
                        this.houseSpace = 0;
                        this.updatedNativeSyncBaseItemTickInfos = null;
                        this.killedSyncBaseItems = null;
                        this.removeSyncBaseItemIds = null;
                    },

                    NativeSimpleSyncBaseItemTickInfo: function () {
                        this.id = 0;
                        this.itemTypeId = 0;
                        this.contained = false;
                        this.x = 0;
                        this.y = 0;
                        this.z = 0;
                    },

                    NativeSyncBaseItemTickInfo: function () {
                        this.id = 0;
                        this.itemTypeId = 0;
                        this.x = 0;
                        this.y = 0;
                        this.z = 0;
                        this.model = null;
                        this.baseId = 0;
                        this.turretAngle = 0;
                        this.spawning = 0;
                        this.buildup = 0;
                        this.health = 0;
                        this.constructing = 0;
                        this.harvestingResourcePosition = null;
                        this.buildingPosition = null;
                        this.interpolatableVelocity = null;
                        this.containingItemCount = 0;
                        this.maxContainingRadius = 0;
                        this.contained = false;
                    }
                }
            }
        }
        // client: {
        //     nativejs: {
        //     }
        // }
    }
};

// Static methode example
// com.btxtech.client.nativejs.NativeControlUtils = {};
// com.btxtech.client.nativejs.NativeControlUtils.openSingleFileDataUrlUpload = function (callback) {
//     callback(xxx, yyy);
// };
