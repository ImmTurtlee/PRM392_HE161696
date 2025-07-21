<?php
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "barber_shop");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit();
}

$clientId = isset($_POST['clientId']) ? intval($_POST['clientId']) : 0;

if ($clientId <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Client ID is required"]);
    exit();
}

// Get client profile
$stmt = $conn->prepare("SELECT ClientId, FullName, Email FROM clients WHERE ClientId = ?");
$stmt->bind_param("i", $clientId);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows == 0) {
    http_response_code(404);
    echo json_encode(["success" => false, "message" => "Client not found"]);
    exit();
}

$row = $result->fetch_assoc();
echo json_encode([
    "success" => true,
    "data" => [
        "clientId" => $row['ClientId'],
        "fullName" => $row['FullName'],
        "email" => $row['Email']
    ]
]);

$stmt->close();
$conn->close();
?>