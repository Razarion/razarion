/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM `PROPERTY`;
/*!40000 ALTER TABLE `PROPERTY` DISABLE KEYS */;
/*!40000 ALTER TABLE `PROPERTY` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;