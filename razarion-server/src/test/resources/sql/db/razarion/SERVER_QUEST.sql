/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `SERVER_QUEST` (
  `serverLevelQuest` int(11) NOT NULL,
  `quest` int(11) NOT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`serverLevelQuest`,`orderColumn`),
  KEY `FKe96upej3nvnc95rgppqael65c` (`quest`),
  CONSTRAINT `FKe96upej3nvnc95rgppqael65c` FOREIGN KEY (`quest`) REFERENCES `QUEST` (`id`),
  CONSTRAINT `FKg9fruou3eudvjonpkdbu0su25` FOREIGN KEY (`serverLevelQuest`) REFERENCES `SERVER_LEVEL_QUEST` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM `SERVER_QUEST`;
/*!40000 ALTER TABLE `SERVER_QUEST` DISABLE KEYS */;
/*!40000 ALTER TABLE `SERVER_QUEST` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;