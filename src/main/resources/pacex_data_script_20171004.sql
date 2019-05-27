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
-- Dumping data for table `WFS_Action`
--

LOCK TABLES `WFS_Action` WRITE;
/*!40000 ALTER TABLE `WFS_Action` DISABLE KEYS */;
/*!40000 ALTER TABLE `WFS_Action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `WFS_ProductionStatus`
--

LOCK TABLES `WFS_ProductionStatus` WRITE;
/*!40000 ALTER TABLE `WFS_ProductionStatus` DISABLE KEYS */;
INSERT INTO `WFS_ProductionStatus` VALUES ('1',NULL,NULL,NULL,NULL,'On Production','ONPROD'),('2',NULL,NULL,NULL,NULL,'Obsolete','OBSOLETE'),('3',NULL,NULL,NULL,NULL,'Temporary','TEMPORARY'),('4',NULL,NULL,NULL,NULL,'Initial','INITIAL');
/*!40000 ALTER TABLE `WFS_ProductionStatus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `WFS_Sequence`
--

LOCK TABLES `WFS_Sequence` WRITE;
/*!40000 ALTER TABLE `WFS_Sequence` DISABLE KEYS */;
/*!40000 ALTER TABLE `WFS_Sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `WFS_Workflow`
--

LOCK TABLES `WFS_Workflow` WRITE;
/*!40000 ALTER TABLE `WFS_Workflow` DISABLE KEYS */;
/*!40000 ALTER TABLE `WFS_Workflow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `binding_Type`
--

LOCK TABLES `binding_Type` WRITE;
/*!40000 ALTER TABLE `binding_Type` DISABLE KEYS */;
INSERT INTO `binding_Type` VALUES ('CARDSS','2016-06-13 07:41:41','',NULL,NULL,'Cards/Single Sheets','Cards/Single Sheets'),('DEFAULT','2016-07-29 00:00:00','walidb','2016-07-29 15:51:03','','Default value used for default stations assignment','Default'),('LOOSELEAF','2016-05-25 10:26:06','',NULL,NULL,'Loose Leaf','Loose Leaf'),('PERFECT','2016-06-03 17:23:32','','2017-06-06 18:41:27','admin','Perfect bind','Perfect Bind'),('PLASTIC','2016-06-08 00:00:00','walidb',NULL,NULL,'Plastic Coil','Plastic Coil'),('STAPLES','2016-06-08 00:00:00','walidb',NULL,NULL,'Staples','Staples'),('WIREO','2016-05-25 10:26:34','','2016-06-06 08:33:25','','Wire-O','Wire-O');
/*!40000 ALTER TABLE `binding_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `critiria`
--

LOCK TABLES `critiria` WRITE;
/*!40000 ALTER TABLE `critiria` DISABLE KEYS */;
INSERT INTO `critiria` VALUES ('3HOLEDRILL','2016-06-09 11:14:52','',NULL,NULL,'3 Hole Drill','3 Hole Drill',NULL),('DEFAULT','2016-06-09 11:13:10','',NULL,NULL,'This is the default part criteria that applies to any part','Default',NULL),('PERF','2016-06-09 11:15:13','',NULL,NULL,'Perf','Perf',NULL),('SELFCOVER','2016-06-09 11:14:15','',NULL,NULL,'Self Cover','Self Cover',NULL),('SHRINKWRAP','2016-06-09 11:15:42','',NULL,NULL,'ShrinkWrap','ShrinkWrap',NULL);
/*!40000 ALTER TABLE `critiria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `default_Station`
--

LOCK TABLES `default_Station` WRITE;
/*!40000 ALTER TABLE `default_Station` DISABLE KEYS */;
INSERT INTO `default_Station` VALUES ('CARDSS','COVER','SELFCOVER','CUTTER','2016-08-01 09:26:02','walidb','2017-06-13 13:26:08','admin',4),('DEFAULT','BOOK','3HOLEDRILL','DRILL','2016-07-29 15:56:54','walidb','2017-06-13 13:27:14','admin',7),('DEFAULT','BOOK','PERF','DRILL','2016-07-29 15:58:15','walidb','2017-06-13 13:27:19','admin',7),('DEFAULT','BOOK','SHRINKWRAP','SHRINKWRAP','2016-07-29 16:04:35','walidb','2017-06-13 13:27:49','admin',9),('DEFAULT','COVER','DEFAULT','COVERPRESS','2016-08-01 09:23:10','walidb','2017-01-03 16:59:52','system',2),('DEFAULT','COVER','DEFAULT','LAMINATION','2017-06-13 13:25:48','admin',NULL,NULL,3),('DEFAULT','TEXT','DEFAULT','PLOWFOLDER','2016-08-01 09:19:06','walidb',NULL,NULL,2),('DEFAULT','TEXT','DEFAULT','PRESS','2016-08-01 09:18:48','walidb',NULL,NULL,1),('DEFAULT','TEXT','SELFCOVER','CUTTER','2017-01-23 00:00:00','walidb',NULL,NULL,3),('PERFECT','BOOK','DEFAULT','BINDER','2016-07-29 15:55:39','walidb','2017-06-13 13:26:47','admin',5),('PERFECT','BOOK','DEFAULT','TRIMMER','2016-07-29 15:56:11','walidb','2017-06-13 13:26:54','admin',6),('PERFECT','COVER','DEFAULT','CUTTER','2016-08-01 09:24:42','walidb','2017-06-13 13:25:58','admin',4),('PLASTIC','BOOK','DEFAULT','PLASTICOIL','2016-07-29 15:58:59','walidb','2017-06-13 13:27:31','admin',8),('PLASTIC','COVER','DEFAULT','CUTTER','2016-08-01 09:24:08','walidb','2017-06-13 13:26:03','admin',4),('PLASTIC','TEXT','DEFAULT','CUTTER','2016-08-01 09:21:33','walidb',NULL,NULL,3),('STAPLES','TEXT','DEFAULT','CUTTER','2016-08-01 09:22:29','walidb','2016-08-01 09:32:54','walidb',3),('WIREO','BOOK','DEFAULT','PLASTICOIL','2016-07-29 16:03:21','walidb','2017-06-13 13:27:36','admin',8),('WIREO','COVER','DEFAULT','CUTTER','2016-08-01 09:24:23','walidb','2017-06-13 13:26:13','admin',4),('WIREO','TEXT','DEFAULT','CUTTER','2016-08-01 09:21:45','walidb','2016-08-01 09:32:50','walidb',3);
/*!40000 ALTER TABLE `default_Station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `job_Status`
--

LOCK TABLES `job_Status` WRITE;
/*!40000 ALTER TABLE `job_Status` DISABLE KEYS */;
INSERT INTO `job_Status` VALUES ('ASSIGNED','2016-09-01 00:00:00','walidb','2017-04-21 10:48:12','','Assigned To Machine','Assigned'),('CANCELLED','2016-07-04 00:00:00','walidb',NULL,NULL,'Cancelled','Cancelled'),('COMPLETE','2016-05-25 10:24:16','',NULL,NULL,'Complete','Complete'),('COMPLETE_PARTIAL','2017-03-13 00:00:00','walidb',NULL,NULL,'Partially Complete','Partially Complete'),('NEW','2016-05-25 13:54:42','',NULL,NULL,'New; once order is accepted, the job is created with status as NEW','New'),('PAUSED','2016-05-25 13:54:55','',NULL,NULL,'Paused','Paused'),('RUNNING','2016-05-02 00:00:00','walidb',NULL,NULL,'Running','Running'),('SCHEDULED','2016-05-25 13:55:15','',NULL,NULL,'Scheduled','Scheduled');
/*!40000 ALTER TABLE `job_Status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `job_Type`
--

LOCK TABLES `job_Type` WRITE;
/*!40000 ALTER TABLE `job_Type` DISABLE KEYS */;
INSERT INTO `job_Type` VALUES ('BINDING','2016-05-25 10:23:58','','2017-05-03 20:44:33','admin','Binding','Binding'),('PRINTING','2016-05-02 00:00:00','walidb',NULL,NULL,'Printing','Printing'),('PRINTING_2UP','2017-04-12 00:00:00','walidb',NULL,NULL,'2UP','2UP'),('PRINTING_3UP','2017-04-12 00:00:00','walidb',NULL,NULL,'3UP','3UP'),('PRINTING_FLYFOLDER','2017-04-12 00:00:00','walidb',NULL,NULL,'Printing_FlyFolder','Printing_FlyFolder'),('PRINTING_POPLINE','2017-04-12 00:00:00','walidb',NULL,NULL,'Printing/Pop Line','Printing/Pop Line'),('REPAIR','2016-05-25 13:56:00','',NULL,NULL,'Repair','Repair'),('SERVICE','2016-05-25 13:55:47','',NULL,NULL,'Service','Service');
/*!40000 ALTER TABLE `job_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lamination_Type`
--

LOCK TABLES `lamination_Type` WRITE;
/*!40000 ALTER TABLE `lamination_Type` DISABLE KEYS */;
INSERT INTO `lamination_Type` VALUES ('GLOSS','2016-05-02 00:00:00','walidb',NULL,NULL,'Gloss','Gloss'),('MATT','2016-12-21 00:00:00','walidb',NULL,NULL,'Matt','Matt'),('SILK','2016-05-19 10:42:28','',NULL,NULL,'Silk','Silk');
/*!40000 ALTER TABLE `lamination_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `log_Cause`
--

LOCK TABLES `log_Cause` WRITE;
/*!40000 ALTER TABLE `log_Cause` DISABLE KEYS */;
INSERT INTO `log_Cause` VALUES ('BREAK','2016-05-25 15:10:58','',NULL,NULL,'Break Time','Break Time'),('ENDSHIFT','2016-05-25 15:10:19','',NULL,NULL,'End Of Shift','End Of Shift'),('ISSUE','2016-05-25 15:10:36','',NULL,NULL,'Issue','Issue'),('JOBSCOMPLETE','2016-11-16 00:00:00','walidb',NULL,NULL,'Jobs Complete','Jobs Complete'),('ONOFF','2016-11-14 00:00:00','walidb',NULL,NULL,'Turn the machine on or off for some reason','OnOff'),('SERVICE','2016-05-02 00:00:00','walidb',NULL,NULL,'Service','Service'),('USERDECISION','2016-11-25 00:00:00','walidb',NULL,NULL,'User Decision','User Decision'),('WASTE','2017-03-30 00:00:00','walidb',NULL,NULL,'Waste generated','Waste');
/*!40000 ALTER TABLE `log_Cause` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `log_Result`
--

LOCK TABLES `log_Result` WRITE;
/*!40000 ALTER TABLE `log_Result` DISABLE KEYS */;
INSERT INTO `log_Result` VALUES ('LEFTOVERROLL','2016-05-25 15:09:10','',NULL,NULL,'Left Over Roll','Left Over Roll'),('OFF','2016-11-14 00:00:00','walidb',NULL,NULL,'Machine turned Off','Off'),('ON','2016-11-14 00:00:00','walidb',NULL,NULL,'Machine turned On','On'),('ONHOLD','2016-08-30 00:00:00','walidb',NULL,NULL,'On Hold','On Hold'),('REPAIR','2016-05-25 15:09:28','',NULL,NULL,'Repair','Repair'),('ROLL_PRODUCED','2016-05-02 00:00:00','walidb',NULL,NULL,'Roll Produced','Roll Produced'),('RUNNING','2016-11-16 00:00:00','walidb',NULL,NULL,'Running','Running'),('SERVICE','2016-05-25 15:09:40','',NULL,NULL,'Service','Service'),('TASKENDED','2016-01-03 00:00:00','walidb',NULL,NULL,'Task Ended','Task Ended');
/*!40000 ALTER TABLE `log_Result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machine`
--

LOCK TABLES `machine` WRITE;
/*!40000 ALTER TABLE `machine` DISABLE KEYS */;
INSERT INTO `machine` VALUES ('BINDER1','2017-05-18 19:54:06','admin','2017-10-02 11:55:15','admin','Binder Station','0','Binder 1',0,NULL,NULL,'1200','BINDER',NULL,NULL,'RUNNING'),('COVERPRESS1','2017-05-18 19:53:16','admin','2017-10-03 13:50:58','admin','Cover Press Machine','','Cover Press 1',0,NULL,NULL,'5400','COVERPRESS',39,NULL,'OUTSERVICE'),('CUTTER1','2017-05-18 19:56:52','admin','2017-06-14 10:57:57','admin','Cutter Machine','','Cutter 1',0,NULL,NULL,'2000','CUTTER',NULL,NULL,'ON'),('FLYFOLDER1','2017-07-24 16:55:31','admin','2017-07-24 16:55:55','admin','Fly Folder Machine','','Fly Folder 1',0,NULL,NULL,'55000','PLOWFOLDER',NULL,'FLYFOLDER','OFF'),('LAMINATOR1','2017-06-13 13:16:55','admin','2017-06-14 10:57:10','admin','Laminator 1','','Laminator 1',0,NULL,NULL,'2000','LAMINATION',NULL,NULL,'ON'),('PLOWFOLDER1','2017-05-18 19:51:55','admin','2017-07-28 12:34:35','admin','Plow Folder Machine','','Plow Folder 1',0,NULL,NULL,'55000','PLOWFOLDER',NULL,'PLOWFOLDER','SERVICE'),('POPLINE1','2017-05-18 19:52:34','admin','2017-06-15 19:24:15','admin','Pop Line Machine','','Pop Line 1',0,NULL,NULL,'','PLOWFOLDER',NULL,'POPLINE','ON'),('PRESS1','2017-05-18 19:50:12','admin','2017-10-03 11:16:40','admin','Fuji 1','127.0.0.1','Press 1',8080,NULL,NULL,'20000','PRESS',12,'4C','RUNNING'),('PRESS2','2017-05-18 19:50:35','admin','2017-09-26 14:48:17','admin','Fuji 2','','Press 2',0,NULL,NULL,'20000','PRESS',NULL,'4C','ON'),('SHIPPING1','2017-08-02 00:00:00','','2017-08-14 00:00:00',NULL,'SHIPPING1','0','SHIPPING1',0,NULL,NULL,NULL,'SHIPPING',NULL,NULL,'ON'),('SHRINKWRAP','2017-05-18 19:57:32','admin','2017-06-14 10:59:33','admin','Shrink Wrap Machine','','Shrinkwrap 1',0,NULL,NULL,'2000','SHRINKWRAP',NULL,NULL,'ON');
/*!40000 ALTER TABLE `machine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machine_Status`
--

LOCK TABLES `machine_Status` WRITE;
/*!40000 ALTER TABLE `machine_Status` DISABLE KEYS */;
INSERT INTO `machine_Status` VALUES ('OFF','2016-05-25 10:23:15','','2016-07-22 14:08:31','','Off','Off'),('ON','2016-05-02 00:00:00','walidb',NULL,NULL,'On','On'),('OUTSERVICE','2016-05-25 00:00:00','walidb',NULL,NULL,'Out Of Service','Out Of Service'),('RUNNING','2016-05-25 13:56:31','',NULL,NULL,'Running','Running'),('SERVICE','2016-05-25 13:56:44','',NULL,NULL,'On Maintenance Window','Maintenance');
/*!40000 ALTER TABLE `machine_Status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machine_Type`
--

LOCK TABLES `machine_Type` WRITE;
/*!40000 ALTER TABLE `machine_Type` DISABLE KEYS */;
INSERT INTO `machine_Type` VALUES ('1C','2017-04-10 00:00:00','walidb','2017-05-03 17:43:16','admin','1C Printer Machine type','1C','PRESS'),('4C','2017-04-10 00:00:00','walidb',NULL,NULL,'4C Printer machine','4C','PRESS'),('ALL','2017-04-10 00:00:00','walidb',NULL,NULL,'All MAchine Types','All',NULL),('FLYFOLDER','2017-04-10 00:00:00','walidb',NULL,NULL,'Fly Folder Machine Type','Fly Folder','PLOWFOLDER'),('PLOWFOLDER','2017-04-10 11:18:37','walidb',NULL,NULL,'Plow Folder Machine Type','Plow Folder','PLOWFOLDER'),('POPLINE','2017-04-10 00:00:00','walidb','2017-04-10 11:17:43','','Pop Line Machine Type','Pop Line','PLOWFOLDER');
/*!40000 ALTER TABLE `machine_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `order_Status`
--

LOCK TABLES `order_Status` WRITE;
/*!40000 ALTER TABLE `order_Status` DISABLE KEYS */;
INSERT INTO `order_Status` VALUES ('ACCEPTED','2016-05-02 00:00:00','walidb',NULL,NULL,'Accepted','Accepted'),('CANCELLED','2016-06-06 11:28:41','',NULL,NULL,'Cancelled','Cancelled'),('COMPLETE','2016-05-25 15:08:01','',NULL,NULL,'Complete','Complete'),('ERROR','2016-12-02 00:00:00','walidb',NULL,NULL,'Error','Error'),('ONPROD','2016-05-25 15:07:41','',NULL,NULL,'On Production','On Production'),('PENDING','2016-05-25 10:23:02','',NULL,NULL,'Pending','Pending'),('REJECTED','2016-05-25 10:22:44','',NULL,NULL,'Rejected','Rejected'),('TOEPAC','2016-05-25 15:08:21','',NULL,NULL,'To EPAC','To EPAC');
/*!40000 ALTER TABLE `order_Status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `paper_Type`
--

LOCK TABLES `paper_Type` WRITE;
/*!40000 ALTER TABLE `paper_Type` DISABLE KEYS */;
INSERT INTO `paper_Type` VALUES ('FSC_INCADA_EXEL_200','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_INCADA_EXEL_200','FSC_INCADA_EXEL_200'),('FSC_MC_AMBER_GRAPHIC_80','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_AMBER_GRAPHIC_80','FSC_MC_AMBER_GRAPHIC_80'),('FSC_MC_AMBER_GRAPHIC_90','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_AMBER_GRAPHIC_90','FSC_MC_AMBER_GRAPHIC_90'),('FSC_MC_AURA_2.0EXW_65','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_AURA_2.0EXW_65','FSC_MC_AURA_2.0EXW_65'),('FSC_MC_AURA_2.0EXW_80','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_AURA_2.0EXW_80','FSC_MC_AURA_2.0EXW_80'),('FSC_MC_CLAIRBOOK_2.0_65','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_CLAIRBOOK_2.0_65','FSC_MC_CLAIRBOOK_2.0_65'),('FSC_MC_CLAIRBOOK_2.0_80','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_CLAIRBOOK_2.0_80','FSC_MC_CLAIRBOOK_2.0_80'),('FSC_MC_CLASSIC_2.0_65','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_CLASSIC_2.0_65','FSC_MC_CLASSIC_2.0_65'),('FSC_MC_CLASSIC_2.0_80','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_CLASSIC_2.0_80','FSC_MC_CLASSIC_2.0_80'),('FSC_MC_DANUBE_2.0_EXW_65','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_DANUBE_2.0_EXW_65','FSC_MC_DANUBE_2.0_EXW_65'),('FSC_MC_DANUBE_2.0_EXW_80','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_DANUBE_2.0_EXW_80','FSC_MC_DANUBE_2.0_EXW_80'),('FSC_MC_HB75_1.6_52','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_HB75_1.6_52','FSC_MC_HB75_1.6_52'),('FSC_MC_HB80_2.0WHITE_65','2017-09-11 00:00:00','admin','2017-09-11 00:00:00','admin','FSC_MC_HB80_2.0WHITE_65','FSC_MC_HB80_2.0WHITE_65'),('FSC_MC_HB80_2.0WHITE_80','2017-05-18 20:17:39','admin',NULL,NULL,'FSC MC HB80 2.0WHITE 80','FSC_MC_HB80_2.0WHITE_80'),('FSC_MC_INCADA_240','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_INCADA_240','FSC_MC_INCADA_240'),('FSC_MC_INCADA_SILK_230','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_INCADA_SILK_230','FSC_MC_INCADA_SILK_230'),('FSC_MC_MEDIOPQ_EXB_2.0_65','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_MEDIOPQ_EXB_2.0_65','FSC_MC_MEDIOPQ_EXB_2.0_65'),('FSC_MC_MEDIOPQ_EXB_2.0_80','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_MEDIOPQ_EXB_2.0_80','FSC_MC_MEDIOPQ_EXB_2.0_80'),('FSC_MC_PLUSBOOK_76_52','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_PLUSBOOK_76_52','FSC_MC_PLUSBOOK_76_52'),('FSC_MC_ULTRASKY VOL_1.15_70','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_ULTRASKY VOL_1.15_70','FSC_MC_ULTRASKY VOL_1.15_70'),('FSC_MC_ULTRASKY_VOL_1.1_70','2017-09-11 00:00:00','admin',NULL,NULL,'FSC_MC_ULTRASKY_VOL_1.1_70','FSC_MC_ULTRASKY_VOL_1.1_70'),('X50_VIVIDJET','2017-05-18 20:15:03','admin','2017-06-06 18:42:19','admin','50b Vivid jet','50# Vivid Jet'),('X60_VIVIDJET','2017-05-18 20:17:39','admin','2017-06-06 18:42:32','admin','60b Vivid jet','60# Vivid Jet');
/*!40000 ALTER TABLE `paper_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `paper_Type_Media`
--

LOCK TABLES `paper_Type_Media` WRITE;
/*!40000 ALTER TABLE `paper_Type_Media` DISABLE KEYS */;
INSERT INTO `paper_Type_Media` VALUES ('AMBER_80','2017-09-24 22:31:40','admin',NULL,NULL,'','AMBER 80 / 495mm','FSC_MC_AMBER_GRAPHIC_80',7000,495),('ULTRASKY_70','2017-09-24 22:34:57','admin',NULL,NULL,'','ULTRASKY 70 / 495 mm','FSC_MC_ULTRASKY_VOL_1.1_70',7000,495);
/*!40000 ALTER TABLE `paper_Type_Media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `part_Category`
--

LOCK TABLES `part_Category` WRITE;
/*!40000 ALTER TABLE `part_Category` DISABLE KEYS */;
INSERT INTO `part_Category` VALUES ('BOOK','2016-05-02 00:00:00','walidb','2017-05-03 17:43:07','admin','Book','Book'),('COVER','2016-05-25 10:21:49','','2016-12-22 17:38:33','','Cover','Cover'),('TEXT','2016-05-25 10:21:36','','2016-12-22 16:26:56','','Text','Text');
/*!40000 ALTER TABLE `part_Category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `preference`
--

LOCK TABLES `preference` WRITE;
/*!40000 ALTER TABLE `preference` DISABLE KEYS */;
INSERT INTO `preference` VALUES ('BINDERSPEED','2017-03-27 00:00:00','walidb','2017-05-18 18:14:43','admin','Binder Speed (Books per Hour)...','1200',1),
('CCEMAILS','2016-11-15 00:00:00','system','2017-02-28 13:46:59','','cc Emails','walidb.epac@gmail.com',3),

