<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

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


$userID = $_POST['userID'];

if ($userID === null) {
    // Handle case where user ID is missing or invalid
    echo json_encode(["error" => "User ID parameter is missing or invalid"]);
} else {
    // Prepare SQL query to fetch the average emotion scores for the given user ID
    $query = "
        SELECT 
            AVG(`angry`) as avg_angry, 
            AVG(`disgust`) as avg_disgust, 
            AVG(`fear`) as avg_fear, 
            AVG(`happy`) as avg_happy, 
            AVG(`neutral`) as avg_neutral, 
            AVG(`sad`) as avg_sad, 
            AVG(`surprise`) as avg_surprise
        FROM emotion_score 
        WHERE UserID = ?
        AND prompt IN ('angry', 'disgust', 'fear', 'happy', 'neutral', 'sad', 'surprise')";

    $stmt = $conn->prepare($query);

    if (!$stmt) {
        // Handle query preparation error
        echo json_encode(["error" => "Query preparation error: " . $conn->error]);
    } else {
        $stmt->bind_param("i", $userID);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result) {
            if ($result->num_rows > 0) {
                $row = $result->fetch_assoc();
                echo json_encode($row);
            } else {
                echo json_encode(["error" => "No data found for the specified user ID"]);
            }
        } else {
            // Handle query execution error
            echo json_encode(["error" => "Query execution error: " . $stmt->error]);
        }
        
        // Close result set
        $result->close();
    }

    // Close statement
    $stmt->close();
}

// Close connection
$conn->close();

?>
