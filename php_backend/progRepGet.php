<?php

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$database = "moodmap";

$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get user ID from POST data (make sure to sanitize or validate input)
$userID = $_POST['userID'];

// Fetch data from score table
$sql = "SELECT gameID, MIN(wrongAnswers) AS leastWrongAnswers, levelID 
        FROM score 
        WHERE userID = $userID
        GROUP BY gameID, levelID";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    // Output data of each row
    $response = array();
    while($row = $result->fetch_assoc()) {
        $gameID = $row["gameID"];
        $leastWrongAnswers = $row["leastWrongAnswers"];
        $levelID = $row["levelID"];

        // Determine total number of questions based on levelID
        switch ($gameID) {
            case 1:
            case 2:
                $totalQuestions = 12; // Assuming levelID 1 and 2 have 6 questions each
                break;
            case 3:
                $totalQuestions = 2; // LevelID 3 has 0 questions
                break;
            default:
                $totalQuestions = 0; // Handle other cases as needed
        } 

        // Calculate average as per your formula
        if ($totalQuestions > 0) {
            $average = (int) round(($totalQuestions - $leastWrongAnswers) / $totalQuestions * $totalQuestions);
        } else {
            $average = 0; // Handle division by zero or undefined cases
        }

        $response["$gameID"] = array("average" => $average);
    }
    echo json_encode($response);
} else {
    echo json_encode(array("error" => "User not found"));
}

$conn->close();
?>
