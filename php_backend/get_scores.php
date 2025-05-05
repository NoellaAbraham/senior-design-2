<?php
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "moodmap");
if ($conn->connect_error) {
    echo json_encode(["error" => "Connection failed: " . $conn->connect_error]);
    exit;
}

$userID = isset($_GET['userID']) ? intval($_GET['userID']) : 0;

if ($userID === 0) {
    echo json_encode(["error" => "Missing or invalid userID"]);
    exit;
}

$sql = "SELECT gameID, levelID, WrongAnswers AS mistakes, time FROM score WHERE userID = ? AND gameID IS NOT NULL AND levelID IS NOT NULL AND time IS NOT NULL";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $userID);
$stmt->execute();
$result = $stmt->get_result();

$scores = [];
while ($row = $result->fetch_assoc()) {
    $time = trim($row["time"]);

    if (preg_match("/^\d{2}:\d{2}$/", $time)) {
        // Already in mm:ss format
    } elseif (ctype_digit($time)) {
        // Convert raw seconds to mm:ss
        $seconds = (int)$time;
        $minutes = floor($seconds / 60);
        $remaining = $seconds % 60;
        $time = sprintf("%02d:%02d", $minutes, $remaining);
    } else {
        $time = "00:00";
    }

    $scores[] = [
        "gameID" => (int)$row["gameID"],
        "levelID" => (int)$row["levelID"],
        "mistakes" => (int)$row["mistakes"],
        "time" => $time
    ];
}

echo json_encode($scores);

$stmt->close();
$conn->close();
?>
