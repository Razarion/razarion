/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `BASE_ITEM_HARVESTER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animationOriginX` double DEFAULT NULL,
  `animationOriginY` double DEFAULT NULL,
  `animationOriginZ` double DEFAULT NULL,
  `harvestRange` int(11) NOT NULL,
  `progress` double NOT NULL,
  `animationShape3d_id` int(11) DEFAULT NULL,
  `particleSystem_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKegmix24ey7e76un4erbrvfcqr` (`animationShape3d_id`),
  KEY `FKiexj0a0ihmary9s51akdqs7p0` (`particleSystem_id`),
  CONSTRAINT `FKegmix24ey7e76un4erbrvfcqr` FOREIGN KEY (`animationShape3d_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKiexj0a0ihmary9s51akdqs7p0` FOREIGN KEY (`particleSystem_id`) REFERENCES `PARTICLE_SYSTEM` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

DELETE FROM `BASE_ITEM_HARVESTER_TYPE`;
/*!40000 ALTER TABLE `BASE_ITEM_HARVESTER_TYPE` DISABLE KEYS */;
INSERT INTO `BASE_ITEM_HARVESTER_TYPE` (`id`, `animationOriginX`, `animationOriginY`, `animationOriginZ`, `harvestRange`, `progress`, `animationShape3d_id`, `particleSystem_id`) VALUES
	(1, 2.5, 0, 1.25, 2, 2, 18, 3),
	(2, 2.5, 0, 1.25, 3, 2, 18, NULL),
	(3, 2.5, 0, 1.25, 3, 2, 18, NULL);
/*!40000 ALTER TABLE `BASE_ITEM_HARVESTER_TYPE` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
