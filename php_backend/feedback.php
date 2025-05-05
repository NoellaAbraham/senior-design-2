<?php
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "moodmap");
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "DB connection failed."]));
}

$name = $_POST['name'];
$email = $_POST['email'];
$message = $_POST['feedback'];

$sql = "INSERT INTO feedbacks (name, email, message) VALUES ('$name', '$email', '$message')";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "Feedback submitted!"]);
} else {
    echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
}

$conn->close();
?>
