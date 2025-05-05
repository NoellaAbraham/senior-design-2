<?php
header("Content-Type: application/json");

// Database connection details
$servername = "localhost";
$username = "root";
$password = "";
$database = "moodmap";

// Create connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Retrieve caretaker's user ID from query parameter
$userID = $_GET['userID'];

// Prepare SQL statement to retrieve students associated with the caretaker
$sql = "SELECT s.student_id, s.student_name FROM student s INNER JOIN caregiver_student cs ON s.student_id = cs.student_id WHERE cs.caregiver_id = $userID";

$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $students = array();
    while($row = $result->fetch_assoc()) {
        $student = array(
            "student_id" => $row["student_id"],
            "student_name" => $row["student_name"]
        );
        $students[] = $student;
    }
    $response = array("success" => true, "students" => $students);
} else {
    $response = array("success" => false, "message" => "No students found for this caregiver");
}

echo json_encode($response);

$conn->close();
?>
