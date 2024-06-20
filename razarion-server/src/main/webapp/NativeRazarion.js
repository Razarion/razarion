com = {
    btxtech: {
        shared: {
            nativejs: {
                NativeMatrixFactory: function () {
                    this.createFromColumnMajorArray = function (array) {
                        return new com.btxtech.shared.nativejs.NativeMatrix(new Float32Array(array), this);
                    };

                    this.createFromColumnMajorFloat32ArrayEmu = function (float32Array) {
                        return new com.btxtech.shared.nativejs.NativeMatrix(float32Array, this);
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

                    this.getColumnMajorFloat32Array = function () {
                        return this.float32Array;
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
                        this.constructingBaseItemTypeId = -1;
                        this.harvestingResourcePosition = null;
                        this.buildingPosition = null;
                        this.interpolatableVelocity = null;
                        this.containingItemCount = 0;
                        this.maxContainingRadius = 0;
                        this.contained = false;
                    },

                    NativeDecimalPosition: function () {
                        this.x = 0;
                        this.y = 0;
                    }
                }
            }
        }
    }
};

// Static methode example
// com.btxtech.client.json.NativeControlUtils = {};
// com.btxtech.client.json.NativeControlUtils.openSingleFileDataUrlUpload = function (callback) {
//     callback(xxx, yyy);
// };
