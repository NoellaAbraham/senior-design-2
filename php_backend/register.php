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

// Retrieve data from POST request
$name = $_POST['name'];
$age = $_POST['age'];
$usertype = $_POST['usertype'];
$email = $_POST['email'];
$password = $_POST['password'];

// Prepare and execute SQL statement
$sql = "INSERT INTO usert (name, password, email, age, usertype) VALUES ('$name', '$password', '$email', $age, '$usertype')";

if ($conn->query($sql) === TRUE) {
    $user_id = $conn->insert_id; // Get the last inserted ID

    // Check usertype and insert into corresponding table
    if ($usertype === 'student') {
        $student_name = $name;
        $sql_student = "INSERT INTO student (student_id, student_name) VALUES ('$user_id', '$student_name')";
        if ($conn->query($sql_student) !== TRUE) {
            $response = array("success" => false, "message" => "Error inserting into student table: " . $conn->error);
            echo json_encode($response);
            $conn->close();
            exit;
        }
    } elseif ($usertype === 'caretaker') {
        $caregiver_name = $name;
        $sql_caregiver = "INSERT INTO caregiver (caregiver_id, caregiver_name) VALUES ('$user_id', '$caregiver_name')";
        if ($conn->query($sql_caregiver) !== TRUE) {
            $response = array("success" => false, "message" => "Error inserting into caregiver table: " . $conn->error);
            echo json_encode($response);
            $conn->close();
            exit;
        }
    }

    $response = array("success" => true, "message" => "User registered successfully");
} else {
    $response = array("success" => false, "message" => "Error: " . $sql . "<br>" . $conn->error);
}

echo json_encode($response);

$conn->close();
?>
