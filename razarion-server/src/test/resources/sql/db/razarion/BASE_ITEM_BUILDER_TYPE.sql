/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `BASE_ITEM_BUILDER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animationOriginX` double DEFAULT NULL,
  `animationOriginY` double DEFAULT NULL,
  `animationOriginZ` double DEFAULT NULL,
  `buildRange` double NOT NULL,
  `progress` double NOT NULL,
  `animationShape3d_id` int(11) DEFAULT NULL,
  `animationParticle_id` int(11) DEFAULT NULL,
  `particleSystem_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqqlnkxi7kdlnc3f4ow78kfg5c` (`animationShape3d_id`),
  KEY `FK38qcc3plemueiufib1bff6179` (`animationParticle_id`),
  KEY `FKie47miqd7bn1numrbu86wm76r` (`particleSystem_id`),
  CONSTRAINT `FK38qcc3plemueiufib1bff6179` FOREIGN KEY (`animationParticle_id`) REFERENCES `PARTICLE_EMITTER_SEQUENCE` (`id`),
  CONSTRAINT `FKie47miqd7bn1numrbu86wm76r` FOREIGN KEY (`particleSystem_id`) REFERENCES `PARTICLE_SYSTEM` (`id`),
  CONSTRAINT `FKqqlnkxi7kdlnc3f4ow78kfg5c` FOREIGN KEY (`animationShape3d_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

DELETE FROM `BASE_ITEM_BUILDER_TYPE`;
/*!40000 ALTER TABLE `BASE_ITEM_BUILDER_TYPE` DISABLE KEYS */;
INSERT INTO `BASE_ITEM_BUILDER_TYPE` (`id`, `animationOriginX`, `animationOriginY`, `animationOriginZ`, `buildRange`, `progress`, `animationShape3d_id`, `animationParticle_id`, `particleSystem_id`) VALUES
	(1, 1.63196, 0, 3.04829, 2, 0.01, NULL, NULL, 1),
	(2, 1.63196, 0, 3.04829, 10, 1, NULL, 6, NULL);
/*!40000 ALTER TABLE `BASE_ITEM_BUILDER_TYPE` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
