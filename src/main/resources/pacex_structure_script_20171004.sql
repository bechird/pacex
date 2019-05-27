-- MySQL dump 10.13  Distrib 5.7.17, for macos10.12 (x86_64)
--
-- Host: localhost    Database: pacex
-- ------------------------------------------------------
-- Server version	5.7.17

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
-- Table structure for table `Company`
--

DROP TABLE IF EXISTS `Company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Company` (
  `id` varchar(255) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `tva` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrderBl`
--

DROP TABLE IF EXISTS `OrderBl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OrderBl` (
  `id` bigint(20) NOT NULL,
  `qty` int(11) NOT NULL,
  `bonLivraison_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKev79ge9bcnoxibogoq6wsqg1l` (`bonLivraison_id`),
  CONSTRAINT `FKev79ge9bcnoxibogoq6wsqg1l` FOREIGN KEY (`bonLivraison_id`) REFERENCES `deliveryNote` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrderPackages`
--

DROP TABLE IF EXISTS `OrderPackages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OrderPackages` (
  `orderId` int(11) NOT NULL,
  `packageId` bigint(20) NOT NULL,
  PRIMARY KEY (`orderId`,`packageId`),
  UNIQUE KEY `UK_3nbothqscct5q4y411txu6xe` (`packageId`),
  CONSTRAINT `FK599ni6ci2k3p9srvlf0i0on1f` FOREIGN KEY (`packageId`) REFERENCES `Package` (`packageId`),
  CONSTRAINT `FKhp0p2wr3dbidnd6ksucqne9m0` FOREIGN KEY (`orderId`) REFERENCES `order_T` (`Order_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrderParts`
--

DROP TABLE IF EXISTS `OrderParts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OrderParts` (
  `orderId` int(11) NOT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`orderId`,`id`),
  UNIQUE KEY `UK_mmt8n1hpxbf27bdbbg5ud4aeq` (`id`),
  CONSTRAINT `FKdex1t5jxxiqy3jux2wfgp5fj9` FOREIGN KEY (`orderId`) REFERENCES `order_T` (`Order_Id`),
  CONSTRAINT `FKnykn69nen0r63v8v06r8xtu47` FOREIGN KEY (`id`) REFERENCES `order_Part` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Package`
--

DROP TABLE IF EXISTS `Package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Package` (
  `packageId` bigint(20) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  PRIMARY KEY (`packageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PackageBook`
--

DROP TABLE IF EXISTS `PackageBook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PackageBook` (
  `packagePartId` bigint(20) NOT NULL,
  `barcode` varchar(255) DEFAULT NULL,
  `bookId` varchar(255) DEFAULT NULL,
  `delivered` int(11) DEFAULT NULL,
  `depthQty` int(11) DEFAULT NULL,
  `heightQty` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `widthQty` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`packagePartId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Package_Package`
--

DROP TABLE IF EXISTS `Package_Package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Package_Package` (
  `Package_packageId` bigint(20) NOT NULL,
  `packages_packageId` bigint(20) NOT NULL,
  PRIMARY KEY (`Package_packageId`,`packages_packageId`),
  UNIQUE KEY `UK_lh6hshe5fc74ogyyds3gal0fu` (`packages_packageId`),
  CONSTRAINT `FKfk5elgca5gp6tr2o0kanbd0s` FOREIGN KEY (`Package_packageId`) REFERENCES `Package` (`packageId`),
  CONSTRAINT `FKqidvtw1piwjg1nu9hvtopatb5` FOREIGN KEY (`packages_packageId`) REFERENCES `Package` (`packageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Package_PackageBook`
--

DROP TABLE IF EXISTS `Package_PackageBook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Package_PackageBook` (
  `Package_packageId` bigint(20) NOT NULL,
  `pcbs_packagePartId` bigint(20) NOT NULL,
  PRIMARY KEY (`Package_packageId`,`pcbs_packagePartId`),
  UNIQUE KEY `UK_5dxtrrn7d7e34wh1rgaarosby` (`pcbs_packagePartId`),
  CONSTRAINT `FKdkbwj1ybb1s0anugxhag6smpu` FOREIGN KEY (`pcbs_packagePartId`) REFERENCES `PackageBook` (`packagePartId`),
  CONSTRAINT `FKei51rel6al04opa1nl6p4w7l7` FOREIGN KEY (`Package_packageId`) REFERENCES `Package` (`packageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Pallette`
--

DROP TABLE IF EXISTS `Pallette`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Pallette` (
  `id` bigint(20) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `Machine_Id` varchar(255) DEFAULT NULL,
  `palletteName` varchar(255) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `Customer_Id` int(11) DEFAULT NULL,
  `delivredDate` datetime DEFAULT NULL,
  `blNumber` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp4p47u7x3e9qy7a94gv5rkq9l` (`Customer_Id`),
  KEY `FKpwgjroa14whai0fk3b29n67ps` (`Machine_Id`),
  CONSTRAINT `FKp4p47u7x3e9qy7a94gv5rkq9l` FOREIGN KEY (`Customer_Id`) REFERENCES `customer` (`Customer_Id`),
  CONSTRAINT `FKpwgjroa14whai0fk3b29n67ps` FOREIGN KEY (`Machine_Id`) REFERENCES `machine` (`Machine_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PalletteBook`
--

DROP TABLE IF EXISTS `PalletteBook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PalletteBook` (
  `packagePartId` bigint(20) NOT NULL,
  `pallette_Id` bigint(20) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  PRIMARY KEY (`packagePartId`,`pallette_Id`),
  KEY `FKmdp75cxnulgvbq0cpo0dl97de` (`pallette_Id`),
  CONSTRAINT `FKjnks79mlm75ewx75y2ywm6e53` FOREIGN KEY (`packagePartId`) REFERENCES `PackageBook` (`packagePartId`),
  CONSTRAINT `FKmdp75cxnulgvbq0cpo0dl97de` FOREIGN KEY (`pallette_Id`) REFERENCES `Pallette` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_Action`
--

DROP TABLE IF EXISTS `WFS_Action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_Action` (
  `idaction` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idaction`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_Datasupport`
--

DROP TABLE IF EXISTS `WFS_Datasupport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_Datasupport` (
  `iddatasupport` int(11) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parameter1` varchar(255) DEFAULT NULL,
  `parameter2` varchar(255) DEFAULT NULL,
  `parameter3` varchar(255) DEFAULT NULL,
  `parameter4` varchar(255) DEFAULT NULL,
  `parameter5` varchar(255) DEFAULT NULL,
  `parameter6` varchar(255) DEFAULT NULL,
  `parameter7` varchar(255) DEFAULT NULL,
  `partnumb` varchar(255) DEFAULT NULL,
  `progress_Id` int(11) DEFAULT NULL,
  `productionstatusid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`iddatasupport`),
  KEY `FKhhvvicyym0ln4gndi5c6q4gmt` (`productionstatusid`),
  KEY `FKtm642j338alsk5qs6sehf7end` (`partnumb`),
  CONSTRAINT `FKhhvvicyym0ln4gndi5c6q4gmt` FOREIGN KEY (`productionstatusid`) REFERENCES `WFS_ProductionStatus` (`productionStatusId`),
  CONSTRAINT `FKtm642j338alsk5qs6sehf7end` FOREIGN KEY (`partnumb`) REFERENCES `part` (`Part_Num`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_Location`
--

DROP TABLE IF EXISTS `WFS_Location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_Location` (
  `idlocation` int(11) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `protocol` varchar(255) DEFAULT NULL,
  `dataSupportId` int(11) NOT NULL,
  PRIMARY KEY (`idlocation`),
  KEY `FKly06bb230dfun4hbtfp7gf1r1` (`dataSupportId`),
  CONSTRAINT `FKly06bb230dfun4hbtfp7gf1r1` FOREIGN KEY (`dataSupportId`) REFERENCES `WFS_Datasupport` (`iddatasupport`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_Part_Workflow`
--

DROP TABLE IF EXISTS `WFS_Part_Workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_Part_Workflow` (
  `partWorkflowId` int(11) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `wf_isready` bit(1) DEFAULT NULL,
  `partNum` varchar(255) DEFAULT NULL,
  `wf_rollWidth` float DEFAULT NULL,
  `wf_status` varchar(255) DEFAULT NULL,
  `workflowId` int(11) DEFAULT NULL,
  PRIMARY KEY (`partWorkflowId`),
  KEY `FKgow1rti29l2u0nt0pedi9pyfu` (`workflowId`),
  KEY `FK41ou58xsxr9tsyhfgtsrr79u` (`partNum`),
  CONSTRAINT `FK41ou58xsxr9tsyhfgtsrr79u` FOREIGN KEY (`partNum`) REFERENCES `part` (`Part_Num`),
  CONSTRAINT `FKgow1rti29l2u0nt0pedi9pyfu` FOREIGN KEY (`workflowId`) REFERENCES `WFS_Workflow` (`idworkflow`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_ProductionStatus`
--

DROP TABLE IF EXISTS `WFS_ProductionStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_ProductionStatus` (
  `productionStatusId` varchar(255) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(55) NOT NULL,
  PRIMARY KEY (`productionStatusId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_Progress`
--

DROP TABLE IF EXISTS `WFS_Progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_Progress` (
  `progressid` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `ends` bigint(20) DEFAULT NULL,
  `starts` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `dataSupport_Id` int(11) DEFAULT NULL,
  `partWorkflowId` int(11) DEFAULT NULL,
  `sequenceid` int(11) DEFAULT NULL,
  PRIMARY KEY (`progressid`),
  KEY `FKkhyly151yobq0558v741u9bsc` (`dataSupport_Id`),
  KEY `FKsxg5j2ps2mvimgveqcheg3s8v` (`partWorkflowId`),
  KEY `FKi26y5srfnuhaf1mnw87q032yj` (`sequenceid`),
  CONSTRAINT `FKi26y5srfnuhaf1mnw87q032yj` FOREIGN KEY (`sequenceid`) REFERENCES `WFS_Sequence` (`idsequence`),
  CONSTRAINT `FKkhyly151yobq0558v741u9bsc` FOREIGN KEY (`dataSupport_Id`) REFERENCES `WFS_Datasupport` (`iddatasupport`),
  CONSTRAINT `FKsxg5j2ps2mvimgveqcheg3s8v` FOREIGN KEY (`partWorkflowId`) REFERENCES `WFS_Part_Workflow` (`partWorkflowId`)
) ENGINE=InnoDB AUTO_INCREMENT=153 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_Sequence`
--

DROP TABLE IF EXISTS `WFS_Sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_Sequence` (
  `idsequence` int(11) NOT NULL AUTO_INCREMENT,
  `workflowid` int(11) DEFAULT NULL,
  `ranking` int(11) DEFAULT NULL,
  `actionid` int(11) DEFAULT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idsequence`),
  KEY `FKe3bom8gg6vnxjsbvdfe6j6rja` (`actionid`),
  KEY `FKspt036qb0kw2qag15p1h6wffm` (`workflowid`),
  CONSTRAINT `FKe3bom8gg6vnxjsbvdfe6j6rja` FOREIGN KEY (`actionid`) REFERENCES `WFS_Action` (`idaction`),
  CONSTRAINT `FKspt036qb0kw2qag15p1h6wffm` FOREIGN KEY (`workflowid`) REFERENCES `WFS_Workflow` (`idworkflow`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WFS_Workflow`
--

DROP TABLE IF EXISTS `WFS_Workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WFS_Workflow` (
  `idworkflow` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `enable` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idworkflow`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `binding_Type`
--

DROP TABLE IF EXISTS `binding_Type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `binding_Type` (
  `Binding_Type_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` longtext,
  `Name` varchar(255) NOT NULL,
  PRIMARY KEY (`Binding_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bonLivraison`
--

DROP TABLE IF EXISTS `bonLivraison`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bonLivraison` (
  `id` bigint(20) NOT NULL,
  `creationDate` datetime DEFAULT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `num` int(11) NOT NULL,
  `qty` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `critiria`
--

DROP TABLE IF EXISTS `critiria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `critiria` (
  `Critiria_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  `Active_Flag` int(11) DEFAULT NULL,
  PRIMARY KEY (`Critiria_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customer` (
  `Customer_Id` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Email` varchar(70) DEFAULT NULL,
  `First_Name` varchar(50) NOT NULL,
  `Last_Name` varchar(50) NOT NULL,
  `Phone_Num` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`Customer_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=256 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `default_Station`
--

DROP TABLE IF EXISTS `default_Station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `default_Station` (
  `Binding_Type_Id` varchar(25) NOT NULL,
  `Category_Id` varchar(25) NOT NULL,
  `Critiria_Id` varchar(25) NOT NULL,
  `Station_Category_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Production_Ordering` int(11) DEFAULT NULL,
  PRIMARY KEY (`Binding_Type_Id`,`Category_Id`,`Critiria_Id`,`Station_Category_Id`),
  KEY `FK9scn8r1d1aoc0xp57ky1tl136` (`Category_Id`),
  KEY `FKjy9y96twn3t2ugc00ll49cauk` (`Station_Category_Id`),
  CONSTRAINT `FK9scn8r1d1aoc0xp57ky1tl136` FOREIGN KEY (`Category_Id`) REFERENCES `part_Category` (`Category_Id`),
  CONSTRAINT `FKjy9y96twn3t2ugc00ll49cauk` FOREIGN KEY (`Station_Category_Id`) REFERENCES `station_Category` (`Category_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `deliveryNote`
--

DROP TABLE IF EXISTS `deliveryNote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `deliveryNote` (
  `id` bigint(20) NOT NULL,
  `creationDate` datetime DEFAULT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `num` int(11) NOT NULL,
  `qty` int(11) NOT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job` (
  `Job_Id` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `File_Sent_Flag` bit(1) DEFAULT NULL,
  `Hours` float DEFAULT NULL,
  `Machine_Id` varchar(255) DEFAULT NULL,
  `Machine_Ordering` int(11) DEFAULT NULL,
  `Production_Ordering` int(11) DEFAULT NULL,
  `Quantity_Needed` int(11) DEFAULT NULL,
  `Quantity_Produced` float DEFAULT NULL,
  `Roll_Id` int(11) DEFAULT NULL,
  `Roll_Ordering` int(11) DEFAULT NULL,
  `Split_Level` int(11) DEFAULT NULL,
  `Station_Id` varchar(255) DEFAULT NULL,
  `Bindery_Priority` varchar(25) DEFAULT NULL,
  `Job_Priority` varchar(25) DEFAULT NULL,
  `Status` varchar(25) DEFAULT NULL,
  `Job_Type` varchar(25) DEFAULT NULL,
  `Order_Id` int(11) DEFAULT NULL,
  `Part_Num` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`Job_Id`),
  KEY `FKl5ilyo9d3sew82gmktcaf6vgm` (`Bindery_Priority`),
  KEY `FKsysqkpxqyakmg88lcxh7gejiq` (`Job_Priority`),
  KEY `FKhkhvvrmt2a87qkkdh7oxh194l` (`Status`),
  KEY `FKorulpoblb2ij3clg8iwav61qy` (`Job_Type`),
  KEY `FK9iduawbm58wrtc0bl90dir3hi` (`Order_Id`),
  KEY `FKt3bw4i6ruct7by7qkgniicug8` (`Part_Num`),
  KEY `FKocbht09iyekrdbdvwsu44ubwf` (`Machine_Id`),
  KEY `FK19qvhsb1fencfftfx3qywj9fe` (`Roll_Id`),
  CONSTRAINT `FK19qvhsb1fencfftfx3qywj9fe` FOREIGN KEY (`Roll_Id`) REFERENCES `roll` (`Roll_Id`),
  CONSTRAINT `FK9iduawbm58wrtc0bl90dir3hi` FOREIGN KEY (`Order_Id`) REFERENCES `order_T` (`Order_Id`),
  CONSTRAINT `FKhkhvvrmt2a87qkkdh7oxh194l` FOREIGN KEY (`Status`) REFERENCES `job_Status` (`Job_Status_Id`),
  CONSTRAINT `FKl5ilyo9d3sew82gmktcaf6vgm` FOREIGN KEY (`Bindery_Priority`) REFERENCES `priority` (`Priority_Id`),
  CONSTRAINT `FKocbht09iyekrdbdvwsu44ubwf` FOREIGN KEY (`Machine_Id`) REFERENCES `machine` (`Machine_Id`),
  CONSTRAINT `FKorulpoblb2ij3clg8iwav61qy` FOREIGN KEY (`Job_Type`) REFERENCES `job_Type` (`Job_Type_Id`),
  CONSTRAINT `FKsysqkpxqyakmg88lcxh7gejiq` FOREIGN KEY (`Job_Priority`) REFERENCES `priority` (`Priority_Id`),
  CONSTRAINT `FKt3bw4i6ruct7by7qkgniicug8` FOREIGN KEY (`Part_Num`) REFERENCES `part` (`Part_Num`)
) ENGINE=InnoDB AUTO_INCREMENT=296 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_Status`
--

DROP TABLE IF EXISTS `job_Status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job_Status` (
  `Job_Status_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Job_Status_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_Type`
--

DROP TABLE IF EXISTS `job_Type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job_Type` (
  `Job_Type_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Job_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lamination_Type`
--

DROP TABLE IF EXISTS `lamination_Type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lamination_Type` (
  `Lamination_Type_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Lamination_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `load_Tag`
--

DROP TABLE IF EXISTS `load_Tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `load_Tag` (
  `Load_Tag_Id` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Cart_Num` varchar(15) DEFAULT NULL,
  `Finish_Time` datetime DEFAULT NULL,
  `Job_Id` int(11) DEFAULT NULL,
  `Machine_Id` varchar(255) DEFAULT NULL,
  `Quantity` float DEFAULT NULL,
  `Start_Time` datetime DEFAULT NULL,
  `Tag_Num` varchar(10) DEFAULT NULL,
  `Used_Flag` bit(1) DEFAULT NULL,
  `Waste` float DEFAULT NULL,
  PRIMARY KEY (`Load_Tag_Id`),
  KEY `FK7ddsnv3m0rcsi04g2ojx82jmc` (`Job_Id`),
  CONSTRAINT `FK7ddsnv3m0rcsi04g2ojx82jmc` FOREIGN KEY (`Job_Id`) REFERENCES `job` (`Job_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log`
--

DROP TABLE IF EXISTS `log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log` (
  `Log_Id` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Counter_Feet` bigint(20) DEFAULT NULL,
  `Current_Job_Id` int(11) DEFAULT NULL,
  `Event` varchar(255) DEFAULT NULL,
  `Finish_Time` datetime DEFAULT NULL,
  `Machine_Id` varchar(255) DEFAULT NULL,
  `Notes` varchar(255) DEFAULT NULL,
  `Roll_Id` int(11) DEFAULT NULL,
  `Roll_Length` int(11) DEFAULT NULL,
  `Start_Time` datetime DEFAULT NULL,
  `Cause` varchar(25) DEFAULT NULL,
  `Result` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`Log_Id`),
  KEY `FKn85l3ys0uwiuhpcysuclte7c1` (`Cause`),
  KEY `FKjkvqfbf3959m1na3bm1uchhn6` (`Result`),
  KEY `FKcwj6rfmkytscr8p0cw9cq66n1` (`Machine_Id`),
  CONSTRAINT `FKcwj6rfmkytscr8p0cw9cq66n1` FOREIGN KEY (`Machine_Id`) REFERENCES `machine` (`Machine_Id`),
  CONSTRAINT `FKjkvqfbf3959m1na3bm1uchhn6` FOREIGN KEY (`Result`) REFERENCES `log_Result` (`Log_Result_Id`),
  CONSTRAINT `FKn85l3ys0uwiuhpcysuclte7c1` FOREIGN KEY (`Cause`) REFERENCES `log_Cause` (`Log_Cause_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log_Cause`
--

DROP TABLE IF EXISTS `log_Cause`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log_Cause` (
  `Log_Cause_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Log_Cause_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log_Result`
--

DROP TABLE IF EXISTS `log_Result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log_Result` (
  `Log_Result_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Log_Result_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `machine`
--

DROP TABLE IF EXISTS `machine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `machine` (
  `Machine_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` longtext,
  `IpAddress` varchar(45) DEFAULT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Port` int(11) DEFAULT NULL,
  `OcInputPath` varchar(100) DEFAULT NULL,
  `Service_Schedule` varchar(100) DEFAULT NULL,
  `Speed` varchar(15) DEFAULT NULL,
  `Station_Id` varchar(25) DEFAULT NULL,
  `Current_Job_Id` int(11) DEFAULT NULL,
  `MachineType` varchar(25) DEFAULT NULL,
  `Status` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`Machine_Id`),
  KEY `FKkctx04atehtadsuf9qld2p08u` (`Current_Job_Id`),
  KEY `FKkulqrj6wpffei523sk7dojqas` (`MachineType`),
  KEY `FKcn5ysdfngxxjoejpp7i1o2c88` (`Status`),
  KEY `FK6w0ktnl857vmilmn4thkebxnq` (`Station_Id`),
  CONSTRAINT `FK6w0ktnl857vmilmn4thkebxnq` FOREIGN KEY (`Station_Id`) REFERENCES `station` (`Station_Id`),
  CONSTRAINT `FKcn5ysdfngxxjoejpp7i1o2c88` FOREIGN KEY (`Status`) REFERENCES `machine_Status` (`Machine_Status_Id`),
  CONSTRAINT `FKkctx04atehtadsuf9qld2p08u` FOREIGN KEY (`Current_Job_Id`) REFERENCES `job` (`Job_Id`),
  CONSTRAINT `FKkulqrj6wpffei523sk7dojqas` FOREIGN KEY (`MachineType`) REFERENCES `machine_Type` (`Machine_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `machine_Status`
--

DROP TABLE IF EXISTS `machine_Status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `machine_Status` (
  `Machine_Status_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Machine_Status_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `machine_Type`
--

DROP TABLE IF EXISTS `machine_Type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `machine_Type` (
  `Machine_Type_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  `Station_Category_Id` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`Machine_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_access_token`
--

DROP TABLE IF EXISTS `oauth_access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(256) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_client_details`
--

DROP TABLE IF EXISTS `oauth_client_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_client_details` (
  `client_id` varchar(256) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_code`
--

DROP TABLE IF EXISTS `oauth_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_code` (
  `code` varchar(256) DEFAULT NULL,
  `authentication` blob
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_refresh_token`
--

DROP TABLE IF EXISTS `oauth_refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication` blob
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_Part`
--

DROP TABLE IF EXISTS `order_Part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_Part` (
  `id` bigint(20) NOT NULL,
  `Printing_Hours` float DEFAULT NULL,
  `Quantity` int(11) DEFAULT NULL,
  `Quantity_Max` int(11) DEFAULT NULL,
  `Quantity_Min` int(11) DEFAULT NULL,
  `Part_Num` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfxv2t47sjh3jbr40906swbxu8` (`Part_Num`),
  CONSTRAINT `FKfxv2t47sjh3jbr40906swbxu8` FOREIGN KEY (`Part_Num`) REFERENCES `part` (`Part_Num`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_Status`
--

DROP TABLE IF EXISTS `order_Status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_Status` (
  `Order_Status_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Order_Status_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_T`
--

DROP TABLE IF EXISTS `order_T`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_T` (
  `Order_Id` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Due_Date` datetime DEFAULT NULL,
  `Notes` longtext,
  `Order_Num` varchar(25) NOT NULL,
  `Priority_Level` varchar(255) DEFAULT NULL,
  `Production_Mode` varchar(45) DEFAULT NULL,
  `Recieved_Date` datetime DEFAULT NULL,
  `Source` varchar(15) DEFAULT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `Customer_Id` int(11) DEFAULT NULL,
  `bonLivraison_id` bigint(20) DEFAULT NULL,
  `completeDate` datetime DEFAULT NULL,
  `company_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Order_Id`),
  KEY `FK7fl53nyg8b7nd3fwtm6doqcbq` (`Customer_Id`),
  KEY `FKmknj282fm04l5wdhctjx1s3lm` (`bonLivraison_id`),
  KEY `FKrj0bcixoofl6jrj7s24chds1i` (`company_id`),
  CONSTRAINT `FK7fl53nyg8b7nd3fwtm6doqcbq` FOREIGN KEY (`Customer_Id`) REFERENCES `customer` (`Customer_Id`),
  CONSTRAINT `FKb2w9iw3dkqh7a199u7hs2ki7f` FOREIGN KEY (`bonLivraison_id`) REFERENCES `bonLivraison` (`id`),
  CONSTRAINT `FKmknj282fm04l5wdhctjx1s3lm` FOREIGN KEY (`bonLivraison_id`) REFERENCES `deliveryNote` (`id`),
  CONSTRAINT `FKrj0bcixoofl6jrj7s24chds1i` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_T_OrderBl`
--

DROP TABLE IF EXISTS `order_T_OrderBl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_T_OrderBl` (
  `Order_Order_Id` int(11) NOT NULL,
  `order_Bl_id` bigint(20) NOT NULL,
  PRIMARY KEY (`Order_Order_Id`,`order_Bl_id`),
  KEY `FKh3ech40afljfdr7vls09hfwwp` (`order_Bl_id`),
  CONSTRAINT `FK6k8tb557tern4nir8x0h60rue` FOREIGN KEY (`Order_Order_Id`) REFERENCES `order_T` (`Order_Id`),
  CONSTRAINT `FKh3ech40afljfdr7vls09hfwwp` FOREIGN KEY (`order_Bl_id`) REFERENCES `OrderBl` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_T_deliveryNote`
--

DROP TABLE IF EXISTS `order_T_deliveryNote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_T_deliveryNote` (
  `Order_Order_Id` int(11) NOT NULL,
  `bonLivraisons_id` bigint(20) NOT NULL,
  PRIMARY KEY (`Order_Order_Id`,`bonLivraisons_id`),
  KEY `FK1vs87n8wh5n60o74s49xh3ho0` (`bonLivraisons_id`),
  CONSTRAINT `FK1vs87n8wh5n60o74s49xh3ho0` FOREIGN KEY (`bonLivraisons_id`) REFERENCES `deliveryNote` (`id`),
  CONSTRAINT `FKenp4lylbgiyb4u17wyh5ryub3` FOREIGN KEY (`Order_Order_Id`) REFERENCES `order_T` (`Order_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper_Type`
--

DROP TABLE IF EXISTS `paper_Type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paper_Type` (
  `Paper_Type_Id` varchar(60) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(60) NOT NULL,
  `Thickness` float DEFAULT NULL,
  `Weight` float DEFAULT NULL,
  `dropFolder` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Paper_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper_Type_Media`
--

DROP TABLE IF EXISTS `paper_Type_Media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paper_Type_Media` (
  `Paper_Type_Media_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  `Paper_Type_Id` varchar(255) DEFAULT NULL,
  `Roll_Length` int(11) DEFAULT NULL,
  `Roll_Width` float DEFAULT NULL,
  PRIMARY KEY (`Paper_Type_Media_Id`),
  KEY `FKak266oj17m3plphoqu3gu3eaf` (`Paper_Type_Id`),
  CONSTRAINT `FKak266oj17m3plphoqu3gu3eaf` FOREIGN KEY (`Paper_Type_Id`) REFERENCES `paper_Type` (`Paper_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `part`
--

DROP TABLE IF EXISTS `part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `part` (
  `Part_Num` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Active_Flag` bit(1) DEFAULT NULL,
  `Author` varchar(255) DEFAULT NULL,
  `Colors` varchar(15) NOT NULL,
  `CoverColor` varchar(15) NOT NULL,
  `ISBN` varchar(25) NOT NULL,
  `Last_Printed` date DEFAULT NULL,
  `Length` float DEFAULT NULL,
  `Notes` longtext,
  `Pages_Count` int(11) DEFAULT NULL,
  `Publish_Date` date DEFAULT NULL,
  `ReadyToProduce` bit(1) DEFAULT NULL,
  `Soft_Delete` bit(1) DEFAULT NULL,
  `Thickness` float DEFAULT NULL,
  `Title` varchar(255) DEFAULT NULL,
  `Version` int(11) DEFAULT NULL,
  `Width` float DEFAULT NULL,
  `Binding_Type_Id` varchar(25) DEFAULT NULL,
  `Category_Id` varchar(25) DEFAULT NULL,
  `Lamination` varchar(25) DEFAULT NULL,
  `Paper_Type` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`Part_Num`),
  KEY `FKgf5n7o78wxfixkpqln2eem162` (`Binding_Type_Id`),
  KEY `FKyqwoksfqajd1sly0xdiunbqo` (`Category_Id`),
  KEY `FK1j9r9j73ylkvmmcuiowlgn92h` (`Lamination`),
  KEY `FK1qgigvv511ew6virmjbt095os` (`Paper_Type`),
  CONSTRAINT `FK1j9r9j73ylkvmmcuiowlgn92h` FOREIGN KEY (`Lamination`) REFERENCES `lamination_Type` (`Lamination_Type_Id`),
  CONSTRAINT `FK1qgigvv511ew6virmjbt095os` FOREIGN KEY (`Paper_Type`) REFERENCES `paper_Type` (`Paper_Type_Id`),
  CONSTRAINT `FKgf5n7o78wxfixkpqln2eem162` FOREIGN KEY (`Binding_Type_Id`) REFERENCES `binding_Type` (`Binding_Type_Id`),
  CONSTRAINT `FKyqwoksfqajd1sly0xdiunbqo` FOREIGN KEY (`Category_Id`) REFERENCES `part_Category` (`Category_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `part_Category`
--

DROP TABLE IF EXISTS `part_Category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `part_Category` (
  `Category_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` longtext,
  `Name` varchar(255) NOT NULL,
  PRIMARY KEY (`Category_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `part_Critiria`
--

DROP TABLE IF EXISTS `part_Critiria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `part_Critiria` (
  `Critiria_Id` varchar(25) NOT NULL,
  `Part_Num` varchar(25) NOT NULL,
  PRIMARY KEY (`Critiria_Id`,`Part_Num`),
  KEY `FKa70a99qm47t07i2hi7n60ikd5` (`Part_Num`),
  CONSTRAINT `FKa70a99qm47t07i2hi7n60ikd5` FOREIGN KEY (`Part_Num`) REFERENCES `part` (`Part_Num`),
  CONSTRAINT `FKh6hyw54n61dkodrb1742bv3w6` FOREIGN KEY (`Critiria_Id`) REFERENCES `critiria` (`Critiria_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `part_WFS_Datasupport`
--

DROP TABLE IF EXISTS `part_WFS_Datasupport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `part_WFS_Datasupport` (
  `Part_Part_Num` varchar(25) NOT NULL,
  `dataSupports_iddatasupport` int(11) NOT NULL,
  PRIMARY KEY (`Part_Part_Num`,`dataSupports_iddatasupport`),
  UNIQUE KEY `UK_a5x27tya5yxj39xa3jmhiwa76` (`dataSupports_iddatasupport`),
  CONSTRAINT `FKc0joi4xoggdnf9a6pp9y000db` FOREIGN KEY (`Part_Part_Num`) REFERENCES `part` (`Part_Num`),
  CONSTRAINT `FKe86pxcrhu58rbhccpug35mlu` FOREIGN KEY (`dataSupports_iddatasupport`) REFERENCES `WFS_Datasupport` (`iddatasupport`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `preference`
--

DROP TABLE IF EXISTS `preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `preference` (
  `Preference_Id` varchar(255) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` longtext,
  `Value` longtext NOT NULL,
  `Group_Id` int(11) DEFAULT NULL,
  PRIMARY KEY (`Preference_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `priority`
--

DROP TABLE IF EXISTS `priority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `priority` (
  `Priority_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Priority_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `Role_Id` varchar(50) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Role_Description` varchar(255) DEFAULT NULL,
  `Role_Name` varchar(100) NOT NULL,
  PRIMARY KEY (`Role_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roll`
--

DROP TABLE IF EXISTS `roll`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roll` (
  `Roll_Id` int(11) NOT NULL AUTO_INCREMENT,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `CopyStatus` varchar(255) DEFAULT NULL,
  `Hours` float DEFAULT NULL,
  `Length` int(11) DEFAULT NULL,
  `Machine_Id` varchar(255) DEFAULT NULL,
  `Machine_Ordering` int(11) DEFAULT NULL,
  `Parent_Roll_Id` int(11) DEFAULT NULL,
  `Roll_Num` varchar(15) DEFAULT NULL,
  `Roll_Tag` varchar(25) DEFAULT NULL,
  `Utilization` int(11) DEFAULT NULL,
  `Weight` int(11) DEFAULT NULL,
  `Width` float DEFAULT NULL,
  `Paper_Type` varchar(60) DEFAULT NULL,
  `Roll_Type` varchar(25) DEFAULT NULL,
  `Status` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`Roll_Id`),
  KEY `FKn9c8oip2bveuokhij0dwblqss` (`Paper_Type`),
  KEY `FKg8kpsiqu4cix0db9pjv8kqpo6` (`Roll_Type`),
  KEY `FK770iu5yi3uj6gae61ixjfujff` (`Status`),
  KEY `FKb5hix3jonq8obb7wb3jhddq6b` (`Machine_Id`),
  CONSTRAINT `FK770iu5yi3uj6gae61ixjfujff` FOREIGN KEY (`Status`) REFERENCES `roll_status` (`Roll_Status_Id`),
  CONSTRAINT `FKb5hix3jonq8obb7wb3jhddq6b` FOREIGN KEY (`Machine_Id`) REFERENCES `machine` (`Machine_Id`),
  CONSTRAINT `FKg8kpsiqu4cix0db9pjv8kqpo6` FOREIGN KEY (`Roll_Type`) REFERENCES `roll_Type` (`Roll_Type_Id`),
  CONSTRAINT `FKn9c8oip2bveuokhij0dwblqss` FOREIGN KEY (`Paper_Type`) REFERENCES `paper_Type` (`Paper_Type_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roll_Type`
--

DROP TABLE IF EXISTS `roll_Type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roll_Type` (
  `Roll_Type_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Roll_Type_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roll_status`
--

DROP TABLE IF EXISTS `roll_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roll_status` (
  `Roll_Status_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(55) NOT NULL,
  PRIMARY KEY (`Roll_Status_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `station`
--

DROP TABLE IF EXISTS `station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `station` (
  `Station_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Active_Flag` bit(1) DEFAULT NULL,
  `Description` longtext,
  `Input_Type` varchar(255) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Parent_Station_Id` varchar(255) DEFAULT NULL,
  `Production_Capacity` float DEFAULT NULL,
  `Production_Ordering` int(11) DEFAULT NULL,
  `Scheduled_Hours` float DEFAULT NULL,
  `Station_Category_Id` varchar(255) DEFAULT NULL,
  `Unscheduled_Hours` float DEFAULT NULL,
  PRIMARY KEY (`Station_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `station_Category`
--

DROP TABLE IF EXISTS `station_Category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `station_Category` (
  `Category_Id` varchar(25) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Description` longtext,
  `Name` varchar(255) NOT NULL,
  PRIMARY KEY (`Category_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sub_Part`
--

DROP TABLE IF EXISTS `sub_Part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sub_Part` (
  `Sub_Part_Num` varchar(25) NOT NULL,
  `Top_Part_Num` varchar(25) NOT NULL,
  PRIMARY KEY (`Sub_Part_Num`,`Top_Part_Num`),
  KEY `FKcaffatxg1nk3spspghde2ryq9` (`Top_Part_Num`),
  CONSTRAINT `FKcaffatxg1nk3spspghde2ryq9` FOREIGN KEY (`Top_Part_Num`) REFERENCES `part` (`Part_Num`),
  CONSTRAINT `FKh8at8pqno9dwrjfo6k855c6ey` FOREIGN KEY (`Sub_Part_Num`) REFERENCES `part` (`Part_Num`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `User_Id` varchar(35) NOT NULL,
  `Creation_Date` datetime DEFAULT NULL,
  `Creator_id` varchar(255) DEFAULT NULL,
  `Modification_Date` datetime DEFAULT NULL,
  `Modifier_Id` varchar(255) DEFAULT NULL,
  `Active_Flag` bit(1) DEFAULT NULL,
  `Email` varchar(70) DEFAULT NULL,
  `First_Name` varchar(50) NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `Last_Name` varchar(50) NOT NULL,
  `Login_Name` varchar(50) DEFAULT NULL,
  `Login_Password` varchar(255) DEFAULT NULL,
  `Phone_Num` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`User_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_Role`
--

DROP TABLE IF EXISTS `user_Role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_Role` (
  `Role_Id` varchar(50) NOT NULL,
  `User_Id` varchar(35) NOT NULL,
  PRIMARY KEY (`Role_Id`,`User_Id`),
  KEY `FKidcm2pf5xuyxmh1yvffyh8kpc` (`User_Id`),
  CONSTRAINT `FKidcm2pf5xuyxmh1yvffyh8kpc` FOREIGN KEY (`User_Id`) REFERENCES `user` (`User_Id`),
  CONSTRAINT `FKj9ij8esd1stg8ythde4cms1u7` FOREIGN KEY (`Role_Id`) REFERENCES `role` (`Role_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-11-10 19:37:51
