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

// Query to check if the user exists in the table
$checkUserQuery = "SELECT * FROM progressreport WHERE userID = $userID";
$userResult = $conn->query($checkUserQuery);

if ($userResult->num_rows == 0) {
    // User does not exist in the table
    echo json_encode(array("error" => "User not found in the table"));
} else {
    // Query to calculate total progress for each game
    $query = "
        SELECT 
            gameID,
            SUM(progress) AS totalProgress
        FROM 
            progressreport 
        WHERE 
            userID = $userID 
        GROUP BY 
            gameID
    ";

    $result = $conn->query($query);

    $response = array();

    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            // Add gameID and totalProgress to response array
            $response[$row['gameID']] = $row['totalProgress'];
        }
    }

    echo json_encode($response);
}

$conn->close();
?>
