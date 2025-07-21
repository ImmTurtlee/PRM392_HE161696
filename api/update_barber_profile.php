<?php
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "barber_shop");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit();
}

$barberId = isset($_POST['barberId']) ? intval($_POST['barberId']) : 0;
$fullName = isset($_POST['fullName']) ? $_POST['fullName'] : '';
$email = isset($_POST['email']) ? $_POST['email'] : '';
$currentPassword = isset($_POST['currentPassword']) ? $_POST['currentPassword'] : '';
$newPassword = isset($_POST['newPassword']) ? $_POST['newPassword'] : '';

if ($barberId <= 0 || empty($fullName) || empty($email) || empty($currentPassword)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Missing required fields"]);
    exit();
}

// Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid email format"]);
    exit();
}

// Check if email already exists in barbers table (excluding current barber)
$stmt = $conn->prepare("SELECT BarberId FROM barbers WHERE Email = ? AND BarberId != ?");
$stmt->bind_param("si", $email, $barberId);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Email already exists in barbers"]);
    exit();
}

// Check if email already exists in clients table
$stmt = $conn->prepare("SELECT ClientId FROM clients WHERE Email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Email already exists in clients"]);
    exit();
}

// Verify current password
$stmt = $conn->prepare("SELECT Password FROM barbers WHERE BarberId = ?");
$stmt->bind_param("i", $barberId);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows == 0) {
    http_response_code(404);
    echo json_encode(["success" => false, "message" => "Barber not found"]);
    exit();
}

$row = $result->fetch_assoc();
$storedPassword = $row['Password'];

// Verify current password (assuming password is stored as plain text for demo)
// In production, use password_verify() for hashed passwords
if ($currentPassword !== $storedPassword) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Current password is incorrect"]);
    exit();
}

// Update profile
if (!empty($newPassword)) {
    // Update with new password
    $stmt = $conn->prepare("UPDATE barbers SET FullName = ?, Email = ?, Password = ? WHERE BarberId = ?");
    $stmt->bind_param("sssi", $fullName, $email, $newPassword, $barberId);
} else {
    // Update without changing password
    $stmt = $conn->prepare("UPDATE barbers SET FullName = ?, Email = ? WHERE BarberId = ?");
    $stmt->bind_param("ssi", $fullName, $email, $barberId);
}

if ($stmt->execute()) {
    echo json_encode([
        "success" => true, 
        "message" => "Profile updated successfully",
        "data" => [
            "barberId" => $barberId,
            "fullName" => $fullName,
            "email" => $email
        ]
    ]);
} else {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to update profile"]);
}

$stmt->close();
$conn->close();
?> 