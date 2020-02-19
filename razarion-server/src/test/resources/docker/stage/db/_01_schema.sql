-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server Version:               10.4.11-MariaDB-1:10.4.11+maria~bionic - mariadb.org binary distribution
-- Server Betriebssystem:        debian-linux-gnu
-- HeidiSQL Version:             9.5.0.5196
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Exportiere Datenbank Struktur für razarion
CREATE DATABASE IF NOT EXISTS `razarion` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `razarion`;

-- Exportiere Struktur von Tabelle razarion.AUDIO_LIBRARY
CREATE TABLE IF NOT EXISTS `AUDIO_LIBRARY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` longblob DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_BUILDER_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_BUILDER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animationOriginX` double DEFAULT NULL,
  `animationOriginY` double DEFAULT NULL,
  `animationOriginZ` double DEFAULT NULL,
  `buildRange` double NOT NULL,
  `progress` double NOT NULL,
  `animationShape3d_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqqlnkxi7kdlnc3f4ow78kfg5c` (`animationShape3d_id`),
  CONSTRAINT `FKqqlnkxi7kdlnc3f4ow78kfg5c` FOREIGN KEY (`animationShape3d_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD
CREATE TABLE IF NOT EXISTS `BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD` (
  `builder` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_iilyj6hka733l0r6fe21q3imu` (`baseItemType`),
  KEY `FK28k6bigopln9kqkqspxgf45wo` (`builder`),
  CONSTRAINT `FK28k6bigopln9kqkqspxgf45wo` FOREIGN KEY (`builder`) REFERENCES `BASE_ITEM_BUILDER_TYPE` (`id`),
  CONSTRAINT `FKt42sflt3ub3tu41rcqgysx4h5` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_CONSUMER_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_CONSUMER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `wattage` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_DEMOLITION_STEP_EFFECT
CREATE TABLE IF NOT EXISTS `BASE_ITEM_DEMOLITION_STEP_EFFECT` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baseItemType` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm64k2tg9gg8iwgrckwb6i3o07` (`baseItemType`),
  CONSTRAINT `FKm64k2tg9gg8iwgrckwb6i3o07` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_DEMOLITION_STEP_EFFECT_PARTICLE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_DEMOLITION_STEP_EFFECT_PARTICLE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `particleConfigId_TMP` int(11) DEFAULT NULL,
  `positionX` double DEFAULT NULL,
  `positionY` double DEFAULT NULL,
  `positionZ` double DEFAULT NULL,
  `demolitionStepEffect` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5y84j4v1h2w5xe21gmu4vda0k` (`demolitionStepEffect`),
  CONSTRAINT `FK5y84j4v1h2w5xe21gmu4vda0k` FOREIGN KEY (`demolitionStepEffect`) REFERENCES `BASE_ITEM_DEMOLITION_STEP_EFFECT` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_FACTORY_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_FACTORY_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `progress` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_FACTORY_TYPE_ABLE_TO_BUILD
CREATE TABLE IF NOT EXISTS `BASE_ITEM_FACTORY_TYPE_ABLE_TO_BUILD` (
  `factory` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_s56cdvc3hq6y8o7r2gsrfu52c` (`baseItemType`),
  KEY `FKtc6hh437se4nqa2g9tmmg4na5` (`factory`),
  CONSTRAINT `FKlfyjj2uwgsatdw7ux1wds5t7x` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKtc6hh437se4nqa2g9tmmg4na5` FOREIGN KEY (`factory`) REFERENCES `BASE_ITEM_FACTORY_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_GENERATOR_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_GENERATOR_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `wattage` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_HARVESTER_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_HARVESTER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animationOriginX` double DEFAULT NULL,
  `animationOriginY` double DEFAULT NULL,
  `animationOriginZ` double DEFAULT NULL,
  `harvestRange` int(11) NOT NULL,
  `progress` double NOT NULL,
  `animationShape3d_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKegmix24ey7e76un4erbrvfcqr` (`animationShape3d_id`),
  CONSTRAINT `FKegmix24ey7e76un4erbrvfcqr` FOREIGN KEY (`animationShape3d_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_HOUSE_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_HOUSE_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_ITEM_CONTAINER_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_ITEM_CONTAINER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `itemRange` double NOT NULL,
  `maxCount` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN
CREATE TABLE IF NOT EXISTS `BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN` (
  `container` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_bdxnra184ggd8if0dqio7px22` (`baseItemType`),
  KEY `FK2u6acl8c6po7hq1ejtifgrlm1` (`container`),
  CONSTRAINT `FK2u6acl8c6po7hq1ejtifgrlm1` FOREIGN KEY (`container`) REFERENCES `BASE_ITEM_ITEM_CONTAINER_TYPE` (`id`),
  CONSTRAINT `FKgmiiow8xi819knlixvlf29we5` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_SPECIAL_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_SPECIAL_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `miniTerrain` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_TURRET_TYPE
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acceleration` double DEFAULT NULL,
  `angularVelocity` double DEFAULT NULL,
  `boxPickupRange` double NOT NULL,
  `buildup` int(11) NOT NULL,
  `dropBoxPossibility` double NOT NULL,
  `explosionParticleConfigId_TMP` int(11) DEFAULT NULL,
  `fixVerticalNorm` bit(1) NOT NULL,
  `health` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `price` int(11) NOT NULL,
  `radius` double NOT NULL,
  `spawnDurationMillis` int(11) NOT NULL,
  `speed` double DEFAULT NULL,
  `terrainType` varchar(255) DEFAULT NULL,
  `unlockCrystals` int(11) DEFAULT NULL,
  `xpOnKilling` int(11) NOT NULL,
  `builderType_id` int(11) DEFAULT NULL,
  `buildupTexture_id` int(11) DEFAULT NULL,
  `consumerType_id` int(11) DEFAULT NULL,
  `demolitionImage_id` int(11) DEFAULT NULL,
  `dropBoxItemTypeEntity_id` int(11) DEFAULT NULL,
  `factoryType_id` int(11) DEFAULT NULL,
  `generatorType_id` int(11) DEFAULT NULL,
  `harvesterType_id` int(11) DEFAULT NULL,
  `houseType_id` int(11) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `itemContainerType_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `spawnAudio_id` int(11) DEFAULT NULL,
  `spawnShape3DId_id` int(11) DEFAULT NULL,
  `specialType_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  `weaponType_id` int(11) DEFAULT NULL,
  `wreckageShape3D_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm7s2waq0lu64mpcrkd9jk521r` (`builderType_id`),
  KEY `FKtfr9577qfkq3ejl5gy2tbs1c0` (`buildupTexture_id`),
  KEY `FKewwopla27f0l5uuo7c111c6vx` (`consumerType_id`),
  KEY `FK5stwq1bmxsf4gfbax7tt1c1g5` (`demolitionImage_id`),
  KEY `FKsgd0elhjg8fo41g1tcjxq4390` (`dropBoxItemTypeEntity_id`),
  KEY `FKma6w4ce90clwtx027b80i3tae` (`factoryType_id`),
  KEY `FK7hhfbb8cnjrhyul76r05u8cdo` (`generatorType_id`),
  KEY `FKtj1i80eadv6cr8ydynl1kc39s` (`harvesterType_id`),
  KEY `FK5popx7pvso0sp3jdqhj1lwutn` (`houseType_id`),
  KEY `FKqdr3dh30y7eaywu60npthmh1i` (`i18nDescription_id`),
  KEY `FKdc6o3q4w1ni0a5hclsdq068u7` (`i18nName_id`),
  KEY `FKhm9xqu5a5dpv41x44vow0no6p` (`itemContainerType_id`),
  KEY `FKjjxk2oxiywyjc4mjhbn2esi5m` (`shape3DId_id`),
  KEY `FK2fd7inmdsi02vom6uk8nr179h` (`spawnAudio_id`),
  KEY `FKsk0soitdh85fl3i9dbxu32hgp` (`spawnShape3DId_id`),
  KEY `FKtog0l4xkcmxs3pgybiph2v4a9` (`specialType_id`),
  KEY `FKcdywk1gvxhj7ph9vjjltltqoc` (`thumbnail_id`),
  KEY `FKm18curnkip5ya1dnsngfe0amv` (`weaponType_id`),
  KEY `FKpfw7lsvxtvnuwy6gj6u09wclc` (`wreckageShape3D_id`),
  CONSTRAINT `FK2fd7inmdsi02vom6uk8nr179h` FOREIGN KEY (`spawnAudio_id`) REFERENCES `AUDIO_LIBRARY` (`id`),
  CONSTRAINT `FK5popx7pvso0sp3jdqhj1lwutn` FOREIGN KEY (`houseType_id`) REFERENCES `BASE_ITEM_HOUSE_TYPE` (`id`),
  CONSTRAINT `FK5stwq1bmxsf4gfbax7tt1c1g5` FOREIGN KEY (`demolitionImage_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FK7hhfbb8cnjrhyul76r05u8cdo` FOREIGN KEY (`generatorType_id`) REFERENCES `BASE_ITEM_GENERATOR_TYPE` (`id`),
  CONSTRAINT `FKcdywk1gvxhj7ph9vjjltltqoc` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKdc6o3q4w1ni0a5hclsdq068u7` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKewwopla27f0l5uuo7c111c6vx` FOREIGN KEY (`consumerType_id`) REFERENCES `BASE_ITEM_CONSUMER_TYPE` (`id`),
  CONSTRAINT `FKhm9xqu5a5dpv41x44vow0no6p` FOREIGN KEY (`itemContainerType_id`) REFERENCES `BASE_ITEM_ITEM_CONTAINER_TYPE` (`id`),
  CONSTRAINT `FKjjxk2oxiywyjc4mjhbn2esi5m` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKm18curnkip5ya1dnsngfe0amv` FOREIGN KEY (`weaponType_id`) REFERENCES `BASE_ITEM_WEAPON_TYPE` (`id`),
  CONSTRAINT `FKm7s2waq0lu64mpcrkd9jk521r` FOREIGN KEY (`builderType_id`) REFERENCES `BASE_ITEM_BUILDER_TYPE` (`id`),
  CONSTRAINT `FKma6w4ce90clwtx027b80i3tae` FOREIGN KEY (`factoryType_id`) REFERENCES `BASE_ITEM_FACTORY_TYPE` (`id`),
  CONSTRAINT `FKpfw7lsvxtvnuwy6gj6u09wclc` FOREIGN KEY (`wreckageShape3D_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKqdr3dh30y7eaywu60npthmh1i` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKsgd0elhjg8fo41g1tcjxq4390` FOREIGN KEY (`dropBoxItemTypeEntity_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FKsk0soitdh85fl3i9dbxu32hgp` FOREIGN KEY (`spawnShape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKtfr9577qfkq3ejl5gy2tbs1c0` FOREIGN KEY (`buildupTexture_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKtj1i80eadv6cr8ydynl1kc39s` FOREIGN KEY (`harvesterType_id`) REFERENCES `BASE_ITEM_HARVESTER_TYPE` (`id`),
  CONSTRAINT `FKtog0l4xkcmxs3pgybiph2v4a9` FOREIGN KEY (`specialType_id`) REFERENCES `BASE_ITEM_SPECIAL_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_WEAPON_TYPE
CREATE TABLE IF NOT EXISTS `BASE_ITEM_WEAPON_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attackRange` double NOT NULL,
  `damage` int(11) NOT NULL,
  `detonationParticleConfigId_TMP` int(11) DEFAULT NULL,
  `detonationRadius` double NOT NULL,
  `muzzleFlashParticleConfigId_TMP` int(11) DEFAULT NULL,
  `projectileSpeed` double DEFAULT NULL,
  `reloadTime` double NOT NULL,
  `projectileShape3D_id` int(11) DEFAULT NULL,
  `turretType_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5rxijs3f390eu6xf3wetbkk8g` (`projectileShape3D_id`),
  KEY `FKpto1m2l3ire9y1mhptu2fve4x` (`turretType_id`),
  CONSTRAINT `FK5rxijs3f390eu6xf3wetbkk8g` FOREIGN KEY (`projectileShape3D_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKpto1m2l3ire9y1mhptu2fve4x` FOREIGN KEY (`turretType_id`) REFERENCES `BASE_ITEM_TURRET_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES
CREATE TABLE IF NOT EXISTS `BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES` (
  `weapon` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_amlxea599glsjiee8ukxv7rj9` (`baseItemType`),
  KEY `FKslr6g6v2qqyxtt9dmc09ftrar` (`weapon`),
  CONSTRAINT `FK7jeslr1x745ry5ybx4agf1kte` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKslr6g6v2qqyxtt9dmc09ftrar` FOREIGN KEY (`weapon`) REFERENCES `BASE_ITEM_WEAPON_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOT_CONFIG
CREATE TABLE IF NOT EXISTS `BOT_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `actionDelay` int(11) NOT NULL,
  `autoAttack` bit(1) NOT NULL,
  `auxiliaryId` int(11) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `maxActiveMs` int(11) DEFAULT NULL,
  `maxInactiveMs` int(11) DEFAULT NULL,
  `minActiveMs` int(11) DEFAULT NULL,
  `minInactiveMs` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `npc` bit(1) NOT NULL,
  `realm_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpevo4xbk12x450abbpwgsdoj4` (`realm_id`),
  CONSTRAINT `FKpevo4xbk12x450abbpwgsdoj4` FOREIGN KEY (`realm_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOT_CONFIG_BOT_ITEM
CREATE TABLE IF NOT EXISTS `BOT_CONFIG_BOT_ITEM` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `angle` double NOT NULL,
  `count` int(11) NOT NULL,
  `createDirectly` bit(1) NOT NULL,
  `idleTtl` int(11) DEFAULT NULL,
  `moveRealmIfIdle` bit(1) NOT NULL,
  `noRebuild` bit(1) NOT NULL,
  `noSpawn` bit(1) NOT NULL,
  `rePopTime` int(11) DEFAULT NULL,
  `baseItemTypeEntity_id` int(11) DEFAULT NULL,
  `place_id` int(11) DEFAULT NULL,
  `botEnragementStateConfig` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrmnmfvv7eqabotm7aik3kp0vo` (`baseItemTypeEntity_id`),
  KEY `FKo3anxwa9qjqljjau19r55097r` (`place_id`),
  KEY `FKka3ph3evw0vd8qvpafrc8u9q3` (`botEnragementStateConfig`),
  CONSTRAINT `FKka3ph3evw0vd8qvpafrc8u9q3` FOREIGN KEY (`botEnragementStateConfig`) REFERENCES `BOT_CONFIG_ENRAGEMENT_STATE_CONFIG` (`id`),
  CONSTRAINT `FKo3anxwa9qjqljjau19r55097r` FOREIGN KEY (`place_id`) REFERENCES `PLACE_CONFIG` (`id`),
  CONSTRAINT `FKrmnmfvv7eqabotm7aik3kp0vo` FOREIGN KEY (`baseItemTypeEntity_id`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOT_CONFIG_ENRAGEMENT_STATE_CONFIG
CREATE TABLE IF NOT EXISTS `BOT_CONFIG_ENRAGEMENT_STATE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `enrageUpKills` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `botConfig` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8cnda4p93imbirpidwrfoq27n` (`botConfig`),
  CONSTRAINT `FK8cnda4p93imbirpidwrfoq27n` FOREIGN KEY (`botConfig`) REFERENCES `BOT_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOT_SCENE_CONFIG
CREATE TABLE IF NOT EXISTS `BOT_SCENE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOT_SCENE_CONFIG_BOTS_TO_WATCH
CREATE TABLE IF NOT EXISTS `BOT_SCENE_CONFIG_BOTS_TO_WATCH` (
  `botScene` int(11) NOT NULL,
  `bot` int(11) NOT NULL,
  UNIQUE KEY `UK_n11vrd5hv42ih8gs7oca20ftc` (`bot`),
  KEY `FKaqf9ms83ch45310assmbd7k33` (`botScene`),
  CONSTRAINT `FKaqf9ms83ch45310assmbd7k33` FOREIGN KEY (`botScene`) REFERENCES `BOT_SCENE_CONFIG` (`id`),
  CONSTRAINT `FKkf6g9dgv0g5v2w4g07bhjpp58` FOREIGN KEY (`bot`) REFERENCES `BOT_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOT_SCENE_CONFLICT_CONFIG
CREATE TABLE IF NOT EXISTS `BOT_SCENE_CONFLICT_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `enterDuration` int(11) NOT NULL,
  `enterKills` int(11) NOT NULL,
  `leaveNoKillDuration` int(11) NOT NULL,
  `maxDistance` double NOT NULL,
  `minDistance` double NOT NULL,
  `rePopMillis` int(11) DEFAULT NULL,
  `stopKills` int(11) DEFAULT NULL,
  `stopMillis` int(11) DEFAULT NULL,
  `botConfig_id` int(11) DEFAULT NULL,
  `targetBaseItemType_id` int(11) DEFAULT NULL,
  `botConfig` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsa7ypi28v52meu8c3le2nwaom` (`botConfig_id`),
  KEY `FKlma4qi8pwcrif0ckup8rxrya7` (`targetBaseItemType_id`),
  KEY `FKeiobef7r4fmbjbxa1pghslhvm` (`botConfig`),
  CONSTRAINT `FKeiobef7r4fmbjbxa1pghslhvm` FOREIGN KEY (`botConfig`) REFERENCES `BOT_SCENE_CONFIG` (`id`),
  CONSTRAINT `FKlma4qi8pwcrif0ckup8rxrya7` FOREIGN KEY (`targetBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKsa7ypi28v52meu8c3le2nwaom` FOREIGN KEY (`botConfig_id`) REFERENCES `BOT_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOX_ITEM_TYPE
CREATE TABLE IF NOT EXISTS `BOX_ITEM_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fixVerticalNorm` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `terrainType` varchar(255) DEFAULT NULL,
  `ttl` int(11) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrq0demtetxtet71d7hfcjp4lb` (`i18nDescription_id`),
  KEY `FKdjertib6r3nx0t006wmbvv5w1` (`i18nName_id`),
  KEY `FK9wtm3xgkwr25ttr1pubebcdc3` (`shape3DId_id`),
  KEY `FKg12jahlu2kis8r2hfswjqykfe` (`thumbnail_id`),
  CONSTRAINT `FK9wtm3xgkwr25ttr1pubebcdc3` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKdjertib6r3nx0t006wmbvv5w1` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKg12jahlu2kis8r2hfswjqykfe` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKrq0demtetxtet71d7hfcjp4lb` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.BOX_ITEM_TYPE_POSSIBILITY
CREATE TABLE IF NOT EXISTS `BOX_ITEM_TYPE_POSSIBILITY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystals` int(11) DEFAULT NULL,
  `possibility` double NOT NULL,
  `inventoryItem_id` int(11) DEFAULT NULL,
  `boxItemTypePossibilities_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo3e4s038xiegu5lp08ts2vbda` (`inventoryItem_id`),
  KEY `FK94mqsdrnx85779a7i1de17db6` (`boxItemTypePossibilities_id`),
  CONSTRAINT `FK94mqsdrnx85779a7i1de17db6` FOREIGN KEY (`boxItemTypePossibilities_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FKo3e4s038xiegu5lp08ts2vbda` FOREIGN KEY (`inventoryItem_id`) REFERENCES `INVENTORY_ITEM` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.CHAT_MESSAGE
CREATE TABLE IF NOT EXISTS `CHAT_MESSAGE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` longtext DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timestamp` datetime(3) DEFAULT NULL,
  `user` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkbs1pyjp60ex8hl7yc01j1gw5` (`user`),
  CONSTRAINT `FKkbs1pyjp60ex8hl7yc01j1gw5` FOREIGN KEY (`user`) REFERENCES `USER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.COLLADA
CREATE TABLE IF NOT EXISTS `COLLADA` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `colladaString` longtext DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.COLLADA_ANIMATIONS
CREATE TABLE IF NOT EXISTS `COLLADA_ANIMATIONS` (
  `ColladaEntity_id` int(11) NOT NULL,
  `animations` varchar(255) DEFAULT NULL,
  `animations_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`animations_KEY`),
  CONSTRAINT `FKtk7g7rbkh3n2wrl77bafo4bhd` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.COLLADA_CHARACTER_REPRESENTING
CREATE TABLE IF NOT EXISTS `COLLADA_CHARACTER_REPRESENTING` (
  `ColladaEntity_id` int(11) NOT NULL,
  `characterRepresentings` bit(1) DEFAULT NULL,
  `characterRepresentings_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`characterRepresentings_KEY`),
  CONSTRAINT `FK3nx9shdy3yrdpk6w8kmhdtfot` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.COLLADA_TEXTURES
CREATE TABLE IF NOT EXISTS `COLLADA_TEXTURES` (
  `ColladaEntity_id` int(11) NOT NULL,
  `textures_id` int(11) NOT NULL,
  `textures_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`textures_KEY`),
  KEY `FKgs74sss6ut5r7iqqbds1yc91w` (`textures_id`),
  CONSTRAINT `FK6ee9gdwsx3sm1fxpo8wkvwj8y` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKgs74sss6ut5r7iqqbds1yc91w` FOREIGN KEY (`textures_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.DEBUG
CREATE TABLE IF NOT EXISTS `DEBUG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `debugMessage` longtext DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `system` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.FB_MARKETING_CLICK_TRACKER
CREATE TABLE IF NOT EXISTS `FB_MARKETING_CLICK_TRACKER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adId` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.FB_MARKETING_CURRENT_AD
CREATE TABLE IF NOT EXISTS `FB_MARKETING_CURRENT_AD` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adId` bigint(20) NOT NULL,
  `adSetId` bigint(20) NOT NULL,
  `body` varchar(255) DEFAULT NULL,
  `campaignId` bigint(20) NOT NULL,
  `custom` varchar(255) DEFAULT NULL,
  `dailyBudget` double DEFAULT NULL,
  `dateStart` datetime(3) DEFAULT NULL,
  `dateStop` datetime(3) DEFAULT NULL,
  `facebookPositions` varchar(255) DEFAULT NULL,
  `imageHash` varchar(255) DEFAULT NULL,
  `lifeTime` bit(1) NOT NULL,
  `lifeTimeBudget` double DEFAULT NULL,
  `scheduleTimeEnd` datetime(3) DEFAULT NULL,
  `scheduleTimeStart` datetime(3) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `urlTagParam` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.FB_MARKETING_CURRENT_AD_INTEREST
CREATE TABLE IF NOT EXISTS `FB_MARKETING_CURRENT_AD_INTEREST` (
  `currentAdEntityId` int(11) NOT NULL,
  `audienceSize` bigint(20) DEFAULT NULL,
  `fbId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  KEY `FKnjtju67voai2g6udj66ueo9v0` (`currentAdEntityId`),
  CONSTRAINT `FKnjtju67voai2g6udj66ueo9v0` FOREIGN KEY (`currentAdEntityId`) REFERENCES `FB_MARKETING_CURRENT_AD` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.FB_MARKETING_HISTORY_AD
CREATE TABLE IF NOT EXISTS `FB_MARKETING_HISTORY_AD` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adId` bigint(20) NOT NULL,
  `adSetId` bigint(20) NOT NULL,
  `body` varchar(255) DEFAULT NULL,
  `campaignId` bigint(20) NOT NULL,
  `clicks` int(11) NOT NULL,
  `custom` varchar(255) DEFAULT NULL,
  `dailyBudget` double DEFAULT NULL,
  `dateStart` datetime(3) DEFAULT NULL,
  `dateStop` datetime(3) DEFAULT NULL,
  `facebookPositions` varchar(255) DEFAULT NULL,
  `imageHash` varchar(255) DEFAULT NULL,
  `impressions` int(11) NOT NULL,
  `lifeTime` bit(1) NOT NULL,
  `lifeTimeBudget` double DEFAULT NULL,
  `scheduleTimeEnd` datetime(3) DEFAULT NULL,
  `scheduleTimeStart` datetime(3) DEFAULT NULL,
  `spent` double NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `urlTagParam` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.FB_MARKETING_HISTORY_AD_INTEREST
CREATE TABLE IF NOT EXISTS `FB_MARKETING_HISTORY_AD_INTEREST` (
  `historyAdEntityId` int(11) NOT NULL,
  `audienceSize` bigint(20) DEFAULT NULL,
  `fbId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  KEY `FKad1jynmyqvb71p3w5ftjsbaxa` (`historyAdEntityId`),
  CONSTRAINT `FKad1jynmyqvb71p3w5ftjsbaxa` FOREIGN KEY (`historyAdEntityId`) REFERENCES `FB_MARKETING_HISTORY_AD` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.GAME_UI_CONTROL_CONFIG
CREATE TABLE IF NOT EXISTS `GAME_UI_CONTROL_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `detailedTracking` bit(1) NOT NULL,
  `gameEngineMode` varchar(255) DEFAULT NULL,
  `minimalLevel_id` int(11) DEFAULT NULL,
  `planetEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsijau4m27twx6nsuc4sa7s31u` (`minimalLevel_id`),
  KEY `FK60h9hy9lm12515cvw99w1oel7` (`planetEntity_id`),
  CONSTRAINT `FK60h9hy9lm12515cvw99w1oel7` FOREIGN KEY (`planetEntity_id`) REFERENCES `PLANET` (`id`),
  CONSTRAINT `FKsijau4m27twx6nsuc4sa7s31u` FOREIGN KEY (`minimalLevel_id`) REFERENCES `LEVEL` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.GROUND_CONFIG
CREATE TABLE IF NOT EXISTS `GROUND_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `topBumpMapDepth` double DEFAULT NULL,
  `topScale` double DEFAULT NULL,
  `topShininess` double DEFAULT NULL,
  `topSpecularStrength` double DEFAULT NULL,
  `topBumpMapId` int(11) DEFAULT NULL,
  `topTextureId` int(11) DEFAULT NULL,
  `bottomBumpMapDepth` double DEFAULT NULL,
  `bottomScale` double DEFAULT NULL,
  `bottomShininess` double DEFAULT NULL,
  `bottomSpecularStrength` double DEFAULT NULL,
  `bottomBumpMapId` int(11) DEFAULT NULL,
  `bottomTextureId` int(11) DEFAULT NULL,
  `splattingScale2` double DEFAULT NULL,
  `splattingAmplitude` double DEFAULT NULL,
  `splattingBlur` double DEFAULT NULL,
  `splattingOffset` double DEFAULT NULL,
  `splattingScale` double DEFAULT NULL,
  `splattingImageId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkqjsikvnicb0qo3auqhlghq3a` (`topBumpMapId`),
  KEY `FKnrbnl1yvrst306eyg8mkpqt6i` (`topTextureId`),
  KEY `FKrwtceqklrjay1axmmlfj75di3` (`bottomBumpMapId`),
  KEY `FKnoefvdom880ef7ioae7tjwct8` (`bottomTextureId`),
  KEY `FKlmwoy4287cl2sksg1cf8cx5dd` (`splattingImageId`),
  CONSTRAINT `FKkqjsikvnicb0qo3auqhlghq3a` FOREIGN KEY (`topBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKlmwoy4287cl2sksg1cf8cx5dd` FOREIGN KEY (`splattingImageId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKnoefvdom880ef7ioae7tjwct8` FOREIGN KEY (`bottomTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKnrbnl1yvrst306eyg8mkpqt6i` FOREIGN KEY (`topTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKrwtceqklrjay1axmmlfj75di3` FOREIGN KEY (`bottomBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HISTORY_BOT_SCENE_INDICATOR
CREATE TABLE IF NOT EXISTS `HISTORY_BOT_SCENE_INDICATOR` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botSceneId` int(11) DEFAULT NULL,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `newBotSceneConflictConfigId` int(11) DEFAULT NULL,
  `oldBotSceneConflictConfigId` int(11) DEFAULT NULL,
  `raise` bit(1) NOT NULL,
  `step` int(11) DEFAULT NULL,
  `stepCount` int(11) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HISTORY_FORGOT_PASSWORDY
CREATE TABLE IF NOT EXISTS `HISTORY_FORGOT_PASSWORDY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `forgotPasswordEntityId` int(11) NOT NULL,
  `humanPlayerId` int(11) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HISTORY_INVENTORY
CREATE TABLE IF NOT EXISTS `HISTORY_INVENTORY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystals` int(11) DEFAULT NULL,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `inventoryItemId` int(11) DEFAULT NULL,
  `inventoryItemName` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HISTORY_LEVEL
CREATE TABLE IF NOT EXISTS `HISTORY_LEVEL` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `levelId` int(11) NOT NULL,
  `levelNumber` int(11) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HISTORY_QUEST
CREATE TABLE IF NOT EXISTS `HISTORY_QUEST` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `questId` int(11) NOT NULL,
  `questInternalName` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HISTORY_UNLOCKED
CREATE TABLE IF NOT EXISTS `HISTORY_UNLOCKED` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystals` int(11) DEFAULT NULL,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `unlockEntityId` int(11) DEFAULT NULL,
  `unlockEntityName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HISTORY_USER
CREATE TABLE IF NOT EXISTS `HISTORY_USER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `loggedIn` datetime(3) DEFAULT NULL,
  `loggedOut` datetime(3) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `userId` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.HUMAN_PLAYER_ENTITY
CREATE TABLE IF NOT EXISTS `HUMAN_PLAYER_ENTITY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.I18N_BUNDLE
CREATE TABLE IF NOT EXISTS `I18N_BUNDLE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.I18N_BUNDLE_STRING
CREATE TABLE IF NOT EXISTS `I18N_BUNDLE_STRING` (
  `bundle` int(11) NOT NULL,
  `i18nString` varchar(10000) DEFAULT NULL,
  `locale` varchar(180) NOT NULL,
  PRIMARY KEY (`bundle`,`locale`),
  CONSTRAINT `FKpveq2u612sld12e8xax55qlgm` FOREIGN KEY (`bundle`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.IMAGE_LIBRARY
CREATE TABLE IF NOT EXISTS `IMAGE_LIBRARY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` longblob DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.INVENTORY_ITEM
CREATE TABLE IF NOT EXISTS `INVENTORY_ITEM` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baseItemTypeCount` int(11) NOT NULL,
  `crystalCost` int(11) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `itemFreeRange` double NOT NULL,
  `razarion` int(11) DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq1ayyaai4mk3dd8gsdcfpp1sk` (`baseItemType_id`),
  KEY `FKr30cctgb52isr7jyu1u6wsiif` (`i18nName_id`),
  KEY `FK70rj6a4eg5uq9s0r0i8rg10yr` (`image_id`),
  CONSTRAINT `FK70rj6a4eg5uq9s0r0i8rg10yr` FOREIGN KEY (`image_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKq1ayyaai4mk3dd8gsdcfpp1sk` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKr30cctgb52isr7jyu1u6wsiif` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.LEVEL
CREATE TABLE IF NOT EXISTS `LEVEL` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `number` int(11) NOT NULL,
  `xp2LevelUp` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.LEVEL_LIMITATION
CREATE TABLE IF NOT EXISTS `LEVEL_LIMITATION` (
  `LevelEntity_id` int(11) NOT NULL,
  `itemTypeLimitation` int(11) DEFAULT NULL,
  `baseItemTypeEntityId` int(11) NOT NULL,
  PRIMARY KEY (`LevelEntity_id`,`baseItemTypeEntityId`),
  KEY `FKiuvkgev9cw8g39baap1oyqhj4` (`baseItemTypeEntityId`),
  CONSTRAINT `FKip53iy42wu39wx0f7brf2cic2` FOREIGN KEY (`LevelEntity_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKiuvkgev9cw8g39baap1oyqhj4` FOREIGN KEY (`baseItemTypeEntityId`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.LEVEL_UNLOCK
CREATE TABLE IF NOT EXISTS `LEVEL_UNLOCK` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baseItemTypeCount` int(11) NOT NULL,
  `crystalCost` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbblsnanqjw6bv8ahra4o3epso` (`baseItemType_id`),
  KEY `FKq7py43ekb6hfgpx9jyhhk9854` (`i18nDescription_id`),
  KEY `FKkduxofk4bacta0r0fbn0sluov` (`i18nName_id`),
  KEY `FK8o89uccfj6dfo3jm0ucb2b2q8` (`thumbnail_id`),
  KEY `FK16dswxni52x0ya7bvjhtif7td` (`level`),
  CONSTRAINT `FK16dswxni52x0ya7bvjhtif7td` FOREIGN KEY (`level`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FK8o89uccfj6dfo3jm0ucb2b2q8` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKbblsnanqjw6bv8ahra4o3epso` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKkduxofk4bacta0r0fbn0sluov` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKq7py43ekb6hfgpx9jyhhk9854` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.PLACE_CONFIG
CREATE TABLE IF NOT EXISTS `PLACE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `radius` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.PLACE_CONFIG_POSITION_POLYGON
CREATE TABLE IF NOT EXISTS `PLACE_CONFIG_POSITION_POLYGON` (
  `OWNER_ID` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`OWNER_ID`,`orderColumn`),
  CONSTRAINT `FKmmbtyhe3hwrjo2djh1jnlfotc` FOREIGN KEY (`OWNER_ID`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.PLANET
CREATE TABLE IF NOT EXISTS `PLANET` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ambientA` double DEFAULT NULL,
  `ambientB` double DEFAULT NULL,
  `ambientG` double DEFAULT NULL,
  `ambientR` double DEFAULT NULL,
  `diffuseA` double DEFAULT NULL,
  `diffuseB` double DEFAULT NULL,
  `diffuseG` double DEFAULT NULL,
  `diffuseR` double DEFAULT NULL,
  `groundMeshDimensionEndX` int(11) DEFAULT NULL,
  `groundMeshDimensionEndY` int(11) DEFAULT NULL,
  `groundMeshDimensionStartX` int(11) DEFAULT NULL,
  `groundMeshDimensionStartY` int(11) DEFAULT NULL,
  `houseSpace` int(11) NOT NULL,
  `lightDirectionX` double DEFAULT NULL,
  `lightDirectionY` double DEFAULT NULL,
  `lightDirectionZ` double DEFAULT NULL,
  `miniMapImage` longblob DEFAULT NULL,
  `playGroundEndX` double DEFAULT NULL,
  `playGroundEndY` double DEFAULT NULL,
  `playGroundStartX` double DEFAULT NULL,
  `playGroundStartY` double DEFAULT NULL,
  `shadowAlpha` double NOT NULL,
  `startRazarion` int(11) NOT NULL,
  `groundConfig_id` int(11) DEFAULT NULL,
  `startBaseItemType_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqvdx3rd9pwbcpl3k3my1pdefw` (`groundConfig_id`),
  KEY `FKpal29tk0xwgxthg5jpc08tcbr` (`startBaseItemType_id`),
  CONSTRAINT `FKpal29tk0xwgxthg5jpc08tcbr` FOREIGN KEY (`startBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKqvdx3rd9pwbcpl3k3my1pdefw` FOREIGN KEY (`groundConfig_id`) REFERENCES `GROUND_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.PLANET_LIMITATION
CREATE TABLE IF NOT EXISTS `PLANET_LIMITATION` (
  `PlanetEntity_id` int(11) NOT NULL,
  `itemTypeLimitation` int(11) DEFAULT NULL,
  `baseItemTypeEntityId` int(11) NOT NULL,
  PRIMARY KEY (`PlanetEntity_id`,`baseItemTypeEntityId`),
  KEY `FKmnjiyn3ivehnao6o5fuj4qdwc` (`baseItemTypeEntityId`),
  CONSTRAINT `FKmnjiyn3ivehnao6o5fuj4qdwc` FOREIGN KEY (`baseItemTypeEntityId`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKmv1lgmud5v7ja4eyakk2sgdi5` FOREIGN KEY (`PlanetEntity_id`) REFERENCES `PLANET` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.PROPERTY
CREATE TABLE IF NOT EXISTS `PROPERTY` (
  `propertyKey` varchar(190) NOT NULL,
  `colorAValue` double DEFAULT NULL,
  `colorBValue` double DEFAULT NULL,
  `colorGValue` double DEFAULT NULL,
  `colorRValue` double DEFAULT NULL,
  `doubleValue` double DEFAULT NULL,
  `intValue` int(11) DEFAULT NULL,
  `audio_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`propertyKey`),
  KEY `FKgv62h5ai81d9fld7h96s4yw5k` (`audio_id`),
  KEY `FK3v2abxg87erc7jc7gm22ap9ui` (`image_id`),
  KEY `FKa5a5hlgo2c1ycdku94ya9vo79` (`shape3DId_id`),
  CONSTRAINT `FK3v2abxg87erc7jc7gm22ap9ui` FOREIGN KEY (`image_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKa5a5hlgo2c1ycdku94ya9vo79` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKgv62h5ai81d9fld7h96s4yw5k` FOREIGN KEY (`audio_id`) REFERENCES `AUDIO_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.QUEST
CREATE TABLE IF NOT EXISTS `QUEST` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystal` int(11) NOT NULL,
  `hidePassedDialog` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `razarion` int(11) NOT NULL,
  `xp` int(11) NOT NULL,
  `conditionConfigEntity_id` int(11) DEFAULT NULL,
  `description_id` int(11) DEFAULT NULL,
  `passedMessage_id` int(11) DEFAULT NULL,
  `title_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg8ekfv29livdwri9llb7p620p` (`conditionConfigEntity_id`),
  KEY `FK8lv5qeunaw77hqlwfticg7w55` (`description_id`),
  KEY `FKmllgre0tlj3i2od98hfmfj9ry` (`passedMessage_id`),
  KEY `FKmbk4qf213aih41banidutli49` (`title_id`),
  CONSTRAINT `FK8lv5qeunaw77hqlwfticg7w55` FOREIGN KEY (`description_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKg8ekfv29livdwri9llb7p620p` FOREIGN KEY (`conditionConfigEntity_id`) REFERENCES `QUEST_CONDITION` (`id`),
  CONSTRAINT `FKmbk4qf213aih41banidutli49` FOREIGN KEY (`title_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKmllgre0tlj3i2od98hfmfj9ry` FOREIGN KEY (`passedMessage_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.QUEST_COMPARISON
CREATE TABLE IF NOT EXISTS `QUEST_COMPARISON` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) DEFAULT NULL,
  `time` int(11) DEFAULT NULL,
  `placeConfig_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlgftwoj397x1478q83caeykv4` (`placeConfig_id`),
  CONSTRAINT `FKlgftwoj397x1478q83caeykv4` FOREIGN KEY (`placeConfig_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.QUEST_COMPARISON_BASE_ITEM
CREATE TABLE IF NOT EXISTS `QUEST_COMPARISON_BASE_ITEM` (
  `ComparisonConfigEntity_id` int(11) NOT NULL,
  `typeCount` int(11) DEFAULT NULL,
  `baseItemTypeEntityId` int(11) NOT NULL,
  PRIMARY KEY (`ComparisonConfigEntity_id`,`baseItemTypeEntityId`),
  KEY `FKox69d9d2u7iegll4y4bua3uhn` (`baseItemTypeEntityId`),
  CONSTRAINT `FKox69d9d2u7iegll4y4bua3uhn` FOREIGN KEY (`baseItemTypeEntityId`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKqewsitlqloonk59cl0vlbyq6o` FOREIGN KEY (`ComparisonConfigEntity_id`) REFERENCES `QUEST_COMPARISON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.QUEST_COMPARISON_BOT
CREATE TABLE IF NOT EXISTS `QUEST_COMPARISON_BOT` (
  `comparisonConfig` int(11) NOT NULL,
  `botConfig` int(11) NOT NULL,
  KEY `FKgsivij4a6t882dkggs8ubbgay` (`botConfig`),
  KEY `FKk49fh7nqm3yx2jbu9088qh4pn` (`comparisonConfig`),
  CONSTRAINT `FKgsivij4a6t882dkggs8ubbgay` FOREIGN KEY (`botConfig`) REFERENCES `BOT_CONFIG` (`id`),
  CONSTRAINT `FKk49fh7nqm3yx2jbu9088qh4pn` FOREIGN KEY (`comparisonConfig`) REFERENCES `QUEST_COMPARISON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.QUEST_CONDITION
CREATE TABLE IF NOT EXISTS `QUEST_CONDITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `conditionTrigger` varchar(255) DEFAULT NULL,
  `comparisonConfig_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe3ke0d5alxod0vb59hbol2t5t` (`comparisonConfig_id`),
  CONSTRAINT `FKe3ke0d5alxod0vb59hbol2t5t` FOREIGN KEY (`comparisonConfig_id`) REFERENCES `QUEST_COMPARISON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.RESOURCE_ITEM_TYPE
CREATE TABLE IF NOT EXISTS `RESOURCE_ITEM_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` int(11) NOT NULL,
  `fixVerticalNorm` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `terrainType` varchar(255) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKna2llalftwo2brjaevmocun4a` (`i18nDescription_id`),
  KEY `FK92mldtoam62qyvjd90f8rw7op` (`i18nName_id`),
  KEY `FKsxtu46oir6k4rqq8em4jcxc7j` (`shape3DId_id`),
  KEY `FKpaxta3ofhuj6op1ris60fnssi` (`thumbnail_id`),
  CONSTRAINT `FK92mldtoam62qyvjd90f8rw7op` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKna2llalftwo2brjaevmocun4a` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKpaxta3ofhuj6op1ris60fnssi` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKsxtu46oir6k4rqq8em4jcxc7j` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE
CREATE TABLE IF NOT EXISTS `SCENE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `duration` int(11) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `removeLoadingCover` bit(1) DEFAULT NULL,
  `scrollUiQuestCrystal` int(11) DEFAULT NULL,
  `scrollUiQuestI18nHidePassedDialog` bit(1) DEFAULT NULL,
  `scrollUiQuestRazarion` int(11) DEFAULT NULL,
  `scrollUiQuestTargetRectangleEndX` double DEFAULT NULL,
  `scrollUiQuestTargetRectangleEndY` double DEFAULT NULL,
  `scrollUiQuestTargetRectangleStartX` double DEFAULT NULL,
  `scrollUiQuestTargetRectangleStartY` double DEFAULT NULL,
  `scrollUiQuestXp` int(11) DEFAULT NULL,
  `suppressSell` bit(1) DEFAULT NULL,
  `viewFieldBottomWidth` double DEFAULT NULL,
  `viewFieldCameraLocked` bit(1) DEFAULT NULL,
  `viewFieldFromPositionX` double DEFAULT NULL,
  `viewFieldFromPositionY` double DEFAULT NULL,
  `viewFieldSpeed` double DEFAULT NULL,
  `viewFieldToPositionX` double DEFAULT NULL,
  `viewFieldToPositionY` double DEFAULT NULL,
  `wait4LevelUpDialog` bit(1) DEFAULT NULL,
  `wait4QuestPassedDialog` bit(1) DEFAULT NULL,
  `waitForBaseLostDialog` bit(1) DEFAULT NULL,
  `gameTipConfigEntity_id` int(11) DEFAULT NULL,
  `i18nIntroText_id` int(11) DEFAULT NULL,
  `questConfig_id` int(11) DEFAULT NULL,
  `scrollUiQuestI18nDescription_id` int(11) DEFAULT NULL,
  `scrollUiQuestI18nPassedMessage_id` int(11) DEFAULT NULL,
  `scrollUiQuestI18nTitle_id` int(11) DEFAULT NULL,
  `startPointPlacerEntity_id` int(11) DEFAULT NULL,
  `gameUiControlConfigEntityId` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3qbn108jatajcx7n3nh1wujwy` (`gameTipConfigEntity_id`),
  KEY `FKmgc4mkbvugjb4pqdaqqhmsjx9` (`i18nIntroText_id`),
  KEY `FKpyv03l0m8msuuohy94f40416s` (`questConfig_id`),
  KEY `FKgiqefa1ka4nmnf94bv0wvaw1e` (`scrollUiQuestI18nDescription_id`),
  KEY `FK5mvo838o44jb4cgcjbl1darg` (`scrollUiQuestI18nPassedMessage_id`),
  KEY `FKqpw9fls2x66i7v80ch6xiv7qq` (`scrollUiQuestI18nTitle_id`),
  KEY `FKt94pq452h8y4a7b8g26y8rfk8` (`startPointPlacerEntity_id`),
  KEY `FKr3y49vh9c84c2udd49avkoyd5` (`gameUiControlConfigEntityId`),
  CONSTRAINT `FK3qbn108jatajcx7n3nh1wujwy` FOREIGN KEY (`gameTipConfigEntity_id`) REFERENCES `SCENE_TIP_CONFIG` (`id`),
  CONSTRAINT `FK5mvo838o44jb4cgcjbl1darg` FOREIGN KEY (`scrollUiQuestI18nPassedMessage_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKgiqefa1ka4nmnf94bv0wvaw1e` FOREIGN KEY (`scrollUiQuestI18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKmgc4mkbvugjb4pqdaqqhmsjx9` FOREIGN KEY (`i18nIntroText_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKpyv03l0m8msuuohy94f40416s` FOREIGN KEY (`questConfig_id`) REFERENCES `QUEST` (`id`),
  CONSTRAINT `FKqpw9fls2x66i7v80ch6xiv7qq` FOREIGN KEY (`scrollUiQuestI18nTitle_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKr3y49vh9c84c2udd49avkoyd5` FOREIGN KEY (`gameUiControlConfigEntityId`) REFERENCES `GAME_UI_CONTROL_CONFIG` (`id`),
  CONSTRAINT `FKt94pq452h8y4a7b8g26y8rfk8` FOREIGN KEY (`startPointPlacerEntity_id`) REFERENCES `SCENE_START_POINT_PLACER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT
CREATE TABLE IF NOT EXISTS `SCENE_BOT` (
  `sceneId` int(11) NOT NULL,
  `botd` int(11) NOT NULL,
  UNIQUE KEY `UK_i0h7apnl04b5k9xuifa2j2qcu` (`botd`),
  KEY `FKghhma6f0af1xgwvaq2dqqcm0m` (`sceneId`),
  CONSTRAINT `FK1wawn9gpryikbmh0cjsqel84c` FOREIGN KEY (`botd`) REFERENCES `BOT_CONFIG` (`id`),
  CONSTRAINT `FKghhma6f0af1xgwvaq2dqqcm0m` FOREIGN KEY (`sceneId`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT_ATTACK_COMMAND
CREATE TABLE IF NOT EXISTS `SCENE_BOT_ATTACK_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `actorItemType_id` int(11) DEFAULT NULL,
  `targetItemType_id` int(11) DEFAULT NULL,
  `targetSelection_id` int(11) DEFAULT NULL,
  `botAttackCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK41qgg36def0x3j8xjg61tl9ly` (`actorItemType_id`),
  KEY `FK9q1onf1n6vq18ep096s80u0vm` (`targetItemType_id`),
  KEY `FK6lfeos35sq8euepu1nqeoijqq` (`targetSelection_id`),
  KEY `FKrgg9j7lq38k61hkwuy73wxght` (`botAttackCommandEntities_id`),
  CONSTRAINT `FK41qgg36def0x3j8xjg61tl9ly` FOREIGN KEY (`actorItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FK6lfeos35sq8euepu1nqeoijqq` FOREIGN KEY (`targetSelection_id`) REFERENCES `PLACE_CONFIG` (`id`),
  CONSTRAINT `FK9q1onf1n6vq18ep096s80u0vm` FOREIGN KEY (`targetItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrgg9j7lq38k61hkwuy73wxght` FOREIGN KEY (`botAttackCommandEntities_id`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT_HARVEST_COMMAND
CREATE TABLE IF NOT EXISTS `SCENE_BOT_HARVEST_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `harvesterItemType_id` int(11) DEFAULT NULL,
  `resourceItemType_id` int(11) DEFAULT NULL,
  `resourceSelection_id` int(11) DEFAULT NULL,
  `botHarvestCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK52rh3hg6sc1g6xuhxqd6l0l0w` (`harvesterItemType_id`),
  KEY `FK9sc5qi93p4cby5b2hojcr90gh` (`resourceItemType_id`),
  KEY `FKs2dv4bd53e7r6vokmm96qh0x4` (`resourceSelection_id`),
  KEY `FK3mpduff9ibnr01pj8lnbnetm1` (`botHarvestCommandEntities_id`),
  CONSTRAINT `FK3mpduff9ibnr01pj8lnbnetm1` FOREIGN KEY (`botHarvestCommandEntities_id`) REFERENCES `SCENE` (`id`),
  CONSTRAINT `FK52rh3hg6sc1g6xuhxqd6l0l0w` FOREIGN KEY (`harvesterItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FK9sc5qi93p4cby5b2hojcr90gh` FOREIGN KEY (`resourceItemType_id`) REFERENCES `RESOURCE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKs2dv4bd53e7r6vokmm96qh0x4` FOREIGN KEY (`resourceSelection_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT_KILL_BOT_COMMAND
CREATE TABLE IF NOT EXISTS `SCENE_BOT_KILL_BOT_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `killBotCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKthyq8egxquxqatrt4xev5fpim` (`killBotCommandEntities_id`),
  CONSTRAINT `FKthyq8egxquxqatrt4xev5fpim` FOREIGN KEY (`killBotCommandEntities_id`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT_KILL_HUMAN_COMMAND
CREATE TABLE IF NOT EXISTS `SCENE_BOT_KILL_HUMAN_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `dominanceFactor` int(11) NOT NULL,
  `attackerBaseItemType_id` int(11) DEFAULT NULL,
  `spawnPoint_id` int(11) DEFAULT NULL,
  `botKillHumanCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqdgvd8bc4gqifn5d46rmf5iy8` (`attackerBaseItemType_id`),
  KEY `FKdyqi8q4nrjqpcu4r9oiahw264` (`spawnPoint_id`),
  KEY `FKptfyx1wxu2fyxkg6nbj0xwec` (`botKillHumanCommandEntities_id`),
  CONSTRAINT `FKdyqi8q4nrjqpcu4r9oiahw264` FOREIGN KEY (`spawnPoint_id`) REFERENCES `PLACE_CONFIG` (`id`),
  CONSTRAINT `FKptfyx1wxu2fyxkg6nbj0xwec` FOREIGN KEY (`botKillHumanCommandEntities_id`) REFERENCES `SCENE` (`id`),
  CONSTRAINT `FKqdgvd8bc4gqifn5d46rmf5iy8` FOREIGN KEY (`attackerBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT_KILL_OTHER_BOT_COMMAND
CREATE TABLE IF NOT EXISTS `SCENE_BOT_KILL_OTHER_BOT_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `dominanceFactor` int(11) NOT NULL,
  `targetBotAuxiliaryIdId` int(11) DEFAULT NULL,
  `attackerBaseItemType_id` int(11) DEFAULT NULL,
  `spawnPoint_id` int(11) DEFAULT NULL,
  `botKillOtherBotCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKckvfx8kt348yfb5d8txret8c1` (`attackerBaseItemType_id`),
  KEY `FKsl1rsgx936c34qyovbws0otae` (`spawnPoint_id`),
  KEY `FKbb3b9ari50ovnnpvjv3mevis4` (`botKillOtherBotCommandEntities_id`),
  CONSTRAINT `FKbb3b9ari50ovnnpvjv3mevis4` FOREIGN KEY (`botKillOtherBotCommandEntities_id`) REFERENCES `SCENE` (`id`),
  CONSTRAINT `FKckvfx8kt348yfb5d8txret8c1` FOREIGN KEY (`attackerBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKsl1rsgx936c34qyovbws0otae` FOREIGN KEY (`spawnPoint_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT_MOVE_COMMAND
CREATE TABLE IF NOT EXISTS `SCENE_BOT_MOVE_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `targetPositionX` double DEFAULT NULL,
  `targetPositionY` double DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `botMoveCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhh30m3n5y5yy51iiodrfjgxj5` (`baseItemType_id`),
  KEY `FKo130arf1f906t656xu20lrd3l` (`botMoveCommandEntities_id`),
  CONSTRAINT `FKhh30m3n5y5yy51iiodrfjgxj5` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKo130arf1f906t656xu20lrd3l` FOREIGN KEY (`botMoveCommandEntities_id`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOT_REMOVE_OWN_ITEMS_COMMAND
CREATE TABLE IF NOT EXISTS `SCENE_BOT_REMOVE_OWN_ITEMS_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `baseItemType2Remove_id` int(11) DEFAULT NULL,
  `botRemoveOwnItemCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd5rbur518hugollv8v21jnj1o` (`baseItemType2Remove_id`),
  KEY `FKhdrkr0eixuks1uoe9t9c949j3` (`botRemoveOwnItemCommandEntities_id`),
  CONSTRAINT `FKd5rbur518hugollv8v21jnj1o` FOREIGN KEY (`baseItemType2Remove_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKhdrkr0eixuks1uoe9t9c949j3` FOREIGN KEY (`botRemoveOwnItemCommandEntities_id`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_BOX_ITEM_POSITION
CREATE TABLE IF NOT EXISTS `SCENE_BOX_ITEM_POSITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `rotationZ` double NOT NULL,
  `boxItemType_id` int(11) DEFAULT NULL,
  `sceneId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1o5uesm507f5p1bu2km3ks769` (`boxItemType_id`),
  KEY `FK3yk9nif0gqotjwm5fmg4ep1ko` (`sceneId`),
  CONSTRAINT `FK1o5uesm507f5p1bu2km3ks769` FOREIGN KEY (`boxItemType_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FK3yk9nif0gqotjwm5fmg4ep1ko` FOREIGN KEY (`sceneId`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_RESOURCE_ITEM_POSITION
CREATE TABLE IF NOT EXISTS `SCENE_RESOURCE_ITEM_POSITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `rotationZ` double NOT NULL,
  `resourceItemType_id` int(11) DEFAULT NULL,
  `sceneId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb59khrjqywtrlb7k5awklv5sg` (`resourceItemType_id`),
  KEY `FKdwjv6d2ofmaqenv5c9ahsjowh` (`sceneId`),
  CONSTRAINT `FKb59khrjqywtrlb7k5awklv5sg` FOREIGN KEY (`resourceItemType_id`) REFERENCES `RESOURCE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKdwjv6d2ofmaqenv5c9ahsjowh` FOREIGN KEY (`sceneId`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_START_PLACE_ALLOWED_AREA
CREATE TABLE IF NOT EXISTS `SCENE_START_PLACE_ALLOWED_AREA` (
  `startPointPlacer` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`startPointPlacer`,`orderColumn`),
  CONSTRAINT `FKdjbsblqruqfirvx3582gh4hbx` FOREIGN KEY (`startPointPlacer`) REFERENCES `SCENE_START_POINT_PLACER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_START_POINT_PLACER
CREATE TABLE IF NOT EXISTS `SCENE_START_POINT_PLACER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `enemyFreeRadius` double DEFAULT NULL,
  `suggestedPositionX` double DEFAULT NULL,
  `suggestedPositionY` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SCENE_TIP_CONFIG
CREATE TABLE IF NOT EXISTS `SCENE_TIP_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `terrainPositionHintX` double DEFAULT NULL,
  `terrainPositionHintY` double DEFAULT NULL,
  `tip` varchar(255) DEFAULT NULL,
  `actor_id` int(11) DEFAULT NULL,
  `boxItemTypeEntity_id` int(11) DEFAULT NULL,
  `inventoryItemEntity_id` int(11) DEFAULT NULL,
  `placeConfig_id` int(11) DEFAULT NULL,
  `resourceItemTypeEntity_id` int(11) DEFAULT NULL,
  `scrollMapImage_id` int(11) DEFAULT NULL,
  `toCreatedItemType_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKghfmw5laj3jq1yjgw957xe8xx` (`actor_id`),
  KEY `FKrjgv1q2d2psdp6e9pmku4lprw` (`boxItemTypeEntity_id`),
  KEY `FKm16pv7bo8k3tuys0w9r13f73r` (`inventoryItemEntity_id`),
  KEY `FKsj33u8l46y25lbxi2tji7fato` (`placeConfig_id`),
  KEY `FKpuux23wj061stcnd20tyyvy8e` (`resourceItemTypeEntity_id`),
  KEY `FKcwh07brwqim61gt1cxpgb8b2c` (`scrollMapImage_id`),
  KEY `FKrb1ne75jd6gn0rlx5av4cy9ga` (`toCreatedItemType_id`),
  CONSTRAINT `FKcwh07brwqim61gt1cxpgb8b2c` FOREIGN KEY (`scrollMapImage_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKghfmw5laj3jq1yjgw957xe8xx` FOREIGN KEY (`actor_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKm16pv7bo8k3tuys0w9r13f73r` FOREIGN KEY (`inventoryItemEntity_id`) REFERENCES `INVENTORY_ITEM` (`id`),
  CONSTRAINT `FKpuux23wj061stcnd20tyyvy8e` FOREIGN KEY (`resourceItemTypeEntity_id`) REFERENCES `RESOURCE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrb1ne75jd6gn0rlx5av4cy9ga` FOREIGN KEY (`toCreatedItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrjgv1q2d2psdp6e9pmku4lprw` FOREIGN KEY (`boxItemTypeEntity_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FKsj33u8l46y25lbxi2tji7fato` FOREIGN KEY (`placeConfig_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_BOX_REGION_CONFIG
CREATE TABLE IF NOT EXISTS `SERVER_BOX_REGION_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `maxInterval` int(11) NOT NULL,
  `minDistanceToItems` double NOT NULL,
  `minInterval` int(11) NOT NULL,
  `boxItemTypeId_id` int(11) DEFAULT NULL,
  `region_id` int(11) DEFAULT NULL,
  `serverGameEngineId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8i0nxbtvhyiksqd3m90j8sble` (`boxItemTypeId_id`),
  KEY `FKcq7owvwvc3cvnd2dy4b134ed2` (`region_id`),
  KEY `FK4q31xi3o8ty49njgjdolpc5rj` (`serverGameEngineId`),
  CONSTRAINT `FK4q31xi3o8ty49njgjdolpc5rj` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`),
  CONSTRAINT `FK8i0nxbtvhyiksqd3m90j8sble` FOREIGN KEY (`boxItemTypeId_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FKcq7owvwvc3cvnd2dy4b134ed2` FOREIGN KEY (`region_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_GAME_ENGINE_BOT_CONFIG
CREATE TABLE IF NOT EXISTS `SERVER_GAME_ENGINE_BOT_CONFIG` (
  `serverGameEngineId` int(11) NOT NULL,
  `botConfigId` int(11) NOT NULL,
  UNIQUE KEY `UK_9w8m9rvbobngldmp0p3hdxgtl` (`botConfigId`),
  KEY `FKh6pao5d5ikcs0h79x6gwx8tfj` (`serverGameEngineId`),
  CONSTRAINT `FKh6pao5d5ikcs0h79x6gwx8tfj` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`),
  CONSTRAINT `FKrlpv849ke6l8gyvfsm5f8ya9g` FOREIGN KEY (`botConfigId`) REFERENCES `BOT_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_GAME_ENGINE_BOT_SCENE_CONFIG
CREATE TABLE IF NOT EXISTS `SERVER_GAME_ENGINE_BOT_SCENE_CONFIG` (
  `serverGameEngineId` int(11) NOT NULL,
  `botSceneConfigId` int(11) NOT NULL,
  UNIQUE KEY `UK_rusfme732vodwo2j02s27oxhp` (`botSceneConfigId`),
  KEY `FK80dfty4j4too59uh1t4axhmt9` (`serverGameEngineId`),
  CONSTRAINT `FK2bm7mr807eoedkfin3pf4mamb` FOREIGN KEY (`botSceneConfigId`) REFERENCES `BOT_SCENE_CONFIG` (`id`),
  CONSTRAINT `FK80dfty4j4too59uh1t4axhmt9` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_GAME_ENGINE_CONFIG
CREATE TABLE IF NOT EXISTS `SERVER_GAME_ENGINE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `planetEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcj2w622aa54egcdbyynkvtfu6` (`planetEntity_id`),
  CONSTRAINT `FKcj2w622aa54egcdbyynkvtfu6` FOREIGN KEY (`planetEntity_id`) REFERENCES `PLANET` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_LEVEL_QUEST
CREATE TABLE IF NOT EXISTS `SERVER_LEVEL_QUEST` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `minimalLevel_id` int(11) DEFAULT NULL,
  `serverGameEngineConfig` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmrpwxnx1x6gewn5mhbf9jqux6` (`minimalLevel_id`),
  KEY `FK11joq90eyoabtv4ltmgl6sx5o` (`serverGameEngineConfig`),
  CONSTRAINT `FK11joq90eyoabtv4ltmgl6sx5o` FOREIGN KEY (`serverGameEngineConfig`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`),
  CONSTRAINT `FKmrpwxnx1x6gewn5mhbf9jqux6` FOREIGN KEY (`minimalLevel_id`) REFERENCES `LEVEL` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_QUEST
CREATE TABLE IF NOT EXISTS `SERVER_QUEST` (
  `serverLevelQuest` int(11) NOT NULL,
  `quest` int(11) NOT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`serverLevelQuest`,`orderColumn`),
  KEY `FKe96upej3nvnc95rgppqael65c` (`quest`),
  CONSTRAINT `FKe96upej3nvnc95rgppqael65c` FOREIGN KEY (`quest`) REFERENCES `QUEST` (`id`),
  CONSTRAINT `FKg9fruou3eudvjonpkdbu0su25` FOREIGN KEY (`serverLevelQuest`) REFERENCES `SERVER_LEVEL_QUEST` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_RESOURCE_REGION_CONFIG
CREATE TABLE IF NOT EXISTS `SERVER_RESOURCE_REGION_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `minDistanceToItems` double NOT NULL,
  `region_id` int(11) DEFAULT NULL,
  `resourceItemType_id` int(11) DEFAULT NULL,
  `serverGameEngineId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrwr42mqa70kps709cr58h2iir` (`region_id`),
  KEY `FKms0li4pxnifye8592vt0yksum` (`resourceItemType_id`),
  KEY `FKm41ytluw6qxyiba2ft8k7p11d` (`serverGameEngineId`),
  CONSTRAINT `FKm41ytluw6qxyiba2ft8k7p11d` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`),
  CONSTRAINT `FKms0li4pxnifye8592vt0yksum` FOREIGN KEY (`resourceItemType_id`) REFERENCES `RESOURCE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrwr42mqa70kps709cr58h2iir` FOREIGN KEY (`region_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_START_REGION_LEVEL_CONFIG
CREATE TABLE IF NOT EXISTS `SERVER_START_REGION_LEVEL_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `minimalLevel_id` int(11) DEFAULT NULL,
  `serverGameEngineId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK88fp044cjqk0qx3ly7mg8fd8v` (`minimalLevel_id`),
  KEY `FKnv57ds9hqity6ikfkwgighxvl` (`serverGameEngineId`),
  CONSTRAINT `FK88fp044cjqk0qx3ly7mg8fd8v` FOREIGN KEY (`minimalLevel_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKnv57ds9hqity6ikfkwgighxvl` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SERVER_START_REGION_LEVEL_CONFIG_POLYGON
CREATE TABLE IF NOT EXISTS `SERVER_START_REGION_LEVEL_CONFIG_POLYGON` (
  `serverEngineLevelConfigId` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`serverEngineLevelConfigId`,`orderColumn`),
  CONSTRAINT `FKh50214q8u337f2ch25gijaqnc` FOREIGN KEY (`serverEngineLevelConfigId`) REFERENCES `SERVER_START_REGION_LEVEL_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SLOPE_CONFIG
CREATE TABLE IF NOT EXISTS `SLOPE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bmDepth` double NOT NULL,
  `coastDelimiterLineGameEngine` double NOT NULL,
  `fractalClampMax` double NOT NULL,
  `fractalClampMin` double NOT NULL,
  `fractalMax` double NOT NULL,
  `fractalMin` double NOT NULL,
  `fractalRoughness` double NOT NULL,
  `horizontalSpace` double NOT NULL,
  `innerLineGameEngine` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `outerLineGameEngine` double NOT NULL,
  `segments` int(11) NOT NULL,
  `specularHardness` double DEFAULT NULL,
  `specularIntensity` double DEFAULT NULL,
  `textureScale` double NOT NULL,
  `type` varchar(255) NOT NULL,
  `bm_id` int(11) DEFAULT NULL,
  `texture_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh5mpq9b19cf1ielcuicbjn9bq` (`bm_id`),
  KEY `FK52qctp83p38492e8u9ihp2at` (`texture_id`),
  CONSTRAINT `FK52qctp83p38492e8u9ihp2at` FOREIGN KEY (`texture_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKh5mpq9b19cf1ielcuicbjn9bq` FOREIGN KEY (`bm_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SLOPE_DRIVEWAY
CREATE TABLE IF NOT EXISTS `SLOPE_DRIVEWAY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `angle` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SLOPE_NODE
CREATE TABLE IF NOT EXISTS `SLOPE_NODE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
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

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.SLOPE_SHAPE
CREATE TABLE IF NOT EXISTS `SLOPE_SHAPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `slopeFactor` float NOT NULL,
  `shape_id` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKla78vv4yhnh8nh0hn60yj5ite` (`shape_id`),
  CONSTRAINT `FKla78vv4yhnh8nh0hn60yj5ite` FOREIGN KEY (`shape_id`) REFERENCES `SLOPE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TERRAIN_OBJECT
CREATE TABLE IF NOT EXISTS `TERRAIN_OBJECT` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `colladaEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd2dgp25xa48e7tpah4gc4jk00` (`colladaEntity_id`),
  CONSTRAINT `FKd2dgp25xa48e7tpah4gc4jk00` FOREIGN KEY (`colladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TERRAIN_OBJECT_POSITION
CREATE TABLE IF NOT EXISTS `TERRAIN_OBJECT_POSITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `rotationZ` double NOT NULL,
  `scale` double NOT NULL,
  `terrainObjectEntity_id` int(11) NOT NULL,
  `planet` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr6v0ku0sca2p8a2e0oiac4g8f` (`terrainObjectEntity_id`),
  KEY `FKay7f7hjllvmgww9j4e2l1tfqu` (`planet`),
  CONSTRAINT `FKay7f7hjllvmgww9j4e2l1tfqu` FOREIGN KEY (`planet`) REFERENCES `PLANET` (`id`),
  CONSTRAINT `FKr6v0ku0sca2p8a2e0oiac4g8f` FOREIGN KEY (`terrainObjectEntity_id`) REFERENCES `TERRAIN_OBJECT` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TERRAIN_SLOPE_CORNER
CREATE TABLE IF NOT EXISTS `TERRAIN_SLOPE_CORNER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `drivewayConfigEntity_id` int(11) DEFAULT NULL,
  `terrainSlopePositionId` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn3or3udtex818ureyxe80ps47` (`drivewayConfigEntity_id`),
  KEY `FKcge2ffcawpabcjmml8cjsgc7v` (`terrainSlopePositionId`),
  CONSTRAINT `FKcge2ffcawpabcjmml8cjsgc7v` FOREIGN KEY (`terrainSlopePositionId`) REFERENCES `TERRAIN_SLOPE_POSITION` (`id`),
  CONSTRAINT `FKn3or3udtex818ureyxe80ps47` FOREIGN KEY (`drivewayConfigEntity_id`) REFERENCES `SLOPE_DRIVEWAY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TERRAIN_SLOPE_POSITION
CREATE TABLE IF NOT EXISTS `TERRAIN_SLOPE_POSITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inverted` bit(1) NOT NULL,
  `slopeConfigEntity_id` int(11) NOT NULL,
  `parentTerrainSlopePosition` int(11) DEFAULT NULL,
  `planet` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4xqtrjsn0gheqg5og8e9s5mmt` (`slopeConfigEntity_id`),
  KEY `FKmhab0g7928t6vw57a79a17pn5` (`parentTerrainSlopePosition`),
  KEY `FK5ptx8wbuewvwsgwc7uom195h0` (`planet`),
  CONSTRAINT `FK4xqtrjsn0gheqg5og8e9s5mmt` FOREIGN KEY (`slopeConfigEntity_id`) REFERENCES `SLOPE_CONFIG` (`id`),
  CONSTRAINT `FK5ptx8wbuewvwsgwc7uom195h0` FOREIGN KEY (`planet`) REFERENCES `PLANET` (`id`),
  CONSTRAINT `FKmhab0g7928t6vw57a79a17pn5` FOREIGN KEY (`parentTerrainSlopePosition`) REFERENCES `TERRAIN_SLOPE_POSITION` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_CONNECTION
CREATE TABLE IF NOT EXISTS `TRACKER_CONNECTION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `humanPlayerId` int(11) NOT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_FRONTEND_NAVIGATION
CREATE TABLE IF NOT EXISTS `TRACKER_FRONTEND_NAVIGATION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  `url` varchar(190) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_GAME_UI_CONTROL
CREATE TABLE IF NOT EXISTS `TRACKER_GAME_UI_CONTROL` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientStartTime` datetime(3) DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXe1u611bhlj3y6j7lycxdvqp0p` (`sessionId`),
  KEY `IDXm23hwcquflqt4qknm1kdyi6g4` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_PAGE
CREATE TABLE IF NOT EXISTS `TRACKER_PAGE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `page` varchar(255) DEFAULT NULL,
  `params` longtext DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `uri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX8r3ssjnqi1ayq740sslge33bl` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_PERFMON
CREATE TABLE IF NOT EXISTS `TRACKER_PERFMON` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientTimeStamp` datetime(3) DEFAULT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `perfmonEnum` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXjerhb27210brxagvh1igum3oa` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_PERFMON_ENTRY
CREATE TABLE IF NOT EXISTS `TRACKER_PERFMON_ENTRY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `avgDuration` double NOT NULL,
  `date` datetime(3) DEFAULT NULL,
  `frequency` double NOT NULL,
  `samples` int(11) NOT NULL,
  `perfmonStatisticEntity` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7j97gwblgwphwk5xxlo6atcvm` (`perfmonStatisticEntity`),
  CONSTRAINT `FK7j97gwblgwphwk5xxlo6atcvm` FOREIGN KEY (`perfmonStatisticEntity`) REFERENCES `TRACKER_PERFMON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_SCENE
CREATE TABLE IF NOT EXISTS `TRACKER_SCENE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientStartTime` datetime(3) DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX9o1helnck9k2whan5epnp63mp` (`sessionId`),
  KEY `IDX2l3oymcscbqbf34mgmmqqito5` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_SESSION
CREATE TABLE IF NOT EXISTS `TRACKER_SESSION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acceptLanguage` longtext DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `razarionCookie` varchar(190) DEFAULT NULL,
  `referer` longtext DEFAULT NULL,
  `remoteAddr` varchar(255) DEFAULT NULL,
  `remoteHost` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `userAgent` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXtmu9rd272j5lofhd2c8rh6fxh` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_STARTUP_TASK
CREATE TABLE IF NOT EXISTS `TRACKER_STARTUP_TASK` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientStartTime` datetime(3) DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `error` longtext DEFAULT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `startTime` datetime(3) NOT NULL,
  `taskEnum` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXmnp841oq89d322wfkc062dlf5` (`sessionId`),
  KEY `IDXd87lfgp8dxx91d6d0mvp4xri8` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_STARTUP_TERMINATED
CREATE TABLE IF NOT EXISTS `TRACKER_STARTUP_TERMINATED` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `successful` bit(1) NOT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  `totalTime` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXe0iw0eq6smtw9ttdsctibxdrk` (`sessionId`),
  KEY `IDXp06cndke1flthaiq26ruywo52` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_TERRAIN_TILE
CREATE TABLE IF NOT EXISTS `TRACKER_TERRAIN_TILE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientTimeStamp` datetime(3) DEFAULT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `generationTime` int(11) NOT NULL,
  `sessionId` varchar(190) NOT NULL,
  `terrainTileX` int(11) DEFAULT NULL,
  `terrainTileY` int(11) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX2p1x3lnepf6trug4a2fo5f4cb` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.TRACKER_WINDOW_CLOSED
CREATE TABLE IF NOT EXISTS `TRACKER_WINDOW_CLOSED` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientTime` varchar(190) DEFAULT NULL,
  `eventString` varchar(190) DEFAULT NULL,
  `serverTime` datetime(3) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `url` varchar(190) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.USER
CREATE TABLE IF NOT EXISTS `USER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin` bit(1) NOT NULL,
  `crystals` int(11) NOT NULL,
  `email` varchar(190) DEFAULT NULL,
  `facebookUserId` varchar(190) DEFAULT NULL,
  `locale` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `passwordHash` varchar(190) DEFAULT NULL,
  `registerDate` datetime(3) DEFAULT NULL,
  `verificationDoneDate` datetime(3) DEFAULT NULL,
  `verificationId` varchar(190) DEFAULT NULL,
  `verificationStartedDate` datetime(3) DEFAULT NULL,
  `verificationTimedOutDate` datetime(3) DEFAULT NULL,
  `xp` int(11) NOT NULL,
  `activeQuest_id` int(11) DEFAULT NULL,
  `humanPlayerIdEntity_id` int(11) DEFAULT NULL,
  `level_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_g6y5gdrvhgx350bvnhhok8us2` (`name`),
  KEY `IDXoj5g1ob8tb6gn928mukpbqat1` (`facebookUserId`),
  KEY `FKtr8la4tg31fj84o5q5wepu6ai` (`activeQuest_id`),
  KEY `FKdvrsr9xke3jgbsxgaarr9cica` (`humanPlayerIdEntity_id`),
  KEY `FKas5w8de0ic1qgeo8edbs89ffy` (`level_id`),
  CONSTRAINT `FKas5w8de0ic1qgeo8edbs89ffy` FOREIGN KEY (`level_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKdvrsr9xke3jgbsxgaarr9cica` FOREIGN KEY (`humanPlayerIdEntity_id`) REFERENCES `HUMAN_PLAYER_ENTITY` (`id`),
  CONSTRAINT `FKtr8la4tg31fj84o5q5wepu6ai` FOREIGN KEY (`activeQuest_id`) REFERENCES `QUEST` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.USER_COMPLETED_QUEST
CREATE TABLE IF NOT EXISTS `USER_COMPLETED_QUEST` (
  `user` int(11) NOT NULL,
  `quest` int(11) NOT NULL,
  KEY `FKtmrbiae80t1bmnx4w522p7lg2` (`quest`),
  KEY `FKj6dyijv15lcehlwpgf27xiv41` (`user`),
  CONSTRAINT `FKj6dyijv15lcehlwpgf27xiv41` FOREIGN KEY (`user`) REFERENCES `USER` (`id`),
  CONSTRAINT `FKtmrbiae80t1bmnx4w522p7lg2` FOREIGN KEY (`quest`) REFERENCES `QUEST` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.USER_FORGOT_PASSWORD
CREATE TABLE IF NOT EXISTS `USER_FORGOT_PASSWORD` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` datetime(3) DEFAULT NULL,
  `uuid` varchar(190) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlpmfqbev9t9xta6ye08i8om3t` (`user_id`),
  CONSTRAINT `FKlpmfqbev9t9xta6ye08i8om3t` FOREIGN KEY (`user_id`) REFERENCES `USER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.USER_INVENTORY
CREATE TABLE IF NOT EXISTS `USER_INVENTORY` (
  `user` int(11) NOT NULL,
  `inventory` int(11) NOT NULL,
  KEY `FK5aj7t60abpgnng973addr49em` (`inventory`),
  KEY `FKim3foqucksxlw5trj7rooqk4` (`user`),
  CONSTRAINT `FK5aj7t60abpgnng973addr49em` FOREIGN KEY (`inventory`) REFERENCES `INVENTORY_ITEM` (`id`),
  CONSTRAINT `FKim3foqucksxlw5trj7rooqk4` FOREIGN KEY (`user`) REFERENCES `USER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.USER_LOGIN_COOKIE
CREATE TABLE IF NOT EXISTS `USER_LOGIN_COOKIE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timeStamp` datetime(3) DEFAULT NULL,
  `token` varchar(190) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5y2bgq3t4643vc7krcd2nv07v` (`user_id`),
  CONSTRAINT `FK5y2bgq3t4643vc7krcd2nv07v` FOREIGN KEY (`user_id`) REFERENCES `USER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.USER_UNLOCKED
CREATE TABLE IF NOT EXISTS `USER_UNLOCKED` (
  `user` int(11) NOT NULL,
  `levelUnlockEntity` int(11) NOT NULL,
  KEY `FKjxewmpw3sfpmw9pa6gfu0noj1` (`levelUnlockEntity`),
  KEY `FK2nok4m7a1xrs5l5lo5jlcdajf` (`user`),
  CONSTRAINT `FK2nok4m7a1xrs5l5lo5jlcdajf` FOREIGN KEY (`user`) REFERENCES `USER` (`id`),
  CONSTRAINT `FKjxewmpw3sfpmw9pa6gfu0noj1` FOREIGN KEY (`levelUnlockEntity`) REFERENCES `LEVEL_UNLOCK` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
-- Exportiere Struktur von Tabelle razarion.WATER_CONFIG
CREATE TABLE IF NOT EXISTS `WATER_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groundLevel` double NOT NULL,
  `specularHardness` double DEFAULT NULL,
  `specularIntensity` double DEFAULT NULL,
  `waterLevel` double NOT NULL,
  `waterTransparency` double NOT NULL,
  `normMapId_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7dnbi34l47at7gso2rog7yjrk` (`normMapId_id`),
  CONSTRAINT `FK7dnbi34l47at7gso2rog7yjrk` FOREIGN KEY (`normMapId_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Daten Export vom Benutzer nicht ausgewählt
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
