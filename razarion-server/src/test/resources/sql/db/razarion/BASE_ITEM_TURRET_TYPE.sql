/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `BASE_ITEM_TURRET_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `angleVelocity` double NOT NULL,
  `muzzlePositionX` double DEFAULT NULL,
  `muzzlePositionY` double DEFAULT NULL,
  `muzzlePositionZ` double DEFAULT NULL,
  `shape3dMaterialId` varchar(255) DEFAULT NULL,
  `torrentCenterX` double DEFAULT NULL,
  `torrentCenterY` double DEFAULT NULL,
  `torrentCenterZ` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;

DELETE FROM `BASE_ITEM_TURRET_TYPE`;
/*!40000 ALTER TABLE `BASE_ITEM_TURRET_TYPE` DISABLE KEYS */;
INSERT INTO `BASE_ITEM_TURRET_TYPE` (`id`, `angleVelocity`, `muzzlePositionX`, `muzzlePositionY`, `muzzlePositionZ`, `shape3dMaterialId`, `torrentCenterX`, `torrentCenterY`, `torrentCenterZ`) VALUES
	(1, 2.0943951023931953, 1.3, 0, 0, 'Turret-material', -0.25, 0, 2),
	(2, 2.6179938779914944, 1.33, 0, 1.07, 'TurretMat-material', 0, 0, 2),
	(3, 2.0943951023931953, 1.3, 0, 0, 'Turret_Mat-material', 1.35245, 0, 2.45898),
	(4, 2.0943951023931953, 1.3, 0, 0, 'Turret_Mat-material', 1.35245, 0, 2.45898),
	(5, 2.0943951023931953, 1.3, 0, 0, 'Turret-material', -0.25, 0, 2),
	(6, 2.6179938779914944, 1.8, 0, 0, 'Turret_Mat-material', -0.8, 0, 2.1),
	(7, 2.6179938779914944, 1.33, 0, 1.07, 'TurretMat-material', 0, 0, 2);
/*!40000 ALTER TABLE `BASE_ITEM_TURRET_TYPE` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
