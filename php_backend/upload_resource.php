<?php
// Connect to database
$host = "localhost";
$user = "root";
$pass = "";
$dbname = "moodmap"; 

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database connection failed."]));
}

// Get POST values
$doctorID = $_POST['doctorID'];
$title = $_POST['title'];
$description = $_POST['description'];
$link = $_POST['link']; // this is the external URL

// Validate required fields
if (empty($doctorID) || empty($title) || empty($link)) {
    echo json_encode(["success" => false, "message" => "doctorID, title, and link are required."]);
    exit;
}

// Insert into DoctorResources table
$stmt = $conn->prepare("INSERT INTO doctorresources (doctorID, title, description, filePath, uploadDate) VALUES (?, ?, ?, ?, NOW())");
$stmt->bind_param("isss", $doctorID, $title, $description, $link);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Resource added successfully."]);
} else {
    echo json_encode(["success" => false, "message" => "Insert failed: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
