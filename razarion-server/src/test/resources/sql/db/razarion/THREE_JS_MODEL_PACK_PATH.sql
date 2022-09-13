/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `THREE_JS_MODEL_PACK_PATH` (
  `threeJsModelPackConfig` int(11) NOT NULL,
  `namePath` varchar(255) DEFAULT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`threeJsModelPackConfig`,`orderColumn`),
  CONSTRAINT `FKh15pk4egi4ql744c66g5tw4r7` FOREIGN KEY (`threeJsModelPackConfig`) REFERENCES `THREE_JS_MODEL_PACK` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM `THREE_JS_MODEL_PACK_PATH`;
/*!40000 ALTER TABLE `THREE_JS_MODEL_PACK_PATH` DISABLE KEYS */;
INSERT INTO `THREE_JS_MODEL_PACK_PATH` (`threeJsModelPackConfig`, `namePath`, `orderColumn`) VALUES
	(9, 'Sketchfab_Scene', 0),
	(9, 'Sketchfab_model', 1),
	(9, 'a72b7f83eef84de9b0e32ffa049274defbx', 2),
	(9, 'RootNode', 3),
	(9, 'palmtree_07', 4),
	(10, 'Sketchfab_Scene', 0),
	(10, 'Sketchfab_model', 1),
	(10, 'a72b7f83eef84de9b0e32ffa049274defbx', 2),
	(10, 'RootNode', 3),
	(10, 'palmtree_05', 4),
	(11, 'Sketchfab_Scene', 0),
	(11, 'Sketchfab_model', 1),
	(11, 'de49941573ca4a6cb3fbcf3e4d72f085fbx', 2),
	(11, 'RootNode', 3),
	(11, 'Mid_4_1', 4),
	(11, 'Object_37', 5),
	(11, 'Mid_4_MID_0_1', 6),
	(12, 'Sketchfab_Scene', 0),
	(12, 'Sketchfab_model', 1),
	(12, 'VegetationFBX', 2),
	(12, 'RootNode', 3),
	(12, 'fern', 4),
	(12, 'fern_fern_0', 5),
	(13, 'Sketchfab_Scene', 0),
	(13, 'Sketchfab_model', 1),
	(13, 'VegetationFBX', 2),
	(13, 'RootNode', 3),
	(13, 'fern1', 4),
	(13, 'fern1_fern1_0', 5),
	(14, 'Sketchfab_Scene', 0),
	(14, 'Sketchfab_model', 1),
	(14, 'VegetationFBX', 2),
	(14, 'RootNode', 3),
	(14, 'leaves02', 4),
	(14, 'Object_8', 5),
	(14, 'leaves02_Material_#0_0', 6),
	(15, 'Sketchfab_Scene', 0),
	(15, 'Sketchfab_model', 1),
	(15, 'VegetationFBX', 2),
	(15, 'RootNode', 3),
	(15, 'trunk01', 4),
	(16, 'Sketchfab_Scene', 0),
	(16, 'Sketchfab_model', 1),
	(16, 'VegetationFBX', 2),
	(16, 'RootNode', 3),
	(16, 'palm_plant03', 4),
	(16, 'palm_plant03_palm_plant_0', 5),
	(17, 'Sketchfab_Scene', 0),
	(17, 'Sketchfab_model', 1),
	(17, 'VegetationFBX', 2),
	(17, 'RootNode', 3),
	(17, 'trunk03', 4),
	(17, 'trunk03_Material_#2_0', 5);
/*!40000 ALTER TABLE `THREE_JS_MODEL_PACK_PATH` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
