<?php

header("Content-Type: application/json");

// Database connection parameters
$servername = "localhost";
$username = "root";
$password = "";
$database = "moodmap";

// Create connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database connection failed"]));
}

// Get POST data safely
$userID = $_POST['userID'] ?? '';
$gameID = $_POST['gameID'] ?? '';
$levelID = $_POST['levelID'] ?? '';
$mistakes = $_POST['mistakes'] ?? null; // mistakes may not exist for Game 4
$time = $_POST['time'] ?? '';

if (empty($userID) || empty($gameID) || empty($levelID) || empty($time)) {
    echo json_encode(["success" => false, "message" => "Missing parameters"]);
    exit();
}

// Prepare SQL statement
$sql = "INSERT INTO score (userID, gameID, levelID, WrongAnswers, time) VALUES (?, ?, ?, ?, ?)";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(["success" => false, "message" => "Prepare failed: " . $conn->error]);
    exit();
}

// If mistakes is missing, insert 0
$mistakes = $mistakes ?? 0;

// Bind parameters
$stmt->bind_param("iiiis", $userID, $gameID, $levelID, $mistakes, $time);

// Execute SQL statement
if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Score saved successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error saving score"]);
}

// Close connections
$stmt->close();
$conn->close();
?>
