/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `TERRAIN_OBJECT` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `threeJsModelPackConfig_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl6f1pat64mpk8p56h60kynr4s` (`threeJsModelPackConfig_id`),
  CONSTRAINT `FKl6f1pat64mpk8p56h60kynr4s` FOREIGN KEY (`threeJsModelPackConfig_id`) REFERENCES `THREE_JS_MODEL_PACK` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4;

DELETE FROM `TERRAIN_OBJECT`;
/*!40000 ALTER TABLE `TERRAIN_OBJECT` DISABLE KEYS */;
INSERT INTO `TERRAIN_OBJECT` (`id`, `internalName`, `radius`, `threeJsModelPackConfig_id`) VALUES
	(6, 'Palm Tree 1', 3, 9),
	(7, 'Palm Tree 2', 3, 10),
	(8, 'Stone 12', 3, 11),
	(9, 'Fern 1', 3, 12),
	(10, 'Fern 2', 3, 13),
	(11, 'Leaves', 3, 14),
	(12, 'Trunk 1', 3, 15),
	(13, 'Palm', 3, 16),
	(14, 'Trunk 3', 3, 17),
	(15, 'Stone 1', 3, 18),
	(16, 'Stone 2', 3, 19),
	(17, 'Stone 3', 3, 20),
	(19, 'Stone 4', 3, 21),
	(20, 'Stone 5', 3, 22),
	(21, 'Stone 6', 3, 23),
	(22, 'Stone 7', 3, 24),
	(23, 'Stone 8', 3, 25),
	(24, 'Stone 9', 3, 26),
	(25, 'Stone 10', 3, 27),
	(26, 'Stone 11', 3, 28);
/*!40000 ALTER TABLE `TERRAIN_OBJECT` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
