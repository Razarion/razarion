-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server Version:               10.1.21-MariaDB - mariadb.org binary distribution
-- Server Betriebssystem:        Win64
-- HeidiSQL Version:             9.4.0.5125
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Exportiere Struktur von Tabelle razarion-test.AUDIO_LIBRARY
DROP TABLE IF EXISTS `AUDIO_LIBRARY`;
CREATE TABLE IF NOT EXISTS `AUDIO_LIBRARY` (
  `id` int(11) NOT NULL,
  `data` longblob,
  `internalName` varchar(255) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.AUDIO_LIBRARY: ~0 rows (ungefähr)
DELETE FROM `AUDIO_LIBRARY`;
/*!40000 ALTER TABLE `AUDIO_LIBRARY` DISABLE KEYS */;
/*!40000 ALTER TABLE `AUDIO_LIBRARY` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.BASE_ITEM_TYPE
DROP TABLE IF EXISTS `BASE_ITEM_TYPE`;
CREATE TABLE IF NOT EXISTS `BASE_ITEM_TYPE` (
  `id` int(11) NOT NULL,
  `health` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `spawnDurationMillis` int(11) NOT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `spawnShape3DId_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjjxk2oxiywyjc4mjhbn2esi5m` (`shape3DId_id`),
  KEY `FKsk0soitdh85fl3i9dbxu32hgp` (`spawnShape3DId_id`),
  CONSTRAINT `FKjjxk2oxiywyjc4mjhbn2esi5m` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKsk0soitdh85fl3i9dbxu32hgp` FOREIGN KEY (`spawnShape3DId_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.BASE_ITEM_TYPE: ~5 rows (ungefähr)
DELETE FROM `BASE_ITEM_TYPE`;
/*!40000 ALTER TABLE `BASE_ITEM_TYPE` DISABLE KEYS */;
INSERT INTO `BASE_ITEM_TYPE` (`id`, `health`, `name`, `radius`, `spawnDurationMillis`, `shape3DId_id`, `spawnShape3DId_id`) VALUES
	(180807, 5, 'Bulldoze', 3, 3000, 180810, 180806),
	(180830, 5, 'Harvester', 3, 3000, 272948, 180806),
	(180832, 5, 'Attacker', 2, 3000, 272496, 180806),
	(272490, 10, 'Factory', 6, 3000, 272488, 180806),
	(272495, 10, 'Tower', 4, 3000, 272492, 180806);
/*!40000 ALTER TABLE `BASE_ITEM_TYPE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.BOX_ITEM_TYPE
DROP TABLE IF EXISTS `BOX_ITEM_TYPE`;
CREATE TABLE IF NOT EXISTS `BOX_ITEM_TYPE` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9wtm3xgkwr25ttr1pubebcdc3` (`shape3DId_id`),
  CONSTRAINT `FK9wtm3xgkwr25ttr1pubebcdc3` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.BOX_ITEM_TYPE: ~0 rows (ungefähr)
DELETE FROM `BOX_ITEM_TYPE`;
/*!40000 ALTER TABLE `BOX_ITEM_TYPE` DISABLE KEYS */;
/*!40000 ALTER TABLE `BOX_ITEM_TYPE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.COLLADA
DROP TABLE IF EXISTS `COLLADA`;
CREATE TABLE IF NOT EXISTS `COLLADA` (
  `id` int(11) NOT NULL,
  `colladaString` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.COLLADA: ~0 rows (ungefähr)
DELETE FROM `COLLADA`;
/*!40000 ALTER TABLE `COLLADA` DISABLE KEYS */;
/*!40000 ALTER TABLE `COLLADA` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.COLLADA_ANIMATIONS
DROP TABLE IF EXISTS `COLLADA_ANIMATIONS`;
CREATE TABLE IF NOT EXISTS `COLLADA_ANIMATIONS` (
  `ColladaEntity_id` int(11) NOT NULL,
  `animations` varchar(255) DEFAULT NULL,
  `animations_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`animations_KEY`),
  CONSTRAINT `FKtk7g7rbkh3n2wrl77bafo4bhd` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.COLLADA_ANIMATIONS: ~0 rows (ungefähr)
DELETE FROM `COLLADA_ANIMATIONS`;
/*!40000 ALTER TABLE `COLLADA_ANIMATIONS` DISABLE KEYS */;
/*!40000 ALTER TABLE `COLLADA_ANIMATIONS` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.COLLADA_TEXTURES
DROP TABLE IF EXISTS `COLLADA_TEXTURES`;
CREATE TABLE IF NOT EXISTS `COLLADA_TEXTURES` (
  `ColladaEntity_id` int(11) NOT NULL,
  `textures_id` int(11) NOT NULL,
  `textures_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`textures_KEY`),
  KEY `FKgs74sss6ut5r7iqqbds1yc91w` (`textures_id`),
  CONSTRAINT `FK6ee9gdwsx3sm1fxpo8wkvwj8y` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKgs74sss6ut5r7iqqbds1yc91w` FOREIGN KEY (`textures_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.COLLADA_TEXTURES: ~0 rows (ungefähr)
DELETE FROM `COLLADA_TEXTURES`;
/*!40000 ALTER TABLE `COLLADA_TEXTURES` DISABLE KEYS */;
/*!40000 ALTER TABLE `COLLADA_TEXTURES` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.FB_MARKETING_CLICK_TRACKER
DROP TABLE IF EXISTS `FB_MARKETING_CLICK_TRACKER`;
CREATE TABLE IF NOT EXISTS `FB_MARKETING_CLICK_TRACKER` (
  `id` int(11) NOT NULL,
  `adId` varchar(255) DEFAULT NULL,
  `timeStamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.FB_MARKETING_CLICK_TRACKER: ~0 rows (ungefähr)
DELETE FROM `FB_MARKETING_CLICK_TRACKER`;
/*!40000 ALTER TABLE `FB_MARKETING_CLICK_TRACKER` DISABLE KEYS */;
/*!40000 ALTER TABLE `FB_MARKETING_CLICK_TRACKER` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.FB_MARKETING_CURRENT_AD
DROP TABLE IF EXISTS `FB_MARKETING_CURRENT_AD`;
CREATE TABLE IF NOT EXISTS `FB_MARKETING_CURRENT_AD` (
  `id` int(11) NOT NULL,
  `adId` bigint(20) NOT NULL,
  `adSetId` bigint(20) NOT NULL,
  `body` varchar(255) DEFAULT NULL,
  `campaignId` bigint(20) NOT NULL,
  `dateStart` datetime DEFAULT NULL,
  `dateStop` datetime DEFAULT NULL,
  `imageHash` varchar(255) DEFAULT NULL,
  `lifeTime` bit(1) NOT NULL,
  `scheduleTimeEnd` datetime DEFAULT NULL,
  `scheduleTimeStart` datetime DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `urlTagParam` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.FB_MARKETING_CURRENT_AD: ~0 rows (ungefähr)
DELETE FROM `FB_MARKETING_CURRENT_AD`;
/*!40000 ALTER TABLE `FB_MARKETING_CURRENT_AD` DISABLE KEYS */;
/*!40000 ALTER TABLE `FB_MARKETING_CURRENT_AD` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.FB_MARKETING_CURRENT_AD_INTEREST
DROP TABLE IF EXISTS `FB_MARKETING_CURRENT_AD_INTEREST`;
CREATE TABLE IF NOT EXISTS `FB_MARKETING_CURRENT_AD_INTEREST` (
  `currentAdEntityId` int(11) NOT NULL,
  `fbId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  KEY `FKnjtju67voai2g6udj66ueo9v0` (`currentAdEntityId`),
  CONSTRAINT `FKnjtju67voai2g6udj66ueo9v0` FOREIGN KEY (`currentAdEntityId`) REFERENCES `FB_MARKETING_CURRENT_AD` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.FB_MARKETING_CURRENT_AD_INTEREST: ~0 rows (ungefähr)
DELETE FROM `FB_MARKETING_CURRENT_AD_INTEREST`;
/*!40000 ALTER TABLE `FB_MARKETING_CURRENT_AD_INTEREST` DISABLE KEYS */;
/*!40000 ALTER TABLE `FB_MARKETING_CURRENT_AD_INTEREST` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.FB_MARKETING_HISTORY_AD
DROP TABLE IF EXISTS `FB_MARKETING_HISTORY_AD`;
CREATE TABLE IF NOT EXISTS `FB_MARKETING_HISTORY_AD` (
  `id` int(11) NOT NULL,
  `adId` bigint(20) NOT NULL,
  `adSetId` bigint(20) NOT NULL,
  `body` varchar(255) DEFAULT NULL,
  `campaignId` bigint(20) NOT NULL,
  `clicks` int(11) NOT NULL,
  `dateStart` datetime DEFAULT NULL,
  `dateStop` datetime DEFAULT NULL,
  `imageHash` varchar(255) DEFAULT NULL,
  `impressions` int(11) NOT NULL,
  `lifeTime` bit(1) NOT NULL,
  `scheduleTimeEnd` datetime DEFAULT NULL,
  `scheduleTimeStart` datetime DEFAULT NULL,
  `spent` double NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `urlTagParam` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.FB_MARKETING_HISTORY_AD: ~0 rows (ungefähr)
DELETE FROM `FB_MARKETING_HISTORY_AD`;
/*!40000 ALTER TABLE `FB_MARKETING_HISTORY_AD` DISABLE KEYS */;
/*!40000 ALTER TABLE `FB_MARKETING_HISTORY_AD` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.FB_MARKETING_HISTORY_AD_INTEREST
DROP TABLE IF EXISTS `FB_MARKETING_HISTORY_AD_INTEREST`;
CREATE TABLE IF NOT EXISTS `FB_MARKETING_HISTORY_AD_INTEREST` (
  `historyAdEntityId` int(11) NOT NULL,
  `fbId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  KEY `FKad1jynmyqvb71p3w5ftjsbaxa` (`historyAdEntityId`),
  CONSTRAINT `FKad1jynmyqvb71p3w5ftjsbaxa` FOREIGN KEY (`historyAdEntityId`) REFERENCES `FB_MARKETING_HISTORY_AD` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.FB_MARKETING_HISTORY_AD_INTEREST: ~0 rows (ungefähr)
DELETE FROM `FB_MARKETING_HISTORY_AD_INTEREST`;
/*!40000 ALTER TABLE `FB_MARKETING_HISTORY_AD_INTEREST` DISABLE KEYS */;
/*!40000 ALTER TABLE `FB_MARKETING_HISTORY_AD_INTEREST` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.GAME_UI_CONTROL_CONFIG
DROP TABLE IF EXISTS `GAME_UI_CONTROL_CONFIG`;
CREATE TABLE IF NOT EXISTS `GAME_UI_CONTROL_CONFIG` (
  `id` int(11) NOT NULL,
  `planetEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK60h9hy9lm12515cvw99w1oel7` (`planetEntity_id`),
  CONSTRAINT `FK60h9hy9lm12515cvw99w1oel7` FOREIGN KEY (`planetEntity_id`) REFERENCES `PLANET` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.GAME_UI_CONTROL_CONFIG: ~0 rows (ungefähr)
DELETE FROM `GAME_UI_CONTROL_CONFIG`;
/*!40000 ALTER TABLE `GAME_UI_CONTROL_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `GAME_UI_CONTROL_CONFIG` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.GROUND_CONFIG
DROP TABLE IF EXISTS `GROUND_CONFIG`;
CREATE TABLE IF NOT EXISTS `GROUND_CONFIG` (
  `id` int(11) NOT NULL,
  `bottomBmDepth` double NOT NULL,
  `bottomBmScale` double NOT NULL,
  `bottomTextureScale` double NOT NULL,
  `heightFractalClampMax` double NOT NULL,
  `heightFractalClampMin` double NOT NULL,
  `heightFractalMax` double NOT NULL,
  `heightFractalMin` double NOT NULL,
  `heightFractalRoughness` double NOT NULL,
  `heightXCount` int(11) NOT NULL,
  `heightYCount` int(11) NOT NULL,
  `ambientA` double NOT NULL,
  `ambientB` double NOT NULL,
  `ambientG` double NOT NULL,
  `ambientR` double NOT NULL,
  `diffuseA` double NOT NULL,
  `diffuseB` double NOT NULL,
  `diffuseG` double NOT NULL,
  `diffuseR` double NOT NULL,
  `specularHardness` double NOT NULL,
  `specularIntensity` double NOT NULL,
  `xRotation` double NOT NULL,
  `yRotation` double NOT NULL,
  `splattingFractalClampMax` double NOT NULL,
  `splattingFractalClampMin` double NOT NULL,
  `splattingFractalMax` double NOT NULL,
  `splattingFractalMin` double NOT NULL,
  `splattingFractalRoughness` double NOT NULL,
  `splattingScale` double NOT NULL,
  `splattingXCount` int(11) NOT NULL,
  `splattingYCount` int(11) NOT NULL,
  `topBmDepth` double NOT NULL,
  `topBmScale` double NOT NULL,
  `topTextureScale` double NOT NULL,
  `bottomBm_id` int(11) DEFAULT NULL,
  `bottomTexture_id` int(11) DEFAULT NULL,
  `splatting_id` int(11) DEFAULT NULL,
  `topBm_id` int(11) DEFAULT NULL,
  `topTexture_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhvf9wdroitf7l250s2si9ra8m` (`bottomBm_id`),
  KEY `FKncu4rmf1d597pn9nkbm40euxh` (`bottomTexture_id`),
  KEY `FK7f71xj1yqpgy5i93ueer11xxd` (`splatting_id`),
  KEY `FK6oo0mrnl6lekycg4msfe49ya8` (`topBm_id`),
  KEY `FKmvyhhqqxmwys0ttqjmnq9975o` (`topTexture_id`),
  CONSTRAINT `FK6oo0mrnl6lekycg4msfe49ya8` FOREIGN KEY (`topBm_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FK7f71xj1yqpgy5i93ueer11xxd` FOREIGN KEY (`splatting_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKhvf9wdroitf7l250s2si9ra8m` FOREIGN KEY (`bottomBm_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKmvyhhqqxmwys0ttqjmnq9975o` FOREIGN KEY (`topTexture_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKncu4rmf1d597pn9nkbm40euxh` FOREIGN KEY (`bottomTexture_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.GROUND_CONFIG: ~0 rows (ungefähr)
DELETE FROM `GROUND_CONFIG`;
/*!40000 ALTER TABLE `GROUND_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `GROUND_CONFIG` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.GROUND_HEIGHT
DROP TABLE IF EXISTS `GROUND_HEIGHT`;
CREATE TABLE IF NOT EXISTS `GROUND_HEIGHT` (
  `id` int(11) NOT NULL,
  `height` double NOT NULL,
  `xIndex` int(11) NOT NULL,
  `yIndex` int(11) NOT NULL,
  `heights_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3dauap09p29rwvdsiiwbs5hw9` (`heights_id`),
  CONSTRAINT `FK3dauap09p29rwvdsiiwbs5hw9` FOREIGN KEY (`heights_id`) REFERENCES `GROUND_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.GROUND_HEIGHT: ~0 rows (ungefähr)
DELETE FROM `GROUND_HEIGHT`;
/*!40000 ALTER TABLE `GROUND_HEIGHT` DISABLE KEYS */;
/*!40000 ALTER TABLE `GROUND_HEIGHT` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.GROUND_SPLATTING
DROP TABLE IF EXISTS `GROUND_SPLATTING`;
CREATE TABLE IF NOT EXISTS `GROUND_SPLATTING` (
  `id` int(11) NOT NULL,
  `splatting` double NOT NULL,
  `xIndex` int(11) NOT NULL,
  `yIndex` int(11) NOT NULL,
  `splattings_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKapp2bfps95kgm1y7oe2s5hp1y` (`splattings_id`),
  CONSTRAINT `FKapp2bfps95kgm1y7oe2s5hp1y` FOREIGN KEY (`splattings_id`) REFERENCES `GROUND_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.GROUND_SPLATTING: ~0 rows (ungefähr)
DELETE FROM `GROUND_SPLATTING`;
/*!40000 ALTER TABLE `GROUND_SPLATTING` DISABLE KEYS */;
/*!40000 ALTER TABLE `GROUND_SPLATTING` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.hibernate_sequence
DROP TABLE IF EXISTS `hibernate_sequence`;
CREATE TABLE IF NOT EXISTS `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.hibernate_sequence: ~31 rows (ungefähr)
DELETE FROM `hibernate_sequence`;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` (`next_val`) VALUES
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20),
	(20);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.HUMAN_PLAYER_ENTITY
DROP TABLE IF EXISTS `HUMAN_PLAYER_ENTITY`;
CREATE TABLE IF NOT EXISTS `HUMAN_PLAYER_ENTITY` (
  `id` int(11) NOT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.HUMAN_PLAYER_ENTITY: ~1 rows (ungefähr)
DELETE FROM `HUMAN_PLAYER_ENTITY`;
/*!40000 ALTER TABLE `HUMAN_PLAYER_ENTITY` DISABLE KEYS */;
/*!40000 ALTER TABLE `HUMAN_PLAYER_ENTITY` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.IMAGE_LIBRARY
DROP TABLE IF EXISTS `IMAGE_LIBRARY`;
CREATE TABLE IF NOT EXISTS `IMAGE_LIBRARY` (
  `id` int(11) NOT NULL,
  `data` longblob,
  `internalName` varchar(255) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.IMAGE_LIBRARY: ~0 rows (ungefähr)
DELETE FROM `IMAGE_LIBRARY`;
/*!40000 ALTER TABLE `IMAGE_LIBRARY` DISABLE KEYS */;
/*!40000 ALTER TABLE `IMAGE_LIBRARY` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.LEVEL
DROP TABLE IF EXISTS `LEVEL`;
CREATE TABLE IF NOT EXISTS `LEVEL` (
  `id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `xp2LevelUp` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.LEVEL: ~4 rows (ungefähr)
DELETE FROM `LEVEL`;
/*!40000 ALTER TABLE `LEVEL` DISABLE KEYS */;
/*!40000 ALTER TABLE `LEVEL` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.LEVEL_LIMITATION
DROP TABLE IF EXISTS `LEVEL_LIMITATION`;
CREATE TABLE IF NOT EXISTS `LEVEL_LIMITATION` (
  `LevelEntity_id` int(11) NOT NULL,
  `itemTypeLimitation` int(11) DEFAULT NULL,
  `baseItemTypeEntityId` int(11) NOT NULL,
  PRIMARY KEY (`LevelEntity_id`,`baseItemTypeEntityId`),
  KEY `FKiuvkgev9cw8g39baap1oyqhj4` (`baseItemTypeEntityId`),
  CONSTRAINT `FKip53iy42wu39wx0f7brf2cic2` FOREIGN KEY (`LevelEntity_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKiuvkgev9cw8g39baap1oyqhj4` FOREIGN KEY (`baseItemTypeEntityId`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.LEVEL_LIMITATION: ~12 rows (ungefähr)
DELETE FROM `LEVEL_LIMITATION`;
/*!40000 ALTER TABLE `LEVEL_LIMITATION` DISABLE KEYS */;
/*!40000 ALTER TABLE `LEVEL_LIMITATION` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.PLANET
DROP TABLE IF EXISTS `PLANET`;
CREATE TABLE IF NOT EXISTS `PLANET` (
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.PLANET: ~0 rows (ungefähr)
DELETE FROM `PLANET`;
/*!40000 ALTER TABLE `PLANET` DISABLE KEYS */;
/*!40000 ALTER TABLE `PLANET` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.RESOURCE_ITEM_TYPE
DROP TABLE IF EXISTS `RESOURCE_ITEM_TYPE`;
CREATE TABLE IF NOT EXISTS `RESOURCE_ITEM_TYPE` (
  `id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsxtu46oir6k4rqq8em4jcxc7j` (`shape3DId_id`),
  CONSTRAINT `FKsxtu46oir6k4rqq8em4jcxc7j` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.RESOURCE_ITEM_TYPE: ~0 rows (ungefähr)
DELETE FROM `RESOURCE_ITEM_TYPE`;
/*!40000 ALTER TABLE `RESOURCE_ITEM_TYPE` DISABLE KEYS */;
/*!40000 ALTER TABLE `RESOURCE_ITEM_TYPE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.SCENE
DROP TABLE IF EXISTS `SCENE`;
CREATE TABLE IF NOT EXISTS `SCENE` (
  `id` int(11) NOT NULL,
  `cameraConfigCameraLocked` bit(1) NOT NULL,
  `cameraConfigFromPositionX` int(11) DEFAULT NULL,
  `cameraConfigFromPositionY` int(11) DEFAULT NULL,
  `cameraConfigSmooth` bit(1) NOT NULL,
  `cameraConfigToPositionX` int(11) DEFAULT NULL,
  `cameraConfigToPositionY` int(11) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `introText` varchar(255) DEFAULT NULL,
  `showQuestSideBar` bit(1) NOT NULL,
  `scenes_id` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKllu905kjsa9p2g5t6tsx517ha` (`scenes_id`),
  CONSTRAINT `FKllu905kjsa9p2g5t6tsx517ha` FOREIGN KEY (`scenes_id`) REFERENCES `GAME_UI_CONTROL_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.SCENE: ~0 rows (ungefähr)
DELETE FROM `SCENE`;
/*!40000 ALTER TABLE `SCENE` DISABLE KEYS */;
/*!40000 ALTER TABLE `SCENE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.SLOPE_CONFIG
DROP TABLE IF EXISTS `SLOPE_CONFIG`;
CREATE TABLE IF NOT EXISTS `SLOPE_CONFIG` (
  `id` int(11) NOT NULL,
  `bmDepth` double NOT NULL,
  `bmScale` double NOT NULL,
  `fractalClampMax` double NOT NULL,
  `fractalClampMin` double NOT NULL,
  `fractalMax` double NOT NULL,
  `fractalMin` double NOT NULL,
  `fractalRoughness` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `ambientA` double NOT NULL,
  `ambientB` double NOT NULL,
  `ambientG` double NOT NULL,
  `ambientR` double NOT NULL,
  `diffuseA` double NOT NULL,
  `diffuseB` double NOT NULL,
  `diffuseG` double NOT NULL,
  `diffuseR` double NOT NULL,
  `specularHardness` double NOT NULL,
  `specularIntensity` double NOT NULL,
  `xRotation` double NOT NULL,
  `yRotation` double NOT NULL,
  `segments` int(11) NOT NULL,
  `slopeOriented` bit(1) NOT NULL,
  `textureScale` double NOT NULL,
  `type` varchar(255) NOT NULL,
  `verticalSpace` double NOT NULL,
  `bm_id` int(11) DEFAULT NULL,
  `texture_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh5mpq9b19cf1ielcuicbjn9bq` (`bm_id`),
  KEY `FK52qctp83p38492e8u9ihp2at` (`texture_id`),
  CONSTRAINT `FK52qctp83p38492e8u9ihp2at` FOREIGN KEY (`texture_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKh5mpq9b19cf1ielcuicbjn9bq` FOREIGN KEY (`bm_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.SLOPE_CONFIG: ~0 rows (ungefähr)
DELETE FROM `SLOPE_CONFIG`;
/*!40000 ALTER TABLE `SLOPE_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `SLOPE_CONFIG` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.SLOPE_NODE
DROP TABLE IF EXISTS `SLOPE_NODE`;
CREATE TABLE IF NOT EXISTS `SLOPE_NODE` (
  `id` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `z` double NOT NULL,
  `rowIndex` int(11) NOT NULL,
  `segmentIndex` int(11) NOT NULL,
  `slopeFactor` double NOT NULL,
  `slopeSkeletonEntries_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKov73tk3lfw0vb6xj812610xkq` (`slopeSkeletonEntries_id`),
  CONSTRAINT `FKov73tk3lfw0vb6xj812610xkq` FOREIGN KEY (`slopeSkeletonEntries_id`) REFERENCES `SLOPE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.SLOPE_NODE: ~0 rows (ungefähr)
DELETE FROM `SLOPE_NODE`;
/*!40000 ALTER TABLE `SLOPE_NODE` DISABLE KEYS */;
/*!40000 ALTER TABLE `SLOPE_NODE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.SLOPE_SHAPE
DROP TABLE IF EXISTS `SLOPE_SHAPE`;
CREATE TABLE IF NOT EXISTS `SLOPE_SHAPE` (
  `id` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `slopeFactor` float NOT NULL,
  `shape_id` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKla78vv4yhnh8nh0hn60yj5ite` (`shape_id`),
  CONSTRAINT `FKla78vv4yhnh8nh0hn60yj5ite` FOREIGN KEY (`shape_id`) REFERENCES `SLOPE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.SLOPE_SHAPE: ~0 rows (ungefähr)
DELETE FROM `SLOPE_SHAPE`;
/*!40000 ALTER TABLE `SLOPE_SHAPE` DISABLE KEYS */;
/*!40000 ALTER TABLE `SLOPE_SHAPE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TERRAIN_OBJECT
DROP TABLE IF EXISTS `TERRAIN_OBJECT`;
CREATE TABLE IF NOT EXISTS `TERRAIN_OBJECT` (
  `id` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `colladaEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd2dgp25xa48e7tpah4gc4jk00` (`colladaEntity_id`),
  CONSTRAINT `FKd2dgp25xa48e7tpah4gc4jk00` FOREIGN KEY (`colladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TERRAIN_OBJECT: ~0 rows (ungefähr)
DELETE FROM `TERRAIN_OBJECT`;
/*!40000 ALTER TABLE `TERRAIN_OBJECT` DISABLE KEYS */;
/*!40000 ALTER TABLE `TERRAIN_OBJECT` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TERRAIN_OBJECT_POSITION
DROP TABLE IF EXISTS `TERRAIN_OBJECT_POSITION`;
CREATE TABLE IF NOT EXISTS `TERRAIN_OBJECT_POSITION` (
  `id` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `rotationZ` double NOT NULL,
  `scale` double NOT NULL,
  `terrainObjectEntity_id` int(11) NOT NULL,
  `terrainObjectPositionEntities_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr6v0ku0sca2p8a2e0oiac4g8f` (`terrainObjectEntity_id`),
  KEY `FKh4j4h0mtiir44uxusqetshoif` (`terrainObjectPositionEntities_id`),
  CONSTRAINT `FKh4j4h0mtiir44uxusqetshoif` FOREIGN KEY (`terrainObjectPositionEntities_id`) REFERENCES `PLANET` (`id`),
  CONSTRAINT `FKr6v0ku0sca2p8a2e0oiac4g8f` FOREIGN KEY (`terrainObjectEntity_id`) REFERENCES `TERRAIN_OBJECT` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TERRAIN_OBJECT_POSITION: ~0 rows (ungefähr)
DELETE FROM `TERRAIN_OBJECT_POSITION`;
/*!40000 ALTER TABLE `TERRAIN_OBJECT_POSITION` DISABLE KEYS */;
/*!40000 ALTER TABLE `TERRAIN_OBJECT_POSITION` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TERRAIN_SLOPE_POSITION
DROP TABLE IF EXISTS `TERRAIN_SLOPE_POSITION`;
CREATE TABLE IF NOT EXISTS `TERRAIN_SLOPE_POSITION` (
  `id` int(11) NOT NULL,
  `slopeConfigEntity_id` int(11) NOT NULL,
  `terrainSlopePositionEntities_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4xqtrjsn0gheqg5og8e9s5mmt` (`slopeConfigEntity_id`),
  KEY `FKsmu5f67wgca4q3h5b09o1qqni` (`terrainSlopePositionEntities_id`),
  CONSTRAINT `FK4xqtrjsn0gheqg5og8e9s5mmt` FOREIGN KEY (`slopeConfigEntity_id`) REFERENCES `SLOPE_CONFIG` (`id`),
  CONSTRAINT `FKsmu5f67wgca4q3h5b09o1qqni` FOREIGN KEY (`terrainSlopePositionEntities_id`) REFERENCES `PLANET` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TERRAIN_SLOPE_POSITION: ~0 rows (ungefähr)
DELETE FROM `TERRAIN_SLOPE_POSITION`;
/*!40000 ALTER TABLE `TERRAIN_SLOPE_POSITION` DISABLE KEYS */;
/*!40000 ALTER TABLE `TERRAIN_SLOPE_POSITION` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TERRAIN_SLOPE_POSITION_POLYGON
DROP TABLE IF EXISTS `TERRAIN_SLOPE_POSITION_POLYGON`;
CREATE TABLE IF NOT EXISTS `TERRAIN_SLOPE_POSITION_POLYGON` (
  `OWNER_ID` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`OWNER_ID`,`orderColumn`),
  CONSTRAINT `FKt9xdrjwg1xea1kp882nvg4cse` FOREIGN KEY (`OWNER_ID`) REFERENCES `TERRAIN_SLOPE_POSITION` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TERRAIN_SLOPE_POSITION_POLYGON: ~0 rows (ungefähr)
DELETE FROM `TERRAIN_SLOPE_POSITION_POLYGON`;
/*!40000 ALTER TABLE `TERRAIN_SLOPE_POSITION_POLYGON` DISABLE KEYS */;
/*!40000 ALTER TABLE `TERRAIN_SLOPE_POSITION_POLYGON` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_GAME_UI_CONTROL
DROP TABLE IF EXISTS `TRACKER_GAME_UI_CONTROL`;
CREATE TABLE IF NOT EXISTS `TRACKER_GAME_UI_CONTROL` (
  `id` int(11) NOT NULL,
  `clientStartTime` datetime DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXe1u611bhlj3y6j7lycxdvqp0p` (`sessionId`),
  KEY `IDXm23hwcquflqt4qknm1kdyi6g4` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_GAME_UI_CONTROL: ~0 rows (ungefähr)
DELETE FROM `TRACKER_GAME_UI_CONTROL`;
/*!40000 ALTER TABLE `TRACKER_GAME_UI_CONTROL` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_GAME_UI_CONTROL` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_PAGE
DROP TABLE IF EXISTS `TRACKER_PAGE`;
CREATE TABLE IF NOT EXISTS `TRACKER_PAGE` (
  `id` int(11) NOT NULL,
  `page` varchar(255) DEFAULT NULL,
  `params` longtext,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime NOT NULL,
  `uri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX8r3ssjnqi1ayq740sslge33bl` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_PAGE: ~0 rows (ungefähr)
DELETE FROM `TRACKER_PAGE`;
/*!40000 ALTER TABLE `TRACKER_PAGE` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_PAGE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_PERFMON
DROP TABLE IF EXISTS `TRACKER_PERFMON`;
CREATE TABLE IF NOT EXISTS `TRACKER_PERFMON` (
  `id` int(11) NOT NULL,
  `clientTimeStamp` datetime DEFAULT NULL,
  `perfmonEnum` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXjerhb27210brxagvh1igum3oa` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_PERFMON: ~0 rows (ungefähr)
DELETE FROM `TRACKER_PERFMON`;
/*!40000 ALTER TABLE `TRACKER_PERFMON` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_PERFMON` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_PERFMON_DURATION
DROP TABLE IF EXISTS `TRACKER_PERFMON_DURATION`;
CREATE TABLE IF NOT EXISTS `TRACKER_PERFMON_DURATION` (
  `perfmonStatisticEntityId` int(11) NOT NULL,
  `avgDuration` double DEFAULT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`perfmonStatisticEntityId`,`orderColumn`),
  CONSTRAINT `FKiobm435fm8fyloi8o21d82hp2` FOREIGN KEY (`perfmonStatisticEntityId`) REFERENCES `TRACKER_PERFMON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_PERFMON_DURATION: ~0 rows (ungefähr)
DELETE FROM `TRACKER_PERFMON_DURATION`;
/*!40000 ALTER TABLE `TRACKER_PERFMON_DURATION` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_PERFMON_DURATION` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_PERFMON_FREQUENCY
DROP TABLE IF EXISTS `TRACKER_PERFMON_FREQUENCY`;
CREATE TABLE IF NOT EXISTS `TRACKER_PERFMON_FREQUENCY` (
  `perfmonStatisticEntityId` int(11) NOT NULL,
  `frequency` double DEFAULT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`perfmonStatisticEntityId`,`orderColumn`),
  CONSTRAINT `FKpax2mb22ot360o7gra2tdpcqc` FOREIGN KEY (`perfmonStatisticEntityId`) REFERENCES `TRACKER_PERFMON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_PERFMON_FREQUENCY: ~0 rows (ungefähr)
DELETE FROM `TRACKER_PERFMON_FREQUENCY`;
/*!40000 ALTER TABLE `TRACKER_PERFMON_FREQUENCY` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_PERFMON_FREQUENCY` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_SCENE
DROP TABLE IF EXISTS `TRACKER_SCENE`;
CREATE TABLE IF NOT EXISTS `TRACKER_SCENE` (
  `id` int(11) NOT NULL,
  `clientStartTime` datetime DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX9o1helnck9k2whan5epnp63mp` (`sessionId`),
  KEY `IDX2l3oymcscbqbf34mgmmqqito5` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_SCENE: ~0 rows (ungefähr)
DELETE FROM `TRACKER_SCENE`;
/*!40000 ALTER TABLE `TRACKER_SCENE` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_SCENE` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_SESSION
DROP TABLE IF EXISTS `TRACKER_SESSION`;
CREATE TABLE IF NOT EXISTS `TRACKER_SESSION` (
  `id` int(11) NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `referer` longtext,
  `remoteAddr` varchar(255) DEFAULT NULL,
  `remoteHost` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime NOT NULL,
  `userAgent` longtext,
  PRIMARY KEY (`id`),
  KEY `IDXtmu9rd272j5lofhd2c8rh6fxh` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_SESSION: ~2 rows (ungefähr)
DELETE FROM `TRACKER_SESSION`;
/*!40000 ALTER TABLE `TRACKER_SESSION` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_SESSION` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_STARTUP_TASK
DROP TABLE IF EXISTS `TRACKER_STARTUP_TASK`;
CREATE TABLE IF NOT EXISTS `TRACKER_STARTUP_TASK` (
  `id` int(11) NOT NULL,
  `clientStartTime` datetime DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `error` longtext,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `startTime` datetime NOT NULL,
  `taskEnum` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXmnp841oq89d322wfkc062dlf5` (`sessionId`),
  KEY `IDXd87lfgp8dxx91d6d0mvp4xri8` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_STARTUP_TASK: ~0 rows (ungefähr)
DELETE FROM `TRACKER_STARTUP_TASK`;
/*!40000 ALTER TABLE `TRACKER_STARTUP_TASK` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_STARTUP_TASK` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.TRACKER_STARTUP_TERMINATED
DROP TABLE IF EXISTS `TRACKER_STARTUP_TERMINATED`;
CREATE TABLE IF NOT EXISTS `TRACKER_STARTUP_TERMINATED` (
  `id` int(11) NOT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `successful` bit(1) NOT NULL,
  `timeStamp` datetime DEFAULT NULL,
  `totalTime` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXe0iw0eq6smtw9ttdsctibxdrk` (`sessionId`),
  KEY `IDXp06cndke1flthaiq26ruywo52` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.TRACKER_STARTUP_TERMINATED: ~0 rows (ungefähr)
DELETE FROM `TRACKER_STARTUP_TERMINATED`;
/*!40000 ALTER TABLE `TRACKER_STARTUP_TERMINATED` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRACKER_STARTUP_TERMINATED` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle razarion-test.USER
DROP TABLE IF EXISTS `USER`;
CREATE TABLE IF NOT EXISTS `USER` (
  `id` int(11) NOT NULL,
  `admin` bit(1) NOT NULL,
  `facebookUserId` varchar(190) DEFAULT NULL,
  `levelId` int(11) NOT NULL,
  `registerDate` datetime DEFAULT NULL,
  `humanPlayerIdEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXoj5g1ob8tb6gn928mukpbqat1` (`facebookUserId`),
  KEY `FKdvrsr9xke3jgbsxgaarr9cica` (`humanPlayerIdEntity_id`),
  CONSTRAINT `FKdvrsr9xke3jgbsxgaarr9cica` FOREIGN KEY (`humanPlayerIdEntity_id`) REFERENCES `HUMAN_PLAYER_ENTITY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle razarion-test.USER: ~1 rows (ungefähr)
DELETE FROM `USER`;
/*!40000 ALTER TABLE `USER` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
