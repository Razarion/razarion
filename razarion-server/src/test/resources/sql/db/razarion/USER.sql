/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

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
  `level_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_g6y5gdrvhgx350bvnhhok8us2` (`name`),
  KEY `IDXoj5g1ob8tb6gn928mukpbqat1` (`facebookUserId`),
  KEY `FKtr8la4tg31fj84o5q5wepu6ai` (`activeQuest_id`),
  KEY `FKas5w8de0ic1qgeo8edbs89ffy` (`level_id`),
  CONSTRAINT `FKas5w8de0ic1qgeo8edbs89ffy` FOREIGN KEY (`level_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKtr8la4tg31fj84o5q5wepu6ai` FOREIGN KEY (`activeQuest_id`) REFERENCES `QUEST` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=619 DEFAULT CHARSET=utf8mb4;

DELETE FROM `USER`;
/*!40000 ALTER TABLE `USER` DISABLE KEYS */;
INSERT INTO `USER` (`id`, `admin`, `crystals`, `email`, `facebookUserId`, `locale`, `name`, `passwordHash`, `registerDate`, `verificationDoneDate`, `verificationId`, `verificationStartedDate`, `verificationTimedOutDate`, `xp`, `activeQuest_id`, `level_id`) VALUES
	(591, b'1', 0, 'admin@admin.com', NULL, NULL, NULL, 'qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==', NULL, '2020-01-27 20:00:00.000', NULL, NULL, NULL, 0, NULL, NULL),
	(592, b'0', 0, 'user@user.com', NULL, NULL, NULL, 'qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==', NULL, '2020-01-27 20:00:00.000', NULL, NULL, NULL, 0, NULL, NULL),
	(593, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(594, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(595, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(596, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(597, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(598, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(599, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(600, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(601, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(602, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(603, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(604, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(605, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(606, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(607, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(608, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(609, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(610, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(611, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(612, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(613, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(614, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(615, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(616, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(617, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265),
	(618, b'0', 0, NULL, NULL, 'de_DE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 265);
/*!40000 ALTER TABLE `USER` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
