<?php

header("Content-Type: application/json");

// Database connection parameters
$servername = "localhost";
$username = "root";
$password = ""; // replace with your actual password
$database = "moodmap";

// Create connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get POST data
$userID = $_POST['userID'];
$angry = $_POST['angry'];
$disgust = $_POST['disgust'];
$fear = $_POST['fear'];
$happy = $_POST['happy'];
$neutral = $_POST['neutral'];
$sad = $_POST['sad'];
$surprise = $_POST['surprise'];
$prompt = $_POST['prompt'];
$time = $_POST['time'];
$date = $_POST['date'];

// Prepare SQL query to insert these values into emotion_score table
$query = "INSERT INTO emotion_score (UserID, angry, disgust, fear, happy, neutral, sad, surprise, prompt, time, date) 
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

// Prepare and bind
$stmt = $conn->prepare($query);
$stmt->bind_param("idddddddsss", $userID, $angry, $disgust, $fear, $happy, $neutral, $sad, $surprise, $prompt, $time, $date);

// Execute the statement
if ($stmt->execute()) {
    echo json_encode(["message" => "Emotion scores saved successfully"]);
} else {
    echo json_encode(["error" => $stmt->error]);
}

// Close connections
$stmt->close();
$conn->close();

?>
