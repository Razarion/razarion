/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `GAME_UI_CONTEXT` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `detailedTracking` bit(1) NOT NULL,
  `gameEngineMode` varchar(255) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `minimalLevel_id` int(11) DEFAULT NULL,
  `planetEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg2h672edbiaseb0sabbdto23x` (`minimalLevel_id`),
  KEY `FKj9wevg184w0jdmabmqk8h93lg` (`planetEntity_id`),
  CONSTRAINT `FKg2h672edbiaseb0sabbdto23x` FOREIGN KEY (`minimalLevel_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKj9wevg184w0jdmabmqk8h93lg` FOREIGN KEY (`planetEntity_id`) REFERENCES `PLANET` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4;

DELETE FROM `GAME_UI_CONTEXT`;
/*!40000 ALTER TABLE `GAME_UI_CONTEXT` DISABLE KEYS */;
INSERT INTO `GAME_UI_CONTEXT` (`id`, `detailedTracking`, `gameEngineMode`, `internalName`, `minimalLevel_id`, `planetEntity_id`) VALUES
	(91, b'0', 'SLAVE', 'Tutorial', 265, 117);
/*!40000 ALTER TABLE `GAME_UI_CONTEXT` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;