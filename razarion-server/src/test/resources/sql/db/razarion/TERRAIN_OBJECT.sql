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
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4;

DELETE FROM `TERRAIN_OBJECT`;
/*!40000 ALTER TABLE `TERRAIN_OBJECT` DISABLE KEYS */;
INSERT INTO `TERRAIN_OBJECT` (`id`, `internalName`, `radius`, `threeJsModelPackConfig_id`) VALUES
	(8, 'Rock1_grup2', 0, 82),
	(9, 'fern', 1.3, 108),
	(10, 'fern_small', 0.6, 109),
	(11, 'tropical_plant', 1.6, 116),
	(12, 'palm_tree2', 0.4, 114),
	(13, 'palm_tree', 0.5, 112),
	(14, 'tropical_fern', 3, 115),
	(15, 'Rock1_grup4', 2.1, 84),
	(16, 'Rock1D', 1, 85),
	(17, 'Rock1C', 1.1, 86),
	(19, 'Rock1A', 3, 87),
	(20, 'Rock1B', 1.9, 88),
	(21, 'Rock1_grup5', 3, 89),
	(22, 'Rock1E', 3, 90),
	(23, 'Rock1LOD_grup5', 3, 91),
	(24, 'Rock1LOD_grup4', 3, 92),
	(25, 'Rock1LOD_grup3', 3, 94),
	(26, 'Rock2', 3, 96),
	(27, 'Rock4B', 1, 97),
	(28, 'Rock5A', 1, 98),
	(29, 'Rock3', 1, 99),
	(30, 'Rock5B', 1, 100),
	(31, 'Rock4A', 1, 101),
	(32, 'Rock6A', 5.4, 102),
	(33, 'Rock6C', 1, 103),
	(34, 'Rock6B', 1, 104),
	(42, 'banana_plant', 0.6, 105),
	(43, 'banana_plant_small', 1, 107),
	(44, 'palm_bush', 1, 110);
/*!40000 ALTER TABLE `TERRAIN_OBJECT` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