('HEIGHTMARGINS','2016-11-15 00:00:00','system','2017-02-28 13:46:59','','HEIGHTMARGINS','30',3),
('TWOUPMARGINS','2016-11-15 00:00:00','system','2017-02-28 13:46:59','','TWOUPMARGINS','24',3),
('THREEUPMARGINS','2016-11-15 00:00:00','system','2017-02-28 13:46:59','','THREEUPMARGINS','35',3),
('FOURUPMARGINS','2016-11-15 00:00:00','system','2017-02-28 13:46:59','','FOURUPMARGINS','48',3),


('COPYCOVERRASTERFILES','2017-09-28 10:50:09','admin','2017-09-28 10:51:45','admin','Whether to copy raster files into cover press or not','false',11),
('COVERPRESSJOBACTIVATION','2017-07-26 10:50:51','admin','2017-09-28 17:31:16','admin','COVERPRESSJOBACTIVATION indicates whether the cover press job should be activated at first when the press job is active/scheduled, or after the press job is done','true',11),('COVERPRESSSPEED','2017-03-27 00:00:00','walidb','2017-05-18 18:17:40','admin','Cover Press Speed (Books/Sheet per Hour).','5400',1),('COVER_IMPOSITION_URL','2017-05-19 16:00:19','admin','2017-05-19 16:11:33','admin','Cover Imposition URL','http://localhost:8080/backend/imposer/cover/execute',6),('CUTTERSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Cutter Speed (Books per Hour).','2000',1),('DRILLSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Drill Speed (Books per Hour)','2000',1),('FACILITY','2017-07-27 12:02:00','admin',NULL,NULL,'France Facility/factoy','France',11),('FILEREPOSITORY','2016-06-15 09:28:49','','2017-09-25 10:59:33','admin','Location of the pdf files','/opt/pacex/repository/upload',6),('IMPOSITION_URL','2017-04-27 16:30:31','walidb@epac.com',NULL,NULL,'Imposition Tool Url','http://localhost:8080/backend/imposer/plowfolder/execute',6),('LAMINATIONSPEED','2017-06-13 13:18:35','admin',NULL,NULL,'Lamination Speed (Books per Hour).','2000',1),('ORDERREPOSITORY','2016-11-10 00:00:00','system','2017-04-27 17:02:55','walidb@epac.com','Repository of upload of XML files','sftp://tunis@sftp.epac.com/incoming/Islem_dont_delete/upload',6),('OVERS','2017-02-07 15:10:10','','2017-05-18 19:47:04','admin','Overs: the percentage that can be added to the produced quantity for the order to be satisfied (in %)','0',2),('OVERSADDITIF','2017-04-05 05:33:16','','2017-05-18 19:47:12','admin','A minimum value added (to the original order quantity) in addition to the overs','5',2),('PLASTICOILSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Plastic coil speed (Books per Hour)','2000',1),('PLOWFOLDERSPEED','2017-03-27 00:00:00','walidb','2017-05-18 19:45:05','admin','Plow Folder Speed (feet per hour)','55000',1),('POLINE_IMPOSITION_URL','2017-05-19 16:01:13','admin','2017-05-19 16:10:55','admin','Pop Line Imposition URL','http://localhost:8080/backend/imposer/popline/execute',6),('PRESSSPEED','2016-05-02 00:00:00','walidb','2017-06-14 07:35:01','admin','Printer speed (In feet/h or meter/h): \n20000 is 6000 meter/h (100 meter/min)\n25000 is 127 meter/min','20000',1),('RECEIVEREMAILS','2016-11-10 00:00:00','system',NULL,NULL,'Email of the receiver','islemt.epac@gmail.com',3),('ROLLINITLENGTH','2016-05-25 14:49:49','','2016-09-27 09:28:16','','Roll initial Length (feet)','54500',5),('ROLLMINLENGTH','2017-04-27 16:28:30','walidb@epac.com',NULL,NULL,'Minimum length of a roll; useful for when to allow creating left over roll...','20',5),('ROLLWIDTH','2016-05-19 11:29:55','','2017-06-14 07:10:38','admin','Roll width in inches (105 in cm).\nSpecial case: 19 inches for books 9*12','18.06',5),('SENDEREMAIL','2016-11-10 00:00:00','system',NULL,NULL,'Email of the sender','islemt@epac.tn',3),('SHIFT','2016-05-25 14:59:12','','2017-03-27 09:52:24','','Shifts of the day: Starting from 0h until 24h, indicate the working hours...\nUse - for the duration and separate shifts by ;\nTo indicate if the weekend is a working day, add :w otherwise add :nw','0-2;9-17;18-24:nw',4),('SHRINKWRAPSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Shrink wrap speed (Books per Hour)','2000',1),('TRIMMERSPEED','2017-03-27 00:00:00','walidb','2017-05-18 19:46:48','admin','Trimmer Speed (Books per Hour).','1200',1),('UNDERS','2017-02-07 15:10:43','','2017-10-03 14:56:11','admin','Unders: the percentage that can be subtracted from the produced quantity for the order to be satisfied (in %)','0',2),('UNDERSADDITIF','2017-04-05 05:35:11','','2017-05-18 19:47:28','admin','A minimum value subtracted (from the original order quantity) in addition to the unders','0',2),('UNITSYSTEM','2017-06-14 07:03:35','admin','2017-09-24 23:04:43','admin','Measurement Unit System: American or French System.\n\'US\' for American System: Uses Inch, Foot, ft/h...\n\'FR\' for French System: uses mm, m, m/mn...','FR',10);
/*!40000 ALTER TABLE `preference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `priority`
--

LOCK TABLES `priority` WRITE;
/*!40000 ALTER TABLE `priority` DISABLE KEYS */;
INSERT INTO `priority` VALUES ('HIGH','2016-05-02 00:00:00','walidb',NULL,NULL,'High','High'),('NORMAL','2016-05-25 10:20:00','',NULL,NULL,'Normal','Normal');
/*!40000 ALTER TABLE `priority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES ('ROLE_ADMIN','2017-04-29 00:00:00','walidb',NULL,NULL,'Administrator','Administrator'),('ROLE_LOP','2016-05-24 18:09:57','walidb',NULL,NULL,'Lead Operator','Lead Operator'),('ROLE_OP','2016-05-24 18:10:13','walidb',NULL,NULL,'Operator','Operator'),('ROLE_PM','2016-05-24 18:09:16','walidb',NULL,NULL,'Project Manager','Project Manager');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `roll_Type`
--

LOCK TABLES `roll_Type` WRITE;
/*!40000 ALTER TABLE `roll_Type` DISABLE KEYS */;
INSERT INTO `roll_Type` VALUES ('LEFTOVER','2016-05-25 10:20:27','',NULL,NULL,'Leftover','Leftover'),('NEW','2016-05-02 00:00:00','walidb',NULL,NULL,'New','New'),('PRODUCED','2016-05-25 10:21:01','',NULL,NULL,'Produced','Produced');
/*!40000 ALTER TABLE `roll_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `roll_status`
--

LOCK TABLES `roll_status` WRITE;
/*!40000 ALTER TABLE `roll_status` DISABLE KEYS */;
INSERT INTO `roll_status` VALUES ('ASSIGNED','2016-08-19 00:00:00','walidb',NULL,NULL,'Assigned','Assigned'),('AVAILABLE','2016-05-02 00:00:00','walidb',NULL,NULL,'Available for printing','Available for printing'),('EXHAUSTED','2016-05-25 14:51:46','',NULL,NULL,'Exhausted','Retired'),('NEW','2016-05-25 14:50:54','',NULL,NULL,'New','New'),('ONPROD','2016-05-25 14:51:29','',NULL,NULL,'On Production','On Production'),('SCHEDULED','2016-05-25 14:51:09','',NULL,NULL,'Scheduled','Scheduled');
/*!40000 ALTER TABLE `roll_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
INSERT INTO `station` VALUES ('BINDER','2017-05-18 17:53:15','admin','2017-07-26 10:35:32','admin','','Finishing (Binder) Station','Sheet','Finishing','',NULL,6,NULL,'BINDER',NULL),('COVERPRESS','2017-05-18 17:52:01','admin',NULL,NULL,'','Cover Press Station','Sheet','Cover Press','',NULL,3,NULL,'COVERPRESS',NULL),('CUTTER','2017-05-18 17:52:38','admin','2017-06-13 13:14:41','admin','','Cutter Station','Sheet','Cutter','',NULL,5,NULL,'CUTTER',NULL),('LAMINATION','2017-06-13 13:04:42','admin','2017-07-27 11:40:00','admin','\0','Lamination Station','Sheet','Lamination','',NULL,4,NULL,'LAMINATION',NULL),('PLOWFOLDER','2017-05-18 17:49:38','admin','2017-07-24 16:58:50','admin','','Hunkeler (Finishing) Station','Roll','Hunkeler','',NULL,2,NULL,'PLOWFOLDER',NULL),('PRESS','2017-05-18 17:48:41','admin',NULL,NULL,'','The Press Station','Roll','Press','',NULL,1,NULL,'PRESS',NULL),('SHIPPING','2017-07-27 11:49:58','admin',NULL,NULL,'','Shipping; used to create a shipping job but the station itself won\'t be displayed/active','Job','Shipping','',NULL,8,NULL,'SHIPPING',NULL),('SHRINKWRAP','2017-05-18 17:54:26','admin','2017-07-27 11:50:32','admin','\0','ShrinkWrap Station','Sheet','ShrinkWrap','',NULL,7,NULL,'SHRINKWRAP',NULL);
/*!40000 ALTER TABLE `station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `station_Category`
--

LOCK TABLES `station_Category` WRITE;
/*!40000 ALTER TABLE `station_Category` DISABLE KEYS */;
INSERT INTO `station_Category` VALUES ('BINDER','2016-07-28 00:00:00','walidb',NULL,NULL,'Binder','Binder'),('COVERPRESS','2016-07-29 12:05:01','',NULL,NULL,'Cover Press','Cover Press'),('CUTTER','2016-07-29 11:59:31','',NULL,NULL,'Cutter','Cutter'),('DRILL','2016-07-29 12:03:17','',NULL,NULL,'Drill','Drill'),('LAMINATION','2017-06-13 13:05:10','admin',NULL,NULL,'Lamination','Lamination'),('PLASTICOIL','2016-07-29 12:02:17','',NULL,NULL,'Plastic Coil','Plastic Coil'),('PLOWFOLDER','2016-07-29 12:05:23','','2017-07-24 16:58:08','admin','Hunkeler (Finishing) station type','Hunkeler'),('PRESS','2016-06-06 11:26:46','walidb',NULL,NULL,'Press','Press'),('SHIPPING','2017-07-27 11:42:03','admin',NULL,NULL,'Shipping Station','Shipping'),('SHRINKWRAP','2016-07-28 00:00:00','walidb',NULL,NULL,'Shrinkwrap','Shrinkwrap'),('TRIMMER','2016-07-28 00:00:00','wlidb',NULL,NULL,'Trimmer','Trimmer');
/*!40000 ALTER TABLE `station_Category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('loplntestcom','2017-05-02 09:05:24','admin',NULL,NULL,'','lopln@test.com','lop','en','lopln','lop','$2a$11$bhIwEYYc26ycOCdwG.PBcuiCjD03/SGENHeg0wm57NRLBkW5nvF5a','1114567890'),('oplntestcom','2017-05-02 09:04:50','admin',NULL,NULL,'','opln@test.com','op','en','opln','op','$2a$11$ecAkawTWtwKPh16ARWjcd.N4srbjErl5z2C0GvAGtlTmPt2O5nOEG','1234567890'),('pmlnpmcom','2017-05-02 09:04:16','admin',NULL,NULL,'','pmln@pm.com','pm','en','ln','pm','$2a$11$XDW3.nzsQvLBt.G7PLWtvuGk6hv4fbKNZrw2fPrJbbVsR7n8ESrgq','6578909877'),('walidb','2016-05-02 00:00:00','walidb','2017-05-02 09:06:03','admin','','walidb@epac.com','admin','fr','adminln','admin','$2a$11$lmlF5gbPbI7E7EQk3iPGBeGBosMhws.KN7ecad0JEMVnXnQSaK4Ja','1231231233');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_Role`
--

LOCK TABLES `user_Role` WRITE;
/*!40000 ALTER TABLE `user_Role` DISABLE KEYS */;
INSERT INTO `user_Role` VALUES ('ROLE_LOP','loplntestcom'),('ROLE_OP','oplntestcom'),('ROLE_PM','pmlnpmcom'),('ROLE_ADMIN','walidb');
/*!40000 ALTER TABLE `user_Role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-10-04 10:09:29








-- Dump for Mexico; NOvember 11, 2017
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
-- Dumping data for table `WFS_Action`
--

LOCK TABLES `WFS_Action` WRITE;
/*!40000 ALTER TABLE `WFS_Action` DISABLE KEYS */;
INSERT INTO `WFS_Action` (`idaction`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `type`, `name`) VALUES (1,NULL,NULL,NULL,NULL,'download','download'),(2,NULL,NULL,NULL,NULL,'copy','copy'),(3,NULL,NULL,NULL,NULL,'imposition','impose'),(4,NULL,NULL,NULL,NULL,'ripping','rip'),(5,NULL,NULL,NULL,NULL,'moving','move'),(6,NULL,NULL,NULL,NULL,'Imposition Popline','impose_popline'),(7,NULL,NULL,NULL,NULL,'Cover Imposition','impose_cover');
/*!40000 ALTER TABLE `WFS_Action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `WFS_ProductionStatus`
--

LOCK TABLES `WFS_ProductionStatus` WRITE;
/*!40000 ALTER TABLE `WFS_ProductionStatus` DISABLE KEYS */;
INSERT INTO `WFS_ProductionStatus` (`productionStatusId`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `description`, `name`) VALUES ('1',NULL,NULL,NULL,NULL,'On Production','ONPROD'),('2',NULL,NULL,NULL,NULL,'Obsolete','OBSOLETE'),('3',NULL,NULL,NULL,NULL,'Temporary','TEMPORARY'),('4',NULL,NULL,NULL,NULL,'Initial','INITIAL');
/*!40000 ALTER TABLE `WFS_ProductionStatus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `WFS_Sequence`
--

LOCK TABLES `WFS_Sequence` WRITE;
/*!40000 ALTER TABLE `WFS_Sequence` DISABLE KEYS */;
INSERT INTO `WFS_Sequence` (`idsequence`, `workflowid`, `ranking`, `actionid`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`) VALUES (1,1,1,2,NULL,NULL,NULL,NULL),(2,1,2,3,NULL,NULL,NULL,NULL),(3,1,3,4,NULL,NULL,NULL,NULL),(5,2,1,2,NULL,NULL,NULL,NULL),(6,2,2,6,NULL,NULL,NULL,NULL),(7,2,3,4,NULL,NULL,NULL,NULL),(9,3,1,2,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `WFS_Sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `WFS_Workflow`
--

LOCK TABLES `WFS_Workflow` WRITE;
/*!40000 ALTER TABLE `WFS_Workflow` DISABLE KEYS */;
INSERT INTO `WFS_Workflow` (`idworkflow`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `enable`, `name`) VALUES (1,NULL,NULL,NULL,NULL,'','TextPlowFolderWorkflow'),(2,NULL,NULL,NULL,NULL,'','TextPoplineWorkflow'),(3,NULL,NULL,NULL,NULL,'','CoverWorkflow');
/*!40000 ALTER TABLE `WFS_Workflow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `binding_Type`
--

LOCK TABLES `binding_Type` WRITE;
/*!40000 ALTER TABLE `binding_Type` DISABLE KEYS */;
INSERT INTO `binding_Type` (`Binding_Type_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('CARDSS','2016-06-13 07:41:41','',NULL,NULL,'Cards/Single Sheets','Cards/Single Sheets'),('DEFAULT','2016-07-29 00:00:00','walidb','2016-07-29 15:51:03','','Default value used for default stations assignment','Default'),('LOOSELEAF','2016-05-25 10:26:06','',NULL,NULL,'Loose Leaf','Loose Leaf'),('PERFECT','2016-06-03 17:23:32','','2017-06-06 18:41:27','admin','Perfect bind','Perfect Bind'),('PLASTIC','2016-06-08 00:00:00','walidb',NULL,NULL,'Plastic Coil','Plastic Coil'),('STAPLES','2016-06-08 00:00:00','walidb',NULL,NULL,'Staples','Staples'),('WIREO','2016-05-25 10:26:34','','2016-06-06 08:33:25','','Wire-O','Wire-O');
/*!40000 ALTER TABLE `binding_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `critiria`
--

LOCK TABLES `critiria` WRITE;
/*!40000 ALTER TABLE `critiria` DISABLE KEYS */;
INSERT INTO `critiria` (`Critiria_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`, `Active_Flag`) VALUES ('3HOLEDRILL','2016-06-09 11:14:52','',NULL,NULL,'3 Hole Drill','3 Hole Drill',NULL),('DEFAULT','2016-06-09 11:13:10','',NULL,NULL,'This is the default part criteria that applies to any part','Default',NULL),('PERF','2016-06-09 11:15:13','',NULL,NULL,'Perf','Perf',NULL),('SELFCOVER','2016-06-09 11:14:15','',NULL,NULL,'Self Cover','Self Cover',NULL),('SHRINKWRAP','2016-06-09 11:15:42','',NULL,NULL,'ShrinkWrap','ShrinkWrap',NULL);
/*!40000 ALTER TABLE `critiria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `default_Station`
--

LOCK TABLES `default_Station` WRITE;
/*!40000 ALTER TABLE `default_Station` DISABLE KEYS */;
INSERT INTO `default_Station` (`Binding_Type_Id`, `Category_Id`, `Critiria_Id`, `Station_Category_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Production_Ordering`) VALUES ('CARDSS','COVER','SELFCOVER','CUTTER','2016-08-01 09:26:02','walidb','2017-06-13 13:26:08','admin',4),('DEFAULT','BOOK','3HOLEDRILL','DRILL','2016-07-29 15:56:54','walidb','2017-06-13 13:27:14','admin',7),('DEFAULT','BOOK','PERF','DRILL','2016-07-29 15:58:15','walidb','2017-06-13 13:27:19','admin',7),('DEFAULT','BOOK','SHRINKWRAP','SHRINKWRAP','2016-07-29 16:04:35','walidb','2017-06-13 13:27:49','admin',9),('DEFAULT','COVER','DEFAULT','COVERPRESS','2016-08-01 09:23:10','walidb','2017-01-03 16:59:52','system',2),('DEFAULT','COVER','DEFAULT','LAMINATION','2017-06-13 13:25:48','admin',NULL,NULL,3),('DEFAULT','TEXT','DEFAULT','PLOWFOLDER','2016-08-01 09:19:06','walidb',NULL,NULL,2),('DEFAULT','TEXT','DEFAULT','PRESS','2016-08-01 09:18:48','walidb',NULL,NULL,1),('DEFAULT','TEXT','SELFCOVER','CUTTER','2017-01-23 00:00:00','walidb',NULL,NULL,3),('PERFECT','BOOK','DEFAULT','BINDER','2016-07-29 15:55:39','walidb','2017-06-13 13:26:47','admin',5),('PERFECT','BOOK','DEFAULT','TRIMMER','2016-07-29 15:56:11','walidb','2017-06-13 13:26:54','admin',6),('PERFECT','COVER','DEFAULT','CUTTER','2016-08-01 09:24:42','walidb','2017-06-13 13:25:58','admin',4),('PLASTIC','BOOK','DEFAULT','PLASTICOIL','2016-07-29 15:58:59','walidb','2017-06-13 13:27:31','admin',8),('PLASTIC','COVER','DEFAULT','CUTTER','2016-08-01 09:24:08','walidb','2017-06-13 13:26:03','admin',4),('PLASTIC','TEXT','DEFAULT','CUTTER','2016-08-01 09:21:33','walidb',NULL,NULL,3),('STAPLES','TEXT','DEFAULT','CUTTER','2016-08-01 09:22:29','walidb','2016-08-01 09:32:54','walidb',3),('WIREO','BOOK','DEFAULT','PLASTICOIL','2016-07-29 16:03:21','walidb','2017-06-13 13:27:36','admin',8),('WIREO','COVER','DEFAULT','CUTTER','2016-08-01 09:24:23','walidb','2017-06-13 13:26:13','admin',4),('WIREO','TEXT','DEFAULT','CUTTER','2016-08-01 09:21:45','walidb','2016-08-01 09:32:50','walidb',3);
/*!40000 ALTER TABLE `default_Station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `job_Status`
--

LOCK TABLES `job_Status` WRITE;
/*!40000 ALTER TABLE `job_Status` DISABLE KEYS */;
INSERT INTO `job_Status` (`Job_Status_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('ASSIGNED','2016-09-01 00:00:00','walidb','2017-04-21 10:48:12','','Assigned To Machine','Assigned'),('CANCELLED','2016-07-04 00:00:00','walidb',NULL,NULL,'Cancelled','Cancelled'),('COMPLETE','2016-05-25 10:24:16','',NULL,NULL,'Complete','Complete'),('COMPLETE_PARTIAL','2017-03-13 00:00:00','walidb',NULL,NULL,'Partially Complete','Partially Complete'),('NEW','2016-05-25 13:54:42','',NULL,NULL,'New; once order is accepted, the job is created with status as NEW','New'),('PAUSED','2016-05-25 13:54:55','',NULL,NULL,'Paused','Paused'),('RUNNING','2016-05-02 00:00:00','walidb',NULL,NULL,'Running','Running'),('SCHEDULED','2016-05-25 13:55:15','',NULL,NULL,'Scheduled','Scheduled');
/*!40000 ALTER TABLE `job_Status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `job_Type`
--

LOCK TABLES `job_Type` WRITE;
/*!40000 ALTER TABLE `job_Type` DISABLE KEYS */;
INSERT INTO `job_Type` (`Job_Type_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('BINDING','2016-05-25 10:23:58','','2017-05-03 20:44:33','admin','Binding','Binding'),('PRINTING','2016-05-02 00:00:00','walidb',NULL,NULL,'Printing','Printing'),('PRINTING_2UP','2017-04-12 00:00:00','walidb',NULL,NULL,'2UP','2UP'),('PRINTING_3UP','2017-04-12 00:00:00','walidb',NULL,NULL,'3UP','3UP'),('PRINTING_FLYFOLDER','2017-04-12 00:00:00','walidb',NULL,NULL,'Printing_FlyFolder','Printing_FlyFolder'),('PRINTING_PLOWFOLDER','2017-04-12 00:00:00','walidb',NULL,NULL,'Printing/PlowFolder','Printing/PlowFolder'),('PRINTING_POPLINE','2017-04-12 00:00:00','walidb',NULL,NULL,'Printing/Pop Line','Printing/Pop Line'),('REPAIR','2016-05-25 13:56:00','',NULL,NULL,'Repair','Repair'),('SERVICE','2016-05-25 13:55:47','',NULL,NULL,'Service','Service');
/*!40000 ALTER TABLE `job_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `lamination_Type`
--

LOCK TABLES `lamination_Type` WRITE;
/*!40000 ALTER TABLE `lamination_Type` DISABLE KEYS */;
INSERT INTO `lamination_Type` (`Lamination_Type_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('GLOSS','2016-05-02 00:00:00','walidb',NULL,NULL,'Gloss','Gloss'),('MATT','2016-12-21 00:00:00','walidb',NULL,NULL,'Matt','Matt'),('SILK','2016-05-19 10:42:28','',NULL,NULL,'Silk','Silk');
/*!40000 ALTER TABLE `lamination_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `log_Cause`
--

LOCK TABLES `log_Cause` WRITE;
/*!40000 ALTER TABLE `log_Cause` DISABLE KEYS */;
INSERT INTO `log_Cause` (`Log_Cause_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('BREAK','2016-05-25 15:10:58','',NULL,NULL,'Break Time','Break Time'),('ENDSHIFT','2016-05-25 15:10:19','',NULL,NULL,'End Of Shift','End Of Shift'),('ISSUE','2016-05-25 15:10:36','',NULL,NULL,'Issue','Issue'),('JOBSCOMPLETE','2016-11-16 00:00:00','walidb',NULL,NULL,'Jobs Complete','Jobs Complete'),('ONOFF','2016-11-14 00:00:00','walidb',NULL,NULL,'Turn the machine on or off for some reason','OnOff'),('SERVICE','2016-05-02 00:00:00','walidb',NULL,NULL,'Service','Service'),('USERDECISION','2016-11-25 00:00:00','walidb',NULL,NULL,'User Decision','User Decision'),('WASTE','2017-03-30 00:00:00','walidb',NULL,NULL,'Waste generated','Waste');
/*!40000 ALTER TABLE `log_Cause` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `log_Result`
--

LOCK TABLES `log_Result` WRITE;
/*!40000 ALTER TABLE `log_Result` DISABLE KEYS */;
INSERT INTO `log_Result` (`Log_Result_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('LEFTOVERROLL','2016-05-25 15:09:10','',NULL,NULL,'Left Over Roll','Left Over Roll'),('OFF','2016-11-14 00:00:00','walidb',NULL,NULL,'Machine turned Off','Off'),('ON','2016-11-14 00:00:00','walidb',NULL,NULL,'Machine turned On','On'),('ONHOLD','2016-08-30 00:00:00','walidb',NULL,NULL,'On Hold','On Hold'),('REPAIR','2016-05-25 15:09:28','',NULL,NULL,'Repair','Repair'),('ROLL_PRODUCED','2016-05-02 00:00:00','walidb',NULL,NULL,'Roll Produced','Roll Produced'),('RUNNING','2016-11-16 00:00:00','walidb',NULL,NULL,'Running','Running'),('SERVICE','2016-05-25 15:09:40','',NULL,NULL,'Service','Service'),('TASKENDED','2016-01-03 00:00:00','walidb',NULL,NULL,'Task Ended','Task Ended');
/*!40000 ALTER TABLE `log_Result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machine`
--

LOCK TABLES `machine` WRITE;
/*!40000 ALTER TABLE `machine` DISABLE KEYS */;
INSERT INTO `machine` (`Machine_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `IpAddress`, `Name`, `Port`, `OcInputPath`, `Service_Schedule`, `Speed`, `Station_Id`, `Current_Job_Id`, `MachineType`, `Status`) VALUES ('BINDER1','2017-05-18 19:54:06','admin','2017-10-02 11:55:15','admin','Binder Station','0','Binder 1',0,NULL,NULL,'1200','BINDER',NULL,NULL,'RUNNING'),('COVERPRESS1','2017-05-18 19:53:16','admin','2017-11-09 17:46:30','admin','Cover Press Machine','','Cover Press 1',0,NULL,NULL,'5400','COVERPRESS',NULL,NULL,'ON'),('CUTTER1','2017-05-18 19:56:52','admin','2017-06-14 10:57:57','admin','Cutter Machine','','Cutter 1',0,NULL,NULL,'2000','CUTTER',NULL,NULL,'ON'),('LAMINATOR1','2017-06-13 13:16:55','admin','2017-06-14 10:57:10','admin','Laminator 1','','Laminator 1',0,NULL,NULL,'2000','LAMINATION',NULL,NULL,'ON'),('PLOWFOLDER1','2017-05-18 19:51:55','admin','2017-11-07 18:25:20','admin','Plow Folder Machine','','Plow Folder 1',0,NULL,NULL,'55000','PLOWFOLDER',187,'PLOWFOLDER','RUNNING'),('POPLINE1','2017-05-18 19:52:34','admin','2017-06-15 19:24:15','admin','Pop Line Machine','','Pop Line 1',0,NULL,NULL,'','PLOWFOLDER',NULL,'POPLINE','ON'),('PRESS1','2017-05-18 19:50:12','admin','2017-11-07 17:24:29','admin','Fuji 1','192.168.75.195','Press 1',8080,'/opt/pacex/repository/press/EM1',NULL,'20000','PRESS',NULL,'4C','ON'),('PRESS2','2017-05-18 19:50:35','admin','2017-10-11 12:20:25','admin','Fuji 2','','Press 2',0,NULL,NULL,'20000','PRESS',NULL,'4C','ON'),('SHIPPING1','2017-08-02 00:00:00','','2017-08-14 00:00:00',NULL,'SHIPPING1','0','SHIPPING1',0,NULL,NULL,NULL,'SHIPPING',NULL,NULL,'ON'),('SHRINKWRAP','2017-05-18 19:57:32','admin','2017-06-14 10:59:33','admin','Shrink Wrap Machine','','Shrinkwrap 1',0,NULL,NULL,'2000','SHRINKWRAP',NULL,NULL,'ON');
/*!40000 ALTER TABLE `machine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machine_Status`
--

LOCK TABLES `machine_Status` WRITE;
/*!40000 ALTER TABLE `machine_Status` DISABLE KEYS */;
INSERT INTO `machine_Status` (`Machine_Status_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('OFF','2016-05-25 10:23:15','','2016-07-22 14:08:31','','Off','Off'),('ON','2016-05-02 00:00:00','walidb',NULL,NULL,'On','On'),('OUTSERVICE','2016-05-25 00:00:00','walidb',NULL,NULL,'Out Of Service','Out Of Service'),('RUNNING','2016-05-25 13:56:31','',NULL,NULL,'Running','Running'),('SERVICE','2016-05-25 13:56:44','',NULL,NULL,'On Maintenance Window','Maintenance');
/*!40000 ALTER TABLE `machine_Status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machine_Type`
--

LOCK TABLES `machine_Type` WRITE;
/*!40000 ALTER TABLE `machine_Type` DISABLE KEYS */;
INSERT INTO `machine_Type` (`Machine_Type_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`, `Station_Category_Id`) VALUES ('1C','2017-04-10 00:00:00','walidb','2017-05-03 17:43:16','admin','1C Printer Machine type','1C','PRESS'),('4C','2017-04-10 00:00:00','walidb',NULL,NULL,'4C Printer machine','4C','PRESS'),('ALL','2017-04-10 00:00:00','walidb',NULL,NULL,'All MAchine Types','All',NULL),('FLYFOLDER','2017-04-10 00:00:00','walidb',NULL,NULL,'Fly Folder Machine Type','Fly Folder','PLOWFOLDER'),('PLOWFOLDER','2017-04-10 11:18:37','walidb',NULL,NULL,'Plow Folder Machine Type','Plow Folder','PLOWFOLDER'),('POPLINE','2017-04-10 00:00:00','walidb','2017-04-10 11:17:43','','Pop Line Machine Type','Pop Line','PLOWFOLDER');
/*!40000 ALTER TABLE `machine_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `order_Status`
--

LOCK TABLES `order_Status` WRITE;
/*!40000 ALTER TABLE `order_Status` DISABLE KEYS */;
INSERT INTO `order_Status` (`Order_Status_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('ACCEPTED','2016-05-02 00:00:00','walidb',NULL,NULL,'Accepted','Accepted'),('CANCELLED','2016-06-06 11:28:41','',NULL,NULL,'Cancelled','Cancelled'),('COMPLETE','2016-05-25 15:08:01','',NULL,NULL,'Complete','Complete'),('ERROR','2016-12-02 00:00:00','walidb',NULL,NULL,'Error','Error'),('ONPROD','2016-05-25 15:07:41','',NULL,NULL,'On Production','On Production'),('PENDING','2016-05-25 10:23:02','',NULL,NULL,'Pending','Pending'),('REJECTED','2016-05-25 10:22:44','',NULL,NULL,'Rejected','Rejected'),('TOEPAC','2016-05-25 15:08:21','',NULL,NULL,'To EPAC','To EPAC');
/*!40000 ALTER TABLE `order_Status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `paper_Type`
--

LOCK TABLES `paper_Type` WRITE;
/*!40000 ALTER TABLE `paper_Type` DISABLE KEYS */;
INSERT INTO `paper_Type` (`Paper_Type_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`, `Thickness`, `Weight`, `dropFolder`) VALUES ('X50_VIVIDJET','2017-05-18 20:15:03','admin','2017-06-06 18:42:19','admin','50b Vivid jet','50# Vivid Jet',0.0038,NULL,'VividjetDrop'),('X60_VIVIDJET','2017-05-18 20:17:39','admin','2017-06-06 18:42:32','admin','60b Vivid jet','60# Vivid Jet',0.0046,NULL,'VividjetDrop');
/*!40000 ALTER TABLE `paper_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `paper_Type_Media`
--

LOCK TABLES `paper_Type_Media` WRITE;
/*!40000 ALTER TABLE `paper_Type_Media` DISABLE KEYS */;
INSERT INTO `paper_Type_Media` (`Paper_Type_Media_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`, `Paper_Type_Id`, `Roll_Length`, `Roll_Width`) VALUES ('X50_18_VIVIDJET','2017-05-24 00:00:00','system',NULL,NULL,'18\" 50b Vivid Jet','18\" 50b Vivid Jet','X50_VIVIDJET',40000,18.06),('X50_19_VIVIDJET','2017-05-24 00:00:00','system',NULL,NULL,'19\" 50b Vivid Jet','19\" 50b Vivid Jet','X50_VIVIDJET',40000,19),('X60_18_VIVIDJET','2017-05-24 00:00:00','system',NULL,NULL,'18\" 60b Vivid Jet','18\" 60b Vivid Jet','X60_VIVIDJET',36000,18.06),('X60_19_VIVIDJET','2017-05-29 09:49:30','admin',NULL,NULL,'19\" 60b Vivid Jet','19\" 60b Vivid Jet','X60_VIVIDJET',36000,19);
/*!40000 ALTER TABLE `paper_Type_Media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `part_Category`
--

LOCK TABLES `part_Category` WRITE;
/*!40000 ALTER TABLE `part_Category` DISABLE KEYS */;
INSERT INTO `part_Category` (`Category_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('BOOK','2016-05-02 00:00:00','walidb','2017-05-03 17:43:07','admin','Book','Book'),('COVER','2016-05-25 10:21:49','','2016-12-22 17:38:33','','Cover','Cover'),('TEXT','2016-05-25 10:21:36','','2016-12-22 16:26:56','','Text','Text');
/*!40000 ALTER TABLE `part_Category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `preference`
--

LOCK TABLES `preference` WRITE;
/*!40000 ALTER TABLE `preference` DISABLE KEYS */;
INSERT INTO `preference` (`Preference_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Value`, `Group_Id`) VALUES ('ACTIVATEBINDERFORSTANLY','2017-10-13 11:50:41','admin',NULL,NULL,'Activates the binder (finishing) job when hunkeler job starts and cover job is complete','true',11),('BINDERSPEED','2017-03-27 00:00:00','walidb','2017-05-18 18:14:43','admin','Binder Speed (Books per Hour)...','1200',1),('CCEMAILS','2016-11-15 00:00:00','system','2017-02-28 13:46:59','','cc Emails','walidb.epac@gmail.com',3),('COPYCOVERRASTERFILES','2017-09-28 10:50:09','admin','2017-09-28 10:51:45','admin','Whether to copy raster files into cover press or not','true',11),('COVERPRESSJOBACTIVATION','2017-07-26 10:50:51','admin','2017-09-28 17:31:16','admin','COVERPRESSJOBACTIVATION indicates whether the cover press job should be activated at first when the press job is active/scheduled, or after the press job is done','true',11),('COVERPRESSSPEED','2017-03-27 00:00:00','walidb','2017-05-18 18:17:40','admin','Cover Press Speed (Books/Sheet per Hour).','5400',1),('COVER_IMPOSITION_URL','2017-05-19 16:00:19','admin','2017-05-19 16:11:33','admin','Cover Imposition URL','http://localhost:8080/backend/imposer/cover/execute',6),('CUTTERSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Cutter Speed (Books per Hour).','2000',1),('DRILLSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Drill Speed (Books per Hour)','2000',1),('FACILITY','2017-07-27 12:02:00','admin',NULL,NULL,'France Facility/factoy','Mexico',11),('FILEREPOSITORY','2016-06-15 09:28:49','','2017-09-25 10:59:33','admin','Location of the pdf files','/opt/pacex/repository/files',6),('IMPOSITION_URL','2017-04-27 16:30:31','walidb@epac.com',NULL,NULL,'Imposition Tool Url','http://localhost:8080/imposer/',6),('LAMINATIONSPEED','2017-06-13 13:18:35','admin',NULL,NULL,'Lamination Speed (Books per Hour).','2000',1),('ORDERREPOSITORY','2016-11-10 00:00:00','system','2017-04-27 17:02:55','walidb@epac.com','Repository of upload of XML files','sftp://tunis@sftp.epac.com/incoming/Islem_dont_delete/upload',6),('OVERS','2017-02-07 15:10:10','','2017-05-18 19:47:04','admin','Overs: the percentage that can be added to the produced quantity for the order to be satisfied (in %)','0',2),('OVERSADDITIF','2017-04-05 05:33:16','','2017-05-18 19:47:12','admin','A minimum value added (to the original order quantity) in addition to the overs','5',2),('PLASTICOILSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Plastic coil speed (Books per Hour)','2000',1),('PLOWFOLDERSPEED','2017-03-27 00:00:00','walidb','2017-05-18 19:45:05','admin','Plow Folder Speed (feet per hour)','55000',1),('POLINE_IMPOSITION_URL','2017-05-19 16:01:13','admin','2017-05-19 16:10:55','admin','Pop Line Imposition URL','http://localhost:8080/backend/imposer/popline/execute',6),('PRESSSPEED','2016-05-02 00:00:00','walidb','2017-06-14 07:35:01','admin','Printer speed (In feet/h or meter/h): \n20000 is 6000 meter/h (100 meter/min)\n25000 is 127 meter/min','20000',1),('RECEIVEREMAILS','2016-11-10 00:00:00','system',NULL,NULL,'Email of the receiver','islemt.epac@gmail.com',3),('ROLLINITLENGTH','2016-05-25 14:49:49','','2016-09-27 09:28:16','','Roll initial Length (feet)','54500',5),('ROLLMINLENGTH','2017-04-27 16:28:30','walidb@epac.com',NULL,NULL,'Minimum length of a roll; useful for when to allow creating left over roll...','20',5),('ROLLWIDTH','2016-05-19 11:29:55','','2017-06-14 07:10:38','admin','Roll width in inches (105 in cm).\nSpecial case: 19 inches for books 9*12','18.06',5),('SENDEREMAIL','2016-11-10 00:00:00','system',NULL,NULL,'Email of the sender','islemt@epac.tn',3),('SHIFT','2016-05-25 14:59:12','','2017-03-27 09:52:24','','Shifts of the day: Starting from 0h until 24h, indicate the working hours...\nUse - for the duration and separate shifts by ;\nTo indicate if the weekend is a working day, add :w otherwise add :nw','0-2;9-17;18-24:nw',4),('SHRINKWRAPSPEED','2017-03-27 00:00:00','walidb',NULL,NULL,'Shrink wrap speed (Books per Hour)','2000',1),('TRIMMERSPEED','2017-03-27 00:00:00','walidb','2017-05-18 19:46:48','admin','Trimmer Speed (Books per Hour).','1200',1),('UNDERS','2017-02-07 15:10:43','','2017-10-03 14:56:11','admin','Unders: the percentage that can be subtracted from the produced quantity for the order to be satisfied (in %)','0',2),('UNDERSADDITIF','2017-04-05 05:35:11','','2017-05-18 19:47:28','admin','A minimum value subtracted (from the original order quantity) in addition to the unders','0',2),('UNITSYSTEM','2017-06-14 07:03:35','admin','2017-11-01 20:24:14','admin','Measurement Unit System: American or French System.\n\'US\' for American System: Uses Inch, Foot, ft/h...\n\'FR\' for French System: uses mm, m, m/mn...','US',10);
/*!40000 ALTER TABLE `preference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `priority`
--

LOCK TABLES `priority` WRITE;
/*!40000 ALTER TABLE `priority` DISABLE KEYS */;
INSERT INTO `priority` (`Priority_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('HIGH','2016-05-02 00:00:00','walidb',NULL,NULL,'High','High'),('NORMAL','2016-05-25 10:20:00','',NULL,NULL,'Normal','Normal');
/*!40000 ALTER TABLE `priority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`Role_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Role_Description`, `Role_Name`) VALUES ('ROLE_ADMIN','2017-04-29 00:00:00','walidb',NULL,NULL,'Administrator','Administrator'),('ROLE_LOP','2016-05-24 18:09:57','walidb',NULL,NULL,'Lead Operator','Lead Operator'),('ROLE_OP','2016-05-24 18:10:13','walidb',NULL,NULL,'Operator','Operator'),('ROLE_PM','2016-05-24 18:09:16','walidb',NULL,NULL,'Project Manager','Project Manager');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `roll_Type`
--

LOCK TABLES `roll_Type` WRITE;
/*!40000 ALTER TABLE `roll_Type` DISABLE KEYS */;
INSERT INTO `roll_Type` (`Roll_Type_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('LEFTOVER','2016-05-25 10:20:27','',NULL,NULL,'Leftover','Leftover'),('NEW','2016-05-02 00:00:00','walidb',NULL,NULL,'New','New'),('PRODUCED','2016-05-25 10:21:01','',NULL,NULL,'Produced','Produced');
/*!40000 ALTER TABLE `roll_Type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `roll_status`
--

LOCK TABLES `roll_status` WRITE;
/*!40000 ALTER TABLE `roll_status` DISABLE KEYS */;
INSERT INTO `roll_status` (`Roll_Status_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('ASSIGNED','2016-08-19 00:00:00','walidb',NULL,NULL,'Assigned','Assigned'),('AVAILABLE','2016-05-02 00:00:00','walidb',NULL,NULL,'Available for printing','Available for printing'),('EXHAUSTED','2016-05-25 14:51:46','',NULL,NULL,'Exhausted','Retired'),('NEW','2016-05-25 14:50:54','',NULL,NULL,'New','New'),('ONPROD','2016-05-25 14:51:29','',NULL,NULL,'On Production','On Production'),('SCHEDULED','2016-05-25 14:51:09','',NULL,NULL,'Scheduled','Scheduled');
/*!40000 ALTER TABLE `roll_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
INSERT INTO `station` (`Station_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Active_Flag`, `Description`, `Input_Type`, `Name`, `Parent_Station_Id`, `Production_Capacity`, `Production_Ordering`, `Scheduled_Hours`, `Station_Category_Id`, `Unscheduled_Hours`) VALUES ('BINDER','2017-05-18 17:53:15','admin','2017-07-26 10:35:32','admin','','Finishing (Binder) Station','Sheet','Finishing','',NULL,6,NULL,'BINDER',NULL),('COVERPRESS','2017-05-18 17:52:01','admin',NULL,NULL,'','Cover Press Station','Sheet','Cover Press','',NULL,3,NULL,'COVERPRESS',NULL),('CUTTER','2017-05-18 17:52:38','admin','2017-06-13 13:14:41','admin','','Cutter Station','Sheet','Cutter','',NULL,5,NULL,'CUTTER',NULL),('LAMINATION','2017-06-13 13:04:42','admin','2017-07-27 11:40:00','admin','\0','Lamination Station','Sheet','Lamination','',NULL,4,NULL,'LAMINATION',NULL),('PLOWFOLDER','2017-05-18 17:49:38','admin','2017-07-24 16:58:50','admin','','Hunkeler (Finishing) Station','Roll','Hunkeler','',NULL,2,NULL,'PLOWFOLDER',NULL),('PRESS','2017-05-18 17:48:41','admin',NULL,NULL,'','The Press Station','Roll','Press','',NULL,1,NULL,'PRESS',NULL),('SHIPPING','2017-07-27 11:49:58','admin',NULL,NULL,'','Shipping; used to create a shipping job but the station itself won\'t be displayed/active','Job','Shipping','',NULL,8,NULL,'SHIPPING',NULL),('SHRINKWRAP','2017-05-18 17:54:26','admin','2017-07-27 11:50:32','admin','\0','ShrinkWrap Station','Sheet','ShrinkWrap','',NULL,7,NULL,'SHRINKWRAP',NULL);
/*!40000 ALTER TABLE `station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `station_Category`
--

LOCK TABLES `station_Category` WRITE;
/*!40000 ALTER TABLE `station_Category` DISABLE KEYS */;
INSERT INTO `station_Category` (`Category_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Description`, `Name`) VALUES ('BINDER','2016-07-28 00:00:00','walidb',NULL,NULL,'Binder','Binder'),('COVERPRESS','2016-07-29 12:05:01','',NULL,NULL,'Cover Press','Cover Press'),('CUTTER','2016-07-29 11:59:31','',NULL,NULL,'Cutter','Cutter'),('DRILL','2016-07-29 12:03:17','',NULL,NULL,'Drill','Drill'),('LAMINATION','2017-06-13 13:05:10','admin',NULL,NULL,'Lamination','Lamination'),('PLASTICOIL','2016-07-29 12:02:17','',NULL,NULL,'Plastic Coil','Plastic Coil'),('PLOWFOLDER','2016-07-29 12:05:23','','2017-07-24 16:58:08','admin','Hunkeler (Finishing) station type','Hunkeler'),('PRESS','2016-06-06 11:26:46','walidb',NULL,NULL,'Press','Press'),('SHIPPING','2017-07-27 11:42:03','admin',NULL,NULL,'Shipping Station','Shipping'),('SHRINKWRAP','2016-07-28 00:00:00','walidb',NULL,NULL,'Shrinkwrap','Shrinkwrap'),('TRIMMER','2016-07-28 00:00:00','wlidb',NULL,NULL,'Trimmer','Trimmer');
/*!40000 ALTER TABLE `station_Category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`User_Id`, `Creation_Date`, `Creator_id`, `Modification_Date`, `Modifier_Id`, `Active_Flag`, `Email`, `First_Name`, `language`, `Last_Name`, `Login_Name`, `Login_Password`, `Phone_Num`) VALUES ('loplntestcom','2017-05-02 09:05:24','admin',NULL,NULL,'','lopln@test.com','lop','en','lopln','lop','$2a$11$bhIwEYYc26ycOCdwG.PBcuiCjD03/SGENHeg0wm57NRLBkW5nvF5a','1114567890'),('oplntestcom','2017-05-02 09:04:50','admin',NULL,NULL,'','opln@test.com','op','en','opln','op','$2a$11$ecAkawTWtwKPh16ARWjcd.N4srbjErl5z2C0GvAGtlTmPt2O5nOEG','1234567890'),('pmlnpmcom','2017-05-02 09:04:16','admin',NULL,NULL,'','pmln@pm.com','pm','en','ln','pm','$2a$11$XDW3.nzsQvLBt.G7PLWtvuGk6hv4fbKNZrw2fPrJbbVsR7n8ESrgq','6578909877'),('walidb','2016-05-02 00:00:00','walidb','2017-05-02 09:06:03','admin','','walidb@epac.com','admin','en','adminln','admin','$2a$11$lmlF5gbPbI7E7EQk3iPGBeGBosMhws.KN7ecad0JEMVnXnQSaK4Ja','1231231233');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_Role`
--

LOCK TABLES `user_Role` WRITE;
/*!40000 ALTER TABLE `user_Role` DISABLE KEYS */;
INSERT INTO `user_Role` (`Role_Id`, `User_Id`) VALUES ('ROLE_LOP','loplntestcom'),('ROLE_OP','oplntestcom'),('ROLE_PM','pmlnpmcom'),('ROLE_ADMIN','walidb');
/*!40000 ALTER TABLE `user_Role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-11-10 19:42:40

