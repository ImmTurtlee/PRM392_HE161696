<?php
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "barber_shop");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit();
}

$barberId = isset($_GET['barberId']) ? intval($_GET['barberId']) : 0;

if ($barberId <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Missing barberId"]);
    exit();
}

$stmt = $conn->prepare("SELECT FullName, Email, Image_barber FROM barbers WHERE BarberId = ?");
$stmt->bind_param("i", $barberId);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        "success" => true,
        "message" => "Profile loaded successfully",
        "fullName" => $row['FullName'],
        "email" => $row['Email'],
        "image" => $row['Image_barber'] // This can be a URL or base64 string
    ]);
} else {
    http_response_code(404);
    echo json_encode(["success" => false, "message" => "Barber not found"]);
}

$stmt->close();
$conn->close();
?>