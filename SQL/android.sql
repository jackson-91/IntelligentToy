-- phpMyAdmin SQL Dump
-- version 4.4.10
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: 2015-11-03 15:04:27
-- 服务器版本： 5.5.44-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `android`
--
CREATE DATABASE IF NOT EXISTS `android` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `android`;

-- --------------------------------------------------------

--
-- 表的结构 `device_data`
--

DROP TABLE IF EXISTS `device_data`;
CREATE TABLE IF NOT EXISTS `device_data` (
  `DeviceId` char(10) NOT NULL COMMENT '设备ID，唯一标识不同设备的属性',
  `DeviceOwner` int(5) DEFAULT NULL COMMENT '外键，设备所有者的用户ID',
  `DeviceState` enum('offline','online') NOT NULL DEFAULT 'offline'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `user_birthday`
--

DROP TABLE IF EXISTS `user_birthday`;
CREATE TABLE IF NOT EXISTS `user_birthday` (
  `UserId` int(5) DEFAULT NULL,
  `NickName` char(10) DEFAULT NULL COMMENT '用户宝宝的昵称',
  `Sex` char(10) DEFAULT NULL COMMENT '用户宝宝的性别',
  `Birthday` date DEFAULT NULL COMMENT '用户宝宝的生日'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `user_data`
--

DROP TABLE IF EXISTS `user_data`;
CREATE TABLE IF NOT EXISTS `user_data` (
  `UserId` int(5) NOT NULL COMMENT '用户ID，唯一标识不同用户的属性',
  `UserName` char(10) DEFAULT NULL COMMENT '用户名，用户的昵称',
  `WXUserId` varchar(255) DEFAULT NULL COMMENT '绑定的微信账号的ID',
  `Password` char(10) DEFAULT NULL COMMENT '使用微信登陆，暂不需要密码',
  `WebpageCode` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `user_mode`
--

DROP TABLE IF EXISTS `user_mode`;
CREATE TABLE IF NOT EXISTS `user_mode` (
  `UserId` int(5) DEFAULT NULL,
  `Mode` varchar(20) DEFAULT NULL COMMENT '用户最后一次按下的模式',
  `addTimestamp` int(11) DEFAULT NULL COMMENT '用户进入此模式的时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `weixin_token`
--

DROP TABLE IF EXISTS `weixin_token`;
CREATE TABLE IF NOT EXISTS `weixin_token` (
  `AppId` varchar(255) DEFAULT NULL COMMENT '公众号的AppId',
  `AppSecret` varchar(255) DEFAULT NULL COMMENT '公众号的AppSecret',
  `Access_Token` varchar(255) DEFAULT NULL COMMENT '根据AppId和AppSecret获取的AccessToken',
  `addTimestamp` int(11) DEFAULT NULL COMMENT '添加AccessToken的时间',
  `expire` int(11) DEFAULT NULL COMMENT 'AccessToken的有效时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `device_data`
--
ALTER TABLE `device_data`
  ADD PRIMARY KEY (`DeviceId`),
  ADD KEY `DeviceOwner` (`DeviceOwner`);

--
-- Indexes for table `user_birthday`
--
ALTER TABLE `user_birthday`
  ADD KEY `UserId` (`UserId`);

--
-- Indexes for table `user_data`
--
ALTER TABLE `user_data`
  ADD PRIMARY KEY (`UserId`);

--
-- Indexes for table `user_mode`
--
ALTER TABLE `user_mode`
  ADD KEY `UserId` (`UserId`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `user_data`
--
ALTER TABLE `user_data`
  MODIFY `UserId` int(5) NOT NULL AUTO_INCREMENT COMMENT '用户ID，唯一标识不同用户的属性';
--
-- 限制导出的表
--

--
-- 限制表 `device_data`
--
ALTER TABLE `device_data`
  ADD CONSTRAINT `device_data_ibfk_1` FOREIGN KEY (`DeviceOwner`) REFERENCES `user_data` (`UserId`);

--
-- 限制表 `user_birthday`
--
ALTER TABLE `user_birthday`
  ADD CONSTRAINT `user_birthday_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `user_data` (`UserId`);

--
-- 限制表 `user_mode`
--
ALTER TABLE `user_mode`
  ADD CONSTRAINT `user_mode_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `user_data` (`UserId`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
