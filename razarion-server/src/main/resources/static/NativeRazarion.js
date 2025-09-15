com = {
    btxtech: {
        shared: {
            nativejs: {
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
                        this.containingItemTypeIds = null;
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
