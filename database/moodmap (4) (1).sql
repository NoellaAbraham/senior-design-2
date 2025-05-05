-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 30, 2025 at 01:03 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `moodmap`
--

-- --------------------------------------------------------

--
-- Table structure for table `admint`
--

CREATE TABLE `admint` (
  `AdminID` int(11) NOT NULL,
  `AName` varchar(50) DEFAULT NULL,
  `Role` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `caregiver`
--

CREATE TABLE `caregiver` (
  `caregiver_id` int(11) NOT NULL,
  `caregiver_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `caregiver`
--

INSERT INTO `caregiver` (`caregiver_id`, `caregiver_name`) VALUES
(5, 'test5');

-- --------------------------------------------------------

--
-- Table structure for table `caregiver_student`
--

CREATE TABLE `caregiver_student` (
  `caregiver_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `caregiver_student`
--

INSERT INTO `caregiver_student` (`caregiver_id`, `student_id`) VALUES
(5, 6),
(5, 7);

-- --------------------------------------------------------

--
-- Table structure for table `customization`
--

CREATE TABLE `customization` (
  `CustomID` int(11) NOT NULL,
  `UserID` int(11) DEFAULT NULL,
  `SensPreference` varchar(50) DEFAULT NULL,
  `LearningLevel` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `doctorresources`
--

CREATE TABLE `doctorresources` (
  `id` int(11) NOT NULL,
  `doctorID` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `filePath` text NOT NULL,
  `uploadDate` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `doctors`
--

CREATE TABLE `doctors` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `specialization` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctors`
--

INSERT INTO `doctors` (`id`, `name`, `specialization`, `email`, `password`, `created_at`) VALUES
(4, 'Dr. Ahmed', 'Autism Specialist', 'ahmed@example.com', 'ahmed123', '2025-04-30 10:08:06');

-- --------------------------------------------------------

--
-- Table structure for table `feedback`
--

CREATE TABLE `feedback` (
  `FeedbackID` int(11) NOT NULL,
  `UserID` int(11) DEFAULT NULL,
  `Message` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `feedbacks`
--

CREATE TABLE `feedbacks` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `feedbacks`
--

INSERT INTO `feedbacks` (`id`, `name`, `email`, `message`, `created_at`) VALUES
(1, 'noel', 'noelz@gmail.com', 'more games more personalization', '2025-04-30 10:16:12');

-- --------------------------------------------------------

--
-- Table structure for table `gametype`
--

CREATE TABLE `gametype` (
  `GameID` int(11) NOT NULL,
  `GName` varchar(50) DEFAULT NULL,
  `Description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `gametype`
--

INSERT INTO `gametype` (`GameID`, `GName`, `Description`) VALUES
(1, 'emotionRecog', 'user learns and recognizes emotions'),
(2, 'Emotions Through Videos', 'learning emotions through real-life video situations'),
(3, 'Match The Emotion', 'matching the emotion to the text'),
(4, 'AI Emotion Tracking Game', 'imitating emotions from videos');

-- --------------------------------------------------------

--
-- Table structure for table `levelt`
--

CREATE TABLE `levelt` (
  `LevelID` int(11) NOT NULL,
  `LevelName` varchar(50) DEFAULT NULL,
  `GameID` int(11) DEFAULT NULL,
  `Difficulty` varchar(20) DEFAULT NULL,
  `Description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `levelt`
--

INSERT INTO `levelt` (`LevelID`, `LevelName`, `GameID`, `Difficulty`, `Description`) VALUES
(1, 'Game 1 Level 1', 1, 'Easy', 'Emotion recognition level 1'),
(2, 'Game 1 Level 0', 1, 'very easy', 'Emotion recognition level 0'),
(3, 'Game 1 Level 2', 1, 'Easy', 'Emotion recognition level 2'),
(7, 'Game 2 Level 1.1', 2, 'Medium', 'Happy Situation Emotion Video'),
(8, 'Game 2 Level 1.2', 2, 'Medium', 'Angry Situation Emotion Video'),
(9, 'Game 2 Level 1.3', 2, 'Medium', 'Sad Situation Emotion Video'),
(10, 'Game 2 Level 1.4', 2, 'Medium', 'Fear Situation Emotion Video'),
(15, 'Game 3', 3, 'Medium-Hard', 'Match The Emotions'),
(31, 'Game 2 Level 3.1', 2, 'Medium', 'Situational Awareness Video'),
(32, 'Game 2 Level 3.2', 2, 'Medium', 'Situational Awareness Video'),
(41, 'Game 4', 4, 'Hard', 'Happy AI Emotion Matching Game'),
(42, 'Game 4', 4, 'Hard', 'Sad AI Emotion Matching Game'),
(43, 'Game 4', 4, 'Hard', 'Neutral AI Emotion Matching Game'),
(44, 'Game 4', 4, 'Hard', 'Angry AI Emotion Matching Game'),
(45, 'Game 4', 4, 'Hard', 'Surprise AI Emotion Matching Game'),
(46, 'Game 4', 4, 'Hard', 'Fear AI Emotion Matching Game');

-- --------------------------------------------------------

--
-- Table structure for table `progressreport`
--

CREATE TABLE `progressreport` (
  `ProgID` int(11) NOT NULL,
  `UserID` int(11) DEFAULT NULL,
  `GameID` int(11) DEFAULT NULL,
  `LevelID` int(11) DEFAULT NULL,
  `progress` int(11) DEFAULT NULL,
  `lastUpdate` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `progressreport`
--

INSERT INTO `progressreport` (`ProgID`, `UserID`, `GameID`, `LevelID`, `progress`, `lastUpdate`) VALUES
(1, 7, 1, 2, 20, '2024-04-30 18:38:00'),
(2, 7, 1, 1, 40, '2024-05-03 10:45:21'),
(4, 7, 1, 3, 40, '2024-05-03 10:45:36');

-- --------------------------------------------------------

--
-- Table structure for table `score`
--

CREATE TABLE `score` (
  `ScoreID` int(11) NOT NULL,
  `UserID` int(11) DEFAULT NULL,
  `GameID` int(11) DEFAULT NULL,
  `LevelID` int(11) DEFAULT NULL,
  `WrongAnswers` int(11) DEFAULT NULL,
  `time` varchar(5) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `score`
--

INSERT INTO `score` (`ScoreID`, `UserID`, `GameID`, `LevelID`, `WrongAnswers`, `time`) VALUES
(1, NULL, NULL, NULL, 3, NULL),
(2, NULL, NULL, NULL, 0, NULL),
(8, 1, 1, 1, 3, NULL),
(9, 2, 1, 1, 5, NULL),
(10, 6, 1, 1, 4, '10987'),
(11, 7, 1, 1, 3, '0'),
(12, 7, 1, 1, 6, '00:11'),
(13, 7, 1, 1, 5, '00:05'),
(14, NULL, NULL, NULL, 4, NULL),
(15, 7, 1, 1, 5, '00:05'),
(16, NULL, NULL, NULL, 0, NULL),
(17, 7, 1, 1, 5, '00:06'),
(18, NULL, NULL, NULL, 5, NULL),
(19, 6, 1, 1, 0, '00:06'),
(20, 6, 1, 2, 0, '00:06'),
(21, 6, 2, 7, 0, '00:18'),
(22, 6, 2, 8, 5, '00:31'),
(23, 6, 2, 9, 1, '00:22'),
(24, 6, 2, 10, 0, '00:13'),
(25, 6, 2, 31, 0, '00:51'),
(26, 6, 2, 32, 1, '00:53'),
(27, 6, 2, 32, 1, '00:53'),
(28, 6, 3, 15, 4, '00:20');

-- --------------------------------------------------------

--
-- Table structure for table `student`
--

CREATE TABLE `student` (
  `student_id` int(11) NOT NULL,
  `student_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `student`
--

INSERT INTO `student` (`student_id`, `student_name`) VALUES
(6, 'test6'),
(7, 'test7');

-- --------------------------------------------------------

--
-- Table structure for table `usert`
--

CREATE TABLE `usert` (
  `UserID` int(11) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `Password` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `Age` int(11) DEFAULT NULL,
  `userType` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `usert`
--

INSERT INTO `usert` (`UserID`, `name`, `Password`, `Email`, `Age`, `userType`) VALUES
(1, 'test', 'test123', 'test@gmail.com', 12, 'student'),
(2, 'test2', 'test123', 'test2@gmail.com', 11, 'student'),
(3, 'test3', 'test123', 'test3@gmail.com', 10, 'student'),
(4, 'test4', 'test123', 'test4@gmail.com', 23, 'caretaker'),
(5, 'test5', 'test123', 'test5@gmail.com', 24, 'caretaker'),
(6, 'test6', 'test123', 'test6@gmail.com', 10, 'student'),
(7, 'test7', 'test123', 'test7@gmail.com', 9, 'student'),
(8, 'Dr. Ahmed', 'ahmed123', 'ahmed@example.com', 35, 'doctor');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admint`
--
ALTER TABLE `admint`
  ADD PRIMARY KEY (`AdminID`);

--
-- Indexes for table `caregiver`
--
ALTER TABLE `caregiver`
  ADD PRIMARY KEY (`caregiver_id`);

--
-- Indexes for table `caregiver_student`
--
ALTER TABLE `caregiver_student`
  ADD PRIMARY KEY (`caregiver_id`,`student_id`),
  ADD KEY `student_id` (`student_id`);

--
-- Indexes for table `customization`
--
ALTER TABLE `customization`
  ADD PRIMARY KEY (`CustomID`),
  ADD KEY `UserID` (`UserID`);

--
-- Indexes for table `doctorresources`
--
ALTER TABLE `doctorresources`
  ADD PRIMARY KEY (`id`),
  ADD KEY `doctorID` (`doctorID`);

--
-- Indexes for table `doctors`
--
ALTER TABLE `doctors`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `feedback`
--
ALTER TABLE `feedback`
  ADD PRIMARY KEY (`FeedbackID`),
  ADD KEY `UserID` (`UserID`);

--
-- Indexes for table `feedbacks`
--
ALTER TABLE `feedbacks`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `gametype`
--
ALTER TABLE `gametype`
  ADD PRIMARY KEY (`GameID`);

--
-- Indexes for table `levelt`
--
ALTER TABLE `levelt`
  ADD PRIMARY KEY (`LevelID`),
  ADD KEY `GameID` (`GameID`);

--
-- Indexes for table `progressreport`
--
ALTER TABLE `progressreport`
  ADD PRIMARY KEY (`ProgID`),
  ADD KEY `UserID` (`UserID`),
  ADD KEY `GameID` (`GameID`),
  ADD KEY `LevelID` (`LevelID`);

--
-- Indexes for table `score`
--
ALTER TABLE `score`
  ADD PRIMARY KEY (`ScoreID`),
  ADD KEY `UserID` (`UserID`),
  ADD KEY `GameID` (`GameID`),
  ADD KEY `LevelID` (`LevelID`);

--
-- Indexes for table `student`
--
ALTER TABLE `student`
  ADD PRIMARY KEY (`student_id`);

--
-- Indexes for table `usert`
--
ALTER TABLE `usert`
  ADD PRIMARY KEY (`UserID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admint`
--
ALTER TABLE `admint`
  MODIFY `AdminID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `caregiver`
--
ALTER TABLE `caregiver`
  MODIFY `caregiver_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `customization`
--
ALTER TABLE `customization`
  MODIFY `CustomID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `doctorresources`
--
ALTER TABLE `doctorresources`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `doctors`
--
ALTER TABLE `doctors`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `feedback`
--
ALTER TABLE `feedback`
  MODIFY `FeedbackID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `feedbacks`
--
ALTER TABLE `feedbacks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `gametype`
--
ALTER TABLE `gametype`
  MODIFY `GameID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `levelt`
--
ALTER TABLE `levelt`
  MODIFY `LevelID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- AUTO_INCREMENT for table `progressreport`
--
ALTER TABLE `progressreport`
  MODIFY `ProgID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `score`
--
ALTER TABLE `score`
  MODIFY `ScoreID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `student`
--
ALTER TABLE `student`
  MODIFY `student_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `usert`
--
ALTER TABLE `usert`
  MODIFY `UserID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `caregiver_student`
--
ALTER TABLE `caregiver_student`
  ADD CONSTRAINT `caregiver_student_ibfk_1` FOREIGN KEY (`caregiver_id`) REFERENCES `caregiver` (`caregiver_id`),
  ADD CONSTRAINT `caregiver_student_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`);

--
-- Constraints for table `customization`
--
ALTER TABLE `customization`
  ADD CONSTRAINT `customization_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `usert` (`UserID`);

--
-- Constraints for table `doctorresources`
--
ALTER TABLE `doctorresources`
  ADD CONSTRAINT `doctorresources_ibfk_1` FOREIGN KEY (`doctorID`) REFERENCES `usert` (`UserID`) ON DELETE CASCADE;

--
-- Constraints for table `feedback`
--
ALTER TABLE `feedback`
  ADD CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `usert` (`UserID`);

--
-- Constraints for table `levelt`
--
ALTER TABLE `levelt`
  ADD CONSTRAINT `levelt_ibfk_1` FOREIGN KEY (`GameID`) REFERENCES `gametype` (`GameID`);

--
-- Constraints for table `progressreport`
--
ALTER TABLE `progressreport`
  ADD CONSTRAINT `progressreport_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `usert` (`UserID`),
  ADD CONSTRAINT `progressreport_ibfk_2` FOREIGN KEY (`GameID`) REFERENCES `gametype` (`GameID`),
  ADD CONSTRAINT `progressreport_ibfk_3` FOREIGN KEY (`LevelID`) REFERENCES `levelt` (`LevelID`);

--
-- Constraints for table `score`
--
ALTER TABLE `score`
  ADD CONSTRAINT `score_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `usert` (`UserID`),
  ADD CONSTRAINT `score_ibfk_2` FOREIGN KEY (`GameID`) REFERENCES `gametype` (`GameID`),
  ADD CONSTRAINT `score_ibfk_3` FOREIGN KEY (`LevelID`) REFERENCES `levelt` (`LevelID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
