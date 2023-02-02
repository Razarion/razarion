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
  `innerSplattingBlur` double DEFAULT NULL,
  `innerSplattingImpact` double DEFAULT NULL,
  `innerSplattingOffset` double DEFAULT NULL,
  `innerSplattingScale` double DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `interpolateNorm` bit(1) NOT NULL,
  `materialBumpMapDepth` double DEFAULT NULL,
  `materialScale` double DEFAULT NULL,
  `materialShininess` double DEFAULT NULL,
  `materialSpecularStrength` double DEFAULT NULL,
  `outerLineGameEngine` double NOT NULL,
  `outerSplattingBlur` double DEFAULT NULL,
  `outerSplattingImpact` double DEFAULT NULL,
  `outerSplattingOffset` double DEFAULT NULL,
  `outerSplattingScale` double DEFAULT NULL,
  `shallowWaterDistortionStrength` double DEFAULT NULL,
  `shallowWaterDurationSeconds` double DEFAULT NULL,
  `shallowWaterScale` double DEFAULT NULL,
  `groundConfig_id` int(11) DEFAULT NULL,
  `innerSplattingTextureId` int(11) DEFAULT NULL,
  `materialBumpMapId` int(11) DEFAULT NULL,
  `materialTextureId` int(11) DEFAULT NULL,
  `outerSplattingTextureId` int(11) DEFAULT NULL,
  `shallowWaterDistortionId` int(11) DEFAULT NULL,
  `shallowWaterStencilId` int(11) DEFAULT NULL,
  `shallowWaterTextureId` int(11) DEFAULT NULL,
  `waterConfig_id` int(11) DEFAULT NULL,
  `materialNormalMapDepth` double DEFAULT NULL,
  `materialNormalMapId` int(11) DEFAULT NULL,
  `threeJsMaterial_id` int(11) DEFAULT NULL,
  `shallowWaterThreeJsMaterial_id` int(11) DEFAULT NULL,
  `innerSlopeThreeJsMaterial_id` int(11) DEFAULT NULL,
  `outerSlopeThreeJsMaterial_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm6v85hb3xsj0d7o7dcsyutdtu` (`groundConfig_id`),
  KEY `FKq4ko6usrulwximbtoh9sfua6m` (`innerSplattingTextureId`),
  KEY `FKd0t0cu236j6ew5sub5qa5vbld` (`materialBumpMapId`),
  KEY `FKfmabq4sou5w8whtsyshp15jwe` (`materialTextureId`),
  KEY `FKkr6ugilc4ilmpx9k4niq0v5h3` (`outerSplattingTextureId`),
  KEY `FKikwl0enwj0gso8unw0ijr9xh7` (`shallowWaterDistortionId`),
  KEY `FK37ttmo5xct34jlqhc9wynq6h5` (`shallowWaterStencilId`),
  KEY `FKkys6s1fy27hepboitgejhub89` (`shallowWaterTextureId`),
  KEY `FKemea7ct22uhupjb877kxqpud9` (`waterConfig_id`),
  KEY `FKl1rruyd2re9k16y4rovwcyqdw` (`materialNormalMapId`),
  KEY `FK4uano63cgtpplq9auryktcfti` (`threeJsMaterial_id`),
  KEY `FK76ed6hynj8vhao9yxfag80r0s` (`shallowWaterThreeJsMaterial_id`),
  KEY `FKemvmak4pk33vkous08hjr1vne` (`innerSlopeThreeJsMaterial_id`),
  KEY `FKjxu6scmtdxmsoevf46vauispw` (`outerSlopeThreeJsMaterial_id`),
  CONSTRAINT `FK37ttmo5xct34jlqhc9wynq6h5` FOREIGN KEY (`shallowWaterStencilId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FK4uano63cgtpplq9auryktcfti` FOREIGN KEY (`threeJsMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FK76ed6hynj8vhao9yxfag80r0s` FOREIGN KEY (`shallowWaterThreeJsMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FKd0t0cu236j6ew5sub5qa5vbld` FOREIGN KEY (`materialBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKemea7ct22uhupjb877kxqpud9` FOREIGN KEY (`waterConfig_id`) REFERENCES `WATER_CONFIG` (`id`),
  CONSTRAINT `FKemvmak4pk33vkous08hjr1vne` FOREIGN KEY (`innerSlopeThreeJsMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FKfmabq4sou5w8whtsyshp15jwe` FOREIGN KEY (`materialTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKikwl0enwj0gso8unw0ijr9xh7` FOREIGN KEY (`shallowWaterDistortionId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKjxu6scmtdxmsoevf46vauispw` FOREIGN KEY (`outerSlopeThreeJsMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FKkr6ugilc4ilmpx9k4niq0v5h3` FOREIGN KEY (`outerSplattingTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKkys6s1fy27hepboitgejhub89` FOREIGN KEY (`shallowWaterTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKl1rruyd2re9k16y4rovwcyqdw` FOREIGN KEY (`materialNormalMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKm6v85hb3xsj0d7o7dcsyutdtu` FOREIGN KEY (`groundConfig_id`) REFERENCES `GROUND_CONFIG` (`id`),
  CONSTRAINT `FKq4ko6usrulwximbtoh9sfua6m` FOREIGN KEY (`innerSplattingTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4;

DELETE FROM `SLOPE_CONFIG`;
/*!40000 ALTER TABLE `SLOPE_CONFIG` DISABLE KEYS */;
INSERT INTO `SLOPE_CONFIG` (`id`, `coastDelimiterLineGameEngine`, `horizontalSpace`, `innerLineGameEngine`, `innerSplattingBlur`, `innerSplattingImpact`, `innerSplattingOffset`, `innerSplattingScale`, `internalName`, `interpolateNorm`, `materialBumpMapDepth`, `materialScale`, `materialShininess`, `materialSpecularStrength`, `outerLineGameEngine`, `outerSplattingBlur`, `outerSplattingImpact`, `outerSplattingOffset`, `outerSplattingScale`, `shallowWaterDistortionStrength`, `shallowWaterDurationSeconds`, `shallowWaterScale`, `groundConfig_id`, `innerSplattingTextureId`, `materialBumpMapId`, `materialTextureId`, `outerSplattingTextureId`, `shallowWaterDistortionId`, `shallowWaterStencilId`, `shallowWaterTextureId`, `waterConfig_id`, `materialNormalMapDepth`, `materialNormalMapId`, `threeJsMaterial_id`, `shallowWaterThreeJsMaterial_id`, `innerSlopeThreeJsMaterial_id`, `outerSlopeThreeJsMaterial_id`) VALUES
	(1, 0, 5, 1.5, NULL, NULL, NULL, NULL, 'Razar Industries', b'0', 0.5, 16, 20, 0.5, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 253, NULL, 103, 102, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 7, NULL, NULL, NULL),
	(22, 5, 2, 7, 0.1, 0.58, 0.5, 300, 'Ocean Beach', b'1', 0.5, 43, 3, 0.5, 3, 0.02, 1, 0.5, 45, 0.5, 10, 14, 254, 101, 107, 106, 101, 110, 109, 108, 10, NULL, NULL, 15, 23, 24, 25),
	(23, 0.5, 6, 1, NULL, NULL, NULL, NULL, 'Razar Poison Pool', b'0', 0.5, 16, 20, 0.5, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 255, NULL, NULL, 138, NULL, NULL, NULL, NULL, 11, 0.5, 139, 20, NULL, NULL, NULL),
	(25, 0, 5, 1.5, NULL, NULL, NULL, NULL, 'Razar Industries inverted', b'0', 5, 20, 20, 0.5, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 103, 102, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 7, NULL, NULL, NULL);
/*!40000 ALTER TABLE `SLOPE_CONFIG` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
