<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

header("Content-Type: application/json");

$servername = "localhost";
$dbUsername = "root";
$dbPassword = "";
$database = "moodmap";

// Create connection
$conn = new mysqli($servername, $dbUsername, $dbPassword, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get username and password from POST or GET
$username = $_POST['username'] ?? $_GET['username'] ?? '';
$password = $_POST['password'] ?? $_GET['password'] ?? '';

// === First try usert table (students, caregivers) ===
$stmt = $conn->prepare("SELECT userID, userType FROM usert WHERE name = ? AND password = ?");
$stmt->bind_param("ss", $username, $password);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // Found in usert table
    $row = $result->fetch_assoc();
    echo json_encode([
        "status" => "success",
        "userID" => $row["userID"],
        "userType" => $row["userType"]
    ]);
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

// === Try doctors table if not found in usert ===
$stmt = $conn->prepare("SELECT id FROM doctors WHERE email = ? AND password = ?");
$stmt->bind_param("ss", $username, $password);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        "status" => "success",
        "userID" => $row["id"],
        "userType" => "doctor"
    ]);
} else {
    echo json_encode(["status" => "failure"]);
}

$stmt->close();
$conn->close();
?>
