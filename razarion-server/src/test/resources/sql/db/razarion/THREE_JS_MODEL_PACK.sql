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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4;

DELETE FROM `THREE_JS_MODEL_PACK`;
/*!40000 ALTER TABLE `THREE_JS_MODEL_PACK` DISABLE KEYS */;
INSERT INTO `THREE_JS_MODEL_PACK` (`id`, `internalName`, `positionX`, `positionY`, `positionZ`, `rotationX`, `rotationY`, `rotationZ`, `scaleX`, `scaleY`, `scaleZ`, `threeJsModelConfig_id`) VALUES
	(11, 'Stone 12 [Stone Pack 1]', -2.5, -0.8, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(12, 'Fern 1 [Tropical Vegetation 1]', 0, -1.2, 0, 1.5708, 0, 0, 0.025, 0.025, -0.025, 13),
	(13, 'Fern 2 [Tropical Vegetation 1]', 0, 0, 0, 1.5708, 0, 0, 0.025, 0.025, -0.025, 13),
	(14, 'Leaves [Tropical Vegetation 1]', -1.8, 0, 0.4, 1.5708, 0, 0, 0.025, 0.025, -0.025, 13),
	(15, 'Trunk 1 [Tropical Vegetation 1]', -2, -1.2, 0, 1.5708, 0, 0, 0.025, 0.025, -0.025, 13),
	(16, 'Palm [Tropical Vegetation 1]', 4, -1, 0, 3.1416, 0, 0, 0.005, 0.005, -0.005, 13),
	(17, 'Trunk 3 [Tropical Vegetation 1]', 0, 0, 0, 1.5708, 0, 0, 0.025, 0.025, -0.025, 13),
	(18, 'Stone 1 [Stone Pack 1]', -7, 0, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(19, 'Stone 2 [Stone Pack 1]', -12, -6, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(20, 'Stone 3 [Stone Pack 1]', -9.5, 4.5, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(21, 'Stone 4 [Stone Pack 1]', -2.5, 0, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(22, 'Stone 5 [Stone Pack 1]', 14, -8, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(23, 'Stone 6 [Stone Pack 1]', 6, -8, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(24, 'Stone 7 [Stone Pack 1]', 0, -8, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(25, 'Stone 8 [Stone Pack 1]', -6, -6.5, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(26, 'Stone 9 [Stone Pack 1]', 11.5, -1, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(27, 'Stone 10 [Stone Pack 1]', 7, -1, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(28, 'Stone 11 [Stone Pack 1]', 2, 0, 0, -1.5708, 0, 0, 0.025, 0.025, -0.025, 12),
	(30, 'Building 01', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 47),
	(31, 'Building 02', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 47);
/*!40000 ALTER TABLE `THREE_JS_MODEL_PACK` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
