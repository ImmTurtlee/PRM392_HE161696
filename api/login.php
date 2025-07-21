<?php
header("Content-Type: application/json");
$conn = new mysqli("localhost", "root", "", "barber_shop");

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin"]);
    exit;
}

// Tìm trong bảng clients
$stmt = $conn->prepare("SELECT ClientId AS id, FullName FROM clients WHERE Email = ? AND Password = ?");
$stmt->bind_param("ss", $email, $password);
$stmt->execute();
$result = $stmt->get_result();
if ($row = $result->fetch_assoc()) {
    echo json_encode([
        "success" => true,
        "role" => "client",
        "userId" => $row['id'],
        "fullname" => $row['FullName']
    ]);
    exit;
}

// Tìm trong bảng barbers
$stmt = $conn->prepare("SELECT BarberId AS id, FullName FROM barbers WHERE Email = ? AND Password = ?");
$stmt->bind_param("ss", $email, $password);
$stmt->execute();
$result = $stmt->get_result();
if ($row = $result->fetch_assoc()) {
    echo json_encode([
        "success" => true,
        "role" => "barber",
        "userId" => $row['id'],
        "fullname" => $row['FullName']
    ]);
    exit;
}

// Không tìm thấy
echo json_encode(["success" => false, "message" => "Email hoặc mật khẩu không đúng"]);
