<?php

header("Content-Type: application/json");

// Database connection parameters
$servername = "localhost";
$username = "root";
$password = "";
$database = "moodmap";

$conn = new mysqli($servername, $username, $password, $database);

// Get POST data
$userID = $_POST['userID'];
$gameID = $_POST['gameID'];
$levelID = $_POST['levelID'];
$progress = $_POST['progress'];

// Check if progress record already exists for the user and level
$query = "SELECT * FROM progressreport WHERE userID = $userID AND gameID = $gameID AND levelID = $levelID";
$result = $conn->query($query);

if ($result->num_rows > 0) {
    // Update progress
    $sql = "UPDATE progressreport SET progress = $progress WHERE userID = $userID AND gameID = $gameID AND levelID = $levelID";
} else {
    // Insert new progress record
    $sql = "INSERT INTO progressreport (userID, gameID, levelID, progress) VALUES ($userID, $gameID, $levelID, $progress)";
}

if ($conn->query($sql) === TRUE) {
    echo "Progress saved successfully";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
?>
