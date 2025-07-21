<?php
header("Content-Type: application/json");

// Kết nối CSDL
$conn = new mysqli("localhost", "root", "", "barber_shop");
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Connection failed"]));
}

$clientId  = $_POST['clientId'] ?? null;
$barberId  = $_POST['barberId'] ?? null;
$date      = $_POST['date'] ?? null;
$time      = $_POST['time'] ?? null;
$services  = $_POST['services'] ?? null;

if (!$clientId || !$barberId || !$date || !$time || !$services) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin."]);
    exit();
}

// Giải mã mảng ServiceId
$serviceIds = json_decode($services, true);
if (!is_array($serviceIds) || count($serviceIds) == 0) {
    echo json_encode(["success" => false, "message" => "Danh sách dịch vụ không hợp lệ"]);
    exit();
}

// 1. Tạo bản ghi lịch hẹn mới
$stmt = $conn->prepare("INSERT INTO appointments (ClientId, BarberId, AppointmentDate, StartTime) VALUES (?, ?, ?, ?)");

$stmt->bind_param("iiss", $clientId, $barberId, $date, $time);
if (!$stmt->execute()) {
    echo json_encode(["success" => false, "message" => "Lỗi khi tạo lịch hẹn"]);
    exit();
}
$appointmentId = $stmt->insert_id;
$stmt->close();

// 2. Thêm từng dịch vụ vào bảng appointment_services
$successAll = true;
foreach ($serviceIds as $serviceId) {
    $stmt = $conn->prepare("INSERT INTO appointment_services (AppointmentId, ServiceId) VALUES (?, ?)");
    $stmt->bind_param("ii", $appointmentId, $serviceId);
    if (!$stmt->execute()) {
        $successAll = false;
        break;
    }
    $stmt->close();
}

if ($successAll) {
    echo json_encode(["success" => true, "message" => "Đặt lịch thành công"]);
} else {
    echo json_encode(["success" => false, "message" => "Lỗi khi thêm dịch vụ vào lịch hẹn"]);
}
$conn->close();
?>
