/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `SCENE_TIP_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `terrainPositionHintX` double DEFAULT NULL,
  `terrainPositionHintY` double DEFAULT NULL,
  `tip` varchar(255) DEFAULT NULL,
  `actor_id` int(11) DEFAULT NULL,
  `boxItemTypeEntity_id` int(11) DEFAULT NULL,
  `inventoryItemEntity_id` int(11) DEFAULT NULL,
  `placeConfig_id` int(11) DEFAULT NULL,
  `resourceItemTypeEntity_id` int(11) DEFAULT NULL,
  `scrollMapImage_id` int(11) DEFAULT NULL,
  `toCreatedItemType_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKghfmw5laj3jq1yjgw957xe8xx` (`actor_id`),
  KEY `FKrjgv1q2d2psdp6e9pmku4lprw` (`boxItemTypeEntity_id`),
  KEY `FKm16pv7bo8k3tuys0w9r13f73r` (`inventoryItemEntity_id`),
  KEY `FKsj33u8l46y25lbxi2tji7fato` (`placeConfig_id`),
  KEY `FKpuux23wj061stcnd20tyyvy8e` (`resourceItemTypeEntity_id`),
  KEY `FKcwh07brwqim61gt1cxpgb8b2c` (`scrollMapImage_id`),
  KEY `FKrb1ne75jd6gn0rlx5av4cy9ga` (`toCreatedItemType_id`),
  CONSTRAINT `FKcwh07brwqim61gt1cxpgb8b2c` FOREIGN KEY (`scrollMapImage_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKghfmw5laj3jq1yjgw957xe8xx` FOREIGN KEY (`actor_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKm16pv7bo8k3tuys0w9r13f73r` FOREIGN KEY (`inventoryItemEntity_id`) REFERENCES `INVENTORY_ITEM` (`id`),
  CONSTRAINT `FKpuux23wj061stcnd20tyyvy8e` FOREIGN KEY (`resourceItemTypeEntity_id`) REFERENCES `RESOURCE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrb1ne75jd6gn0rlx5av4cy9ga` FOREIGN KEY (`toCreatedItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrjgv1q2d2psdp6e9pmku4lprw` FOREIGN KEY (`boxItemTypeEntity_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FKsj33u8l46y25lbxi2tji7fato` FOREIGN KEY (`placeConfig_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM `SCENE_TIP_CONFIG`;
/*!40000 ALTER TABLE `SCENE_TIP_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `SCENE_TIP_CONFIG` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
