/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

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
) ENGINE=InnoDB AUTO_INCREMENT=223 DEFAULT CHARSET=utf8mb4;

DELETE FROM `BOT_CONFIG`;
/*!40000 ALTER TABLE `BOT_CONFIG` DISABLE KEYS */;
INSERT INTO `BOT_CONFIG` (`id`, `actionDelay`, `autoAttack`, `auxiliaryId`, `internalName`, `maxActiveMs`, `maxInactiveMs`, `minActiveMs`, `minInactiveMs`, `name`, `npc`, `realm_id`) VALUES
	(31, 1000, b'0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, b'0', NULL),
	(222, 3000, b'1', NULL, 'Outpost Mini 1', NULL, NULL, NULL, NULL, 'Raza Outpost Mini', b'0', 697);
/*!40000 ALTER TABLE `BOT_CONFIG` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
