/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `GROUND_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bottomBumpMapDepth` double DEFAULT NULL,
  `bottomScale` double DEFAULT NULL,
  `bottomShininess` double DEFAULT NULL,
  `bottomSpecularStrength` double DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `splattingBlur` double DEFAULT NULL,
  `splattingOffset` double DEFAULT NULL,
  `splattingScale1` double DEFAULT NULL,
  `splattingScale2` double DEFAULT NULL,
  `topBumpMapDepth` double DEFAULT NULL,
  `topScale` double DEFAULT NULL,
  `topShininess` double DEFAULT NULL,
  `topSpecularStrength` double DEFAULT NULL,
  `bottomBumpMapId` int(11) DEFAULT NULL,
  `bottomTextureId` int(11) DEFAULT NULL,
  `splattingTextureId` int(11) DEFAULT NULL,
  `topBumpMapId` int(11) DEFAULT NULL,
  `topTextureId` int(11) DEFAULT NULL,
  `bottomNormalMapDepth` double DEFAULT NULL,
  `topNormalMapDepth` double DEFAULT NULL,
  `bottomNormalMapId` int(11) DEFAULT NULL,
  `topNormalMapId` int(11) DEFAULT NULL,
  `bottomMaterial_id` int(11) DEFAULT NULL,
  `topMaterial_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrwtceqklrjay1axmmlfj75di3` (`bottomBumpMapId`),
  KEY `FKnoefvdom880ef7ioae7tjwct8` (`bottomTextureId`),
  KEY `FK8vbr1544f1nkaxkkhk7vdf1ew` (`splattingTextureId`),
  KEY `FKkqjsikvnicb0qo3auqhlghq3a` (`topBumpMapId`),
  KEY `FKnrbnl1yvrst306eyg8mkpqt6i` (`topTextureId`),
  KEY `FKiy2giiunded3eusa4downovu5` (`bottomNormalMapId`),
  KEY `FKd4w171tw693jvfg7g1r2kdv3k` (`topNormalMapId`),
  KEY `FK5aaxmjvpd8ftb307bca463eb1` (`bottomMaterial_id`),
  KEY `FK4aw29gchqjiaqbg4tfn8y8x1n` (`topMaterial_id`),
  CONSTRAINT `FK4aw29gchqjiaqbg4tfn8y8x1n` FOREIGN KEY (`topMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FK5aaxmjvpd8ftb307bca463eb1` FOREIGN KEY (`bottomMaterial_id`) REFERENCES `THREE_JS_MODEL` (`id`),
  CONSTRAINT `FK8vbr1544f1nkaxkkhk7vdf1ew` FOREIGN KEY (`splattingTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKd4w171tw693jvfg7g1r2kdv3k` FOREIGN KEY (`topNormalMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKiy2giiunded3eusa4downovu5` FOREIGN KEY (`bottomNormalMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKkqjsikvnicb0qo3auqhlghq3a` FOREIGN KEY (`topBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKnoefvdom880ef7ioae7tjwct8` FOREIGN KEY (`bottomTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKnrbnl1yvrst306eyg8mkpqt6i` FOREIGN KEY (`topTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKrwtceqklrjay1axmmlfj75di3` FOREIGN KEY (`bottomBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=257 DEFAULT CHARSET=utf8mb4;

DELETE FROM `GROUND_CONFIG`;
/*!40000 ALTER TABLE `GROUND_CONFIG` DISABLE KEYS */;
INSERT INTO `GROUND_CONFIG` (`id`, `bottomBumpMapDepth`, `bottomScale`, `bottomShininess`, `bottomSpecularStrength`, `internalName`, `splattingBlur`, `splattingOffset`, `splattingScale1`, `splattingScale2`, `topBumpMapDepth`, `topScale`, `topShininess`, `topSpecularStrength`, `bottomBumpMapId`, `bottomTextureId`, `splattingTextureId`, `topBumpMapId`, `topTextureId`, `bottomNormalMapDepth`, `topNormalMapDepth`, `bottomNormalMapId`, `topNormalMapId`, `bottomMaterial_id`, `topMaterial_id`) VALUES
	(252, 0.8, 50, 5, 0.5, 'Grass Dirt', 0.2, 0.5, 50, 1000, 0.2, 50, 3, 0.5, 129, 128, 101, 98, 97, 0.3, NULL, 133, NULL, NULL, 5),
	(253, NULL, NULL, NULL, NULL, 'Razar Industries', NULL, NULL, NULL, NULL, 0.3, 10, 3, 0.5, NULL, NULL, NULL, NULL, 136, NULL, 0.1, NULL, 137, NULL, 6),
	(254, NULL, NULL, NULL, NULL, 'Ocean Ground', NULL, NULL, NULL, NULL, 1, 700, 30, 1, NULL, NULL, NULL, 28, 111, NULL, NULL, NULL, NULL, NULL, 11),
	(255, NULL, NULL, NULL, NULL, 'Razar Poison Pool', NULL, NULL, NULL, NULL, NULL, 20, 1, 1, NULL, NULL, NULL, NULL, 140, NULL, 0.1, NULL, 137, NULL, 21);
/*!40000 ALTER TABLE `GROUND_CONFIG` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
