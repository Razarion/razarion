/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `THREE_JS_MODEL_PACK` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `positionX` double DEFAULT NULL,
  `positionY` double DEFAULT NULL,
  `positionZ` double DEFAULT NULL,
  `rotationX` double DEFAULT NULL,
  `rotationY` double DEFAULT NULL,
  `rotationZ` double DEFAULT NULL,
  `scaleX` double DEFAULT NULL,
  `scaleY` double DEFAULT NULL,
  `scaleZ` double DEFAULT NULL,
  `threeJsModelConfig_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1odbqn8a5lg9edjr451fi17hf` (`threeJsModelConfig_id`),
  CONSTRAINT `FK1odbqn8a5lg9edjr451fi17hf` FOREIGN KEY (`threeJsModelConfig_id`) REFERENCES `THREE_JS_MODEL` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;

DELETE FROM `THREE_JS_MODEL_PACK`;
/*!40000 ALTER TABLE `THREE_JS_MODEL_PACK` DISABLE KEYS */;
INSERT INTO `THREE_JS_MODEL_PACK` (`id`, `internalName`, `positionX`, `positionY`, `positionZ`, `rotationX`, `rotationY`, `rotationZ`, `scaleX`, `scaleY`, `scaleZ`, `threeJsModelConfig_id`) VALUES
	(9, 'Palm Tree Pack 1', -1.5, 0, 0, 1.571, 0, 0, 0.01, 0.01, 0.01, 1),
	(10, 'Palm Tree Pack 2', -4, 0, 0, 1.571, 0, 0, 0.01, 0.01, 0.01, 1),
	(11, 'Stone Pack 1', -2.5, 0, 0, 0, 0, 0, 0.025, 0.025, 0.025, 12),
	(12, 'Fern 1 [Tropical Vegetation 1]', 0, -1.2, 0, 0, 0, 0, 0.015, 0.015, 0.015, 13),
	(13, 'Fern 2 [Tropical Vegetation 1]', 0, 0, 0, 0, 0, 0, 0.015, 0.015, 0.015, 13),
	(14, 'Leaves [Tropical Vegetation 1]', -1.8, -2.4, 0, 0, 0, 0, 0.015, 0.015, 0.015, 13),
	(15, 'Trunk 1 [Tropical Vegetation 1]', -2, -1.2, 0, 1.5708, 1, 0, 0.015, 0.015, 0.015, 13),
	(16, 'Palm [Tropical Vegetation 1]', 4, -1, 0, 1.5708, 0, 0, 0.004, 0.004, 0.004, 13),
	(17, 'Trunk 3 [Tropical Vegetation 1]', 0, 0, 0, 0, 0, 0, 0.015, 0.015, 0.015, 13);
/*!40000 ALTER TABLE `THREE_JS_MODEL_PACK` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
