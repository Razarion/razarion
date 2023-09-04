/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `BASE_ITEM_WEAPON_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attackRange` double NOT NULL,
  `damage` int(11) NOT NULL,
  `detonationRadius` double NOT NULL,
  `projectileSpeed` double DEFAULT NULL,
  `reloadTime` double NOT NULL,
  `projectileShape3D_id` int(11) DEFAULT NULL,
  `turretType_id` int(11) DEFAULT NULL,
  `detonationParticle_id` int(11) DEFAULT NULL,
  `muzzleFlashParticle_id` int(11) DEFAULT NULL,
  `muzzleFlashParticleSystem_id` int(11) DEFAULT NULL,
  `muzzleFlashAudioLibraryEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5rxijs3f390eu6xf3wetbkk8g` (`projectileShape3D_id`),
  KEY `FKpto1m2l3ire9y1mhptu2fve4x` (`turretType_id`),
  KEY `FKkh5sat7l282j5prbv0rdg3sxy` (`detonationParticle_id`),
  KEY `FK6borfwouuokg2nhxh8p9jykyc` (`muzzleFlashParticle_id`),
  KEY `FKen9g2vcx9g3vcbrct1nwuskkq` (`muzzleFlashParticleSystem_id`),
  KEY `FKs48judgcbr9n3hxvgcgvwtj21` (`muzzleFlashAudioLibraryEntity_id`),
  CONSTRAINT `FK5rxijs3f390eu6xf3wetbkk8g` FOREIGN KEY (`projectileShape3D_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FK6borfwouuokg2nhxh8p9jykyc` FOREIGN KEY (`muzzleFlashParticle_id`) REFERENCES `PARTICLE_EMITTER_SEQUENCE` (`id`),
  CONSTRAINT `FKen9g2vcx9g3vcbrct1nwuskkq` FOREIGN KEY (`muzzleFlashParticleSystem_id`) REFERENCES `PARTICLE_SYSTEM` (`id`),
  CONSTRAINT `FKkh5sat7l282j5prbv0rdg3sxy` FOREIGN KEY (`detonationParticle_id`) REFERENCES `PARTICLE_EMITTER_SEQUENCE` (`id`),
  CONSTRAINT `FKpto1m2l3ire9y1mhptu2fve4x` FOREIGN KEY (`turretType_id`) REFERENCES `BASE_ITEM_TURRET_TYPE` (`id`),
  CONSTRAINT `FKs48judgcbr9n3hxvgcgvwtj21` FOREIGN KEY (`muzzleFlashAudioLibraryEntity_id`) REFERENCES `AUDIO_LIBRARY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;

DELETE FROM `BASE_ITEM_WEAPON_TYPE`;
/*!40000 ALTER TABLE `BASE_ITEM_WEAPON_TYPE` DISABLE KEYS */;
INSERT INTO `BASE_ITEM_WEAPON_TYPE` (`id`, `attackRange`, `damage`, `detonationRadius`, `projectileSpeed`, `reloadTime`, `projectileShape3D_id`, `turretType_id`, `detonationParticle_id`, `muzzleFlashParticle_id`, `muzzleFlashParticleSystem_id`, `muzzleFlashAudioLibraryEntity_id`) VALUES
	(1, 10, 10, 1, 30, 1, 6, 1, 3, 4, 2, 10),
	(2, 20, 30, 2, 47, 2, 6, 2, 3, 4, NULL, NULL),
	(3, 15, 12, 1, 50, 1, 6, 3, 3, 4, NULL, NULL),
	(4, 15, 12, 1, 50, 1, 6, 4, 3, 4, NULL, NULL),
	(5, 10, 10, 1, 30, 1, 6, 5, 3, 4, 2, NULL),
	(6, 20, 20, 2, 50, 1, 6, 6, 3, 4, NULL, NULL),
	(7, 20, 30, 2, 47, 2, 6, 7, 3, 4, NULL, NULL);
/*!40000 ALTER TABLE `BASE_ITEM_WEAPON_TYPE` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
