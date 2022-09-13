/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `WATER_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bumpDistortionScale` double NOT NULL,
  `bumpMapDepth` double NOT NULL,
  `distortionDurationSeconds` double NOT NULL,
  `distortionStrength` double NOT NULL,
  `fresnelDelta` double NOT NULL,
  `fresnelOffset` double NOT NULL,
  `groundLevel` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `reflectionScale` double NOT NULL,
  `shininess` double NOT NULL,
  `specularStrength` double NOT NULL,
  `transparency` double NOT NULL,
  `waterLevel` double NOT NULL,
  `bumpMap_id` int(11) DEFAULT NULL,
  `distortion_id` int(11) DEFAULT NULL,
  `reflection_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk8u5re1hk96pkj5mbohe9fue7` (`bumpMap_id`),
  KEY `FKky3w6oh8qfvnu81s0ghcmlg8a` (`distortion_id`),
  KEY `FKjxbl886qx1n0nyxth0uto4g0j` (`reflection_id`),
  CONSTRAINT `FKjxbl886qx1n0nyxth0uto4g0j` FOREIGN KEY (`reflection_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKk8u5re1hk96pkj5mbohe9fue7` FOREIGN KEY (`bumpMap_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKky3w6oh8qfvnu81s0ghcmlg8a` FOREIGN KEY (`distortion_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4;

DELETE FROM `WATER_CONFIG`;
/*!40000 ALTER TABLE `WATER_CONFIG` DISABLE KEYS */;
INSERT INTO `WATER_CONFIG` (`id`, `bumpDistortionScale`, `bumpMapDepth`, `distortionDurationSeconds`, `distortionStrength`, `fresnelDelta`, `fresnelOffset`, `groundLevel`, `internalName`, `reflectionScale`, `shininess`, `specularStrength`, `transparency`, `waterLevel`, `bumpMap_id`, `distortion_id`, `reflection_id`) VALUES
	(10, 40, 0.5, 30, 7, 0.5, 0.8, -1, 'Ocean', 80, 30, 0.7, 0.6, -0.1, 161, 114, 112),
	(11, 30, 0.5, 30, 0.05, 0.5, 0.8, -3, 'Poison', 80, 30, 0.7, 0.8, -1, 34, 89, 132);
/*!40000 ALTER TABLE `WATER_CONFIG` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
