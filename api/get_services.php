<?php
header("Content-Type: application/json");

// Kết nối cơ sở dữ liệu
$servername = "localhost";
$username = "root";
$password = "";
$database = "barber_shop";

$conn = new mysqli($servername, $username, $password, $database);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Kết nối thất bại: " . $conn->connect_error]);
    exit();
}

$sql = "SELECT ServiceId, Name, Price, ServiceTime FROM service";
$result = $conn->query($sql);

if (!$result) {
    echo json_encode(["success" => false, "message" => "Lỗi SQL: " . $conn->error]);
    exit();
}

$services = [];
while ($row = $result->fetch_assoc()) {
    // Chuyển ServiceTime từ chuỗi sang số phút
    $serviceTimeStr = $row['ServiceTime']; // "00:30:00"
    list($h, $m, $s) = explode(':', $serviceTimeStr);
    $minutes = intval($h) * 60 + intval($m);
    $row['ServiceTime'] = $minutes;
    $services[] = $row;
}

echo json_encode($services);
$conn->close();
?>