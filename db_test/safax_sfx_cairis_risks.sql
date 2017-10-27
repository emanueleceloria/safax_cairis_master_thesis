-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: localhost    Database: safax
-- ------------------------------------------------------
-- Server version	5.5.54-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `sfx_cairis_risks`
--

DROP TABLE IF EXISTS `sfx_cairis_risks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sfx_cairis_risks` (
  `resourceid` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `resourcename` varchar(45) NOT NULL,
  `threatname` varchar(45) DEFAULT NULL,
  `environmentname` varchar(45) DEFAULT NULL,
  `riskvalue` int(11) NOT NULL,
  PRIMARY KEY (`resourceid`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=ucs2;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sfx_cairis_risks`
--

LOCK TABLES `sfx_cairis_risks` WRITE;
/*!40000 ALTER TABLE `sfx_cairis_risks` DISABLE KEYS */;
INSERT INTO `sfx_cairis_risks` VALUES (0000000001,'configFiles','malware','Day',8),(0000000002,'SCADAHmiFiles','MITM','Day',6),(0000000003,'configFiles','misbehaviour','Night',4),(0000000004,'configFiles','misbehaviour','Day',7),(0000000005,'SCADAHmiFiles','misbehaviour','Night',5),(0000000006,'SCADAHmiFiles','MITM','Night',5);
/*!40000 ALTER TABLE `sfx_cairis_risks` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-10-27 17:03:47
