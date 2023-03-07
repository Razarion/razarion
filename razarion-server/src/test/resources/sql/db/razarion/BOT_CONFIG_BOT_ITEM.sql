/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

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
) ENGINE=InnoDB AUTO_INCREMENT=530 DEFAULT CHARSET=utf8mb4;

DELETE FROM `BOT_CONFIG_BOT_ITEM`;
/*!40000 ALTER TABLE `BOT_CONFIG_BOT_ITEM` DISABLE KEYS */;
INSERT INTO `BOT_CONFIG_BOT_ITEM` (`id`, `angle`, `count`, `createDirectly`, `idleTtl`, `moveRealmIfIdle`, `noRebuild`, `noSpawn`, `rePopTime`, `baseItemTypeEntity_id`, `place_id`, `botEnragementStateConfig`) VALUES
	(58, 0, 1, b'1', NULL, b'0', b'0', b'0', NULL, 15, 79, 30),
	(59, 0, 1, b'1', NULL, b'0', b'1', b'0', NULL, 5, 80, 30),
	(60, 0, 1, b'1', NULL, b'0', b'1', b'0', NULL, 5, 81, 30),
	(516, 0, 1, b'1', NULL, b'0', b'0', b'0', 600000, 5, 597, 177),
	(517, 0, 1, b'1', NULL, b'0', b'0', b'0', 600000, 5, 598, 177),
	(518, 0, 1, b'1', NULL, b'0', b'0', b'0', 600000, 5, 599, 177),
	(519, 0, 10, b'1', NULL, b'0', b'0', b'0', 600000, 16, 600, 177),
	(520, 0, 1, b'1', NULL, b'0', b'0', b'0', NULL, 13, NULL, 178),
	(521, 0.7853, 1, b'0', NULL, b'0', b'0', b'0', NULL, 22, 602, 178),
	(522, 0, 1, b'0', NULL, b'0', b'0', b'0', 600000, 15, 603, 178),
	(523, 0, 2, b'0', NULL, b'0', b'0', b'0', 1000, 16, NULL, 178),
	(524, 0, 1, b'1', NULL, b'0', b'0', b'0', 600000, 15, 605, 179),
	(525, 0, 2, b'0', NULL, b'0', b'0', b'0', 600000, 16, NULL, 179),
	(526, 0, 1, b'1', NULL, b'0', b'0', b'0', 600000, 15, 607, 180),
	(527, 0, 2, b'0', NULL, b'0', b'0', b'0', 600000, 16, NULL, 180),
	(528, 0, 1, b'1', NULL, b'0', b'0', b'0', 600000, 15, NULL, 181),
	(529, 0, 2, b'0', NULL, b'0', b'0', b'0', 180000, 16, NULL, 181);
/*!40000 ALTER TABLE `BOT_CONFIG_BOT_ITEM` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
