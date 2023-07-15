/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `SLOPE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coastDelimiterLineGameEngine` double NOT NULL,
  `horizontalSpace` double NOT NULL,
  `innerLineGameEngine` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `interpolateNorm` bit(1) NOT NULL,
  `outerLineGameEngine` double NOT NULL,
  `shallowWaterDistortionStrength` double DEFAULT NULL,
  `shallowWaterDurationSeconds` double DEFAULT NULL,
  `shallowWaterScale` double DEFAULT NULL,
  `groundConfig_id` int(11) DEFAULT NULL,
  `shallowWaterDistortionId` int(11) DEFAULT NULL,
  `shallowWaterStencilId` int(11) DEFAULT NULL,
  `shallowWaterTextureId` int(11) DEFAULT NULL,
  `waterConfig_id` int(11) DEFAULT NULL,
  `threeJsMaterial_id` int(11) DEFAULT NULL,
  `shallowWaterThreeJsMaterial_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm6v85hb3xsj0d7o7dcsyutdtu` (`groundConfig_id`),
  KEY `FKikwl0enwj0gso8unw0ijr9xh7` (`shallowWaterDistortionId`),
  KEY `FK37ttmo5xct34jlqhc9wynq6h5` (`shallowWaterStencilId`),
  KEY `FKkys6s1fy27hepboitgejhub89` (`shallowWaterTextureId`),
  KEY `FKemea7ct22uhupjb877kxqpud9` (`waterConfig_id`),
  KEY `FK4uano63cgtpplq9auryktcfti` (`threeJsMaterial_id`),
  KEY `FK76ed6hynj8vhao9yxfag80r0s` (`shallowWaterThreeJsMaterial_id`),
  CONSTRAINT `FK37ttmo5xct34jlqhc9wynq6h5` FOREIGN KEY (`shallowWaterStencilId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FK4uano63cgtpplq9auryktcfti` FOREIGN KEY (`threeJsMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FK76ed6hynj8vhao9yxfag80r0s` FOREIGN KEY (`shallowWaterThreeJsMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FKemea7ct22uhupjb877kxqpud9` FOREIGN KEY (`waterConfig_id`) REFERENCES `WATER_CONFIG` (`id`),
  CONSTRAINT `FKikwl0enwj0gso8unw0ijr9xh7` FOREIGN KEY (`shallowWaterDistortionId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKkys6s1fy27hepboitgejhub89` FOREIGN KEY (`shallowWaterTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKm6v85hb3xsj0d7o7dcsyutdtu` FOREIGN KEY (`groundConfig_id`) REFERENCES `GROUND_CONFIG` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4;

DELETE FROM `SLOPE_CONFIG`;
/*!40000 ALTER TABLE `SLOPE_CONFIG` DISABLE KEYS */;
INSERT INTO `SLOPE_CONFIG` (`id`, `coastDelimiterLineGameEngine`, `horizontalSpace`, `innerLineGameEngine`, `internalName`, `interpolateNorm`, `outerLineGameEngine`, `shallowWaterDistortionStrength`, `shallowWaterDurationSeconds`, `shallowWaterScale`, `groundConfig_id`, `shallowWaterDistortionId`, `shallowWaterStencilId`, `shallowWaterTextureId`, `waterConfig_id`, `threeJsMaterial_id`, `shallowWaterThreeJsMaterial_id`) VALUES
	(1, 0, 5, 1, 'Razar Industries', b'0', 0, NULL, NULL, NULL, 253, NULL, NULL, NULL, NULL, 7, NULL),
	(22, 5, 2, 7, 'Ocean Beach', b'1', 3, 0.5, 10, 14, 254, 110, 109, 108, 10, 15, 23);
/*!40000 ALTER TABLE `SLOPE_CONFIG` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
