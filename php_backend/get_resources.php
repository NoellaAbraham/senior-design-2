<?php
$host = "localhost";
$user = "root";
$pass = "";
$dbname = "moodmap"; // your database name

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database connection failed."]));
}

// Fetch all doctor resources
$sql = "SELECT id, doctorID, title, description, filePath, uploadDate FROM doctorresources ORDER BY uploadDate DESC";
$result = $conn->query($sql);

$resources = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $resources[] = [
            "id" => $row["id"],
            "doctorID" => $row["doctorID"],
            "title" => $row["title"],
            "description" => $row["description"],
            "link" => $row["filePath"], 
            "uploadDate" => $row["uploadDate"]
        ];
    }

    echo json_encode(["success" => true, "resources" => $resources]);
} else {
    echo json_encode(["success" => true, "resources" => []]); // empty list, still success
}

$conn->close();
?>
