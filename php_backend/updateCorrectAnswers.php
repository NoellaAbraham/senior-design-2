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

// Define the total number of questions for each GameID
$questionsPerGame = array(
    1 => 12,
    2 => 12,
    3 => 2,
    // Add more GameID => total questions pairs if needed
);

// Fetch all rows from the score table
$sql = "SELECT ScoreID, GameID, WrongAnswers FROM score WHERE userID = $userID";
$result = $conn->query($sql);

$updatedScores = array();

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $scoreID = $row['ScoreID'];
        $gameID = $row['GameID'];
        $wrongAnswers = $row['WrongAnswers'];

        // Check if GameID is in the questionsPerGame array
        if (isset($questionsPerGame[$gameID])) {
            $totalQuestions = $questionsPerGame[$gameID];
            $correctAnswers = $totalQuestions - $wrongAnswers;

            // Update the score table with the calculated correctAnswers
            $updateSql = "UPDATE score SET CorrectAnswers = ? WHERE ScoreID = ?";
            $stmt = $conn->prepare($updateSql);
            $stmt->bind_param("ii", $correctAnswers, $scoreID);

            if ($stmt->execute()) {
                $updatedScores[] = array(
                    "ScoreID" => $scoreID,
                    "GameID" => $gameID,
                    "CorrectAnswers" => $correctAnswers
                );
            } else {
                echo "Error updating ScoreID: $scoreID\n";
            }

            $stmt->close();
        }
    }
} else {
    echo "No records found in the score table.";
}

header('Content-Type: application/json');
echo json_encode($updatedScores);

$conn->close();
?>
