<?php
header('Content-Type: application/json');

// Kết nối database
$host = "localhost";
$user = "root";
$pass = "";
$db = "barber_shop";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["error" => "Database connection failed"]);
    exit();
}

// Lấy barberId từ query string
$barberId = isset($_GET['barberId']) ? intval($_GET['barberId']) : 0;
if ($barberId <= 0) {
    http_response_code(400);
    echo json_encode(["error" => "Missing or invalid barberId"]);
    exit();
}

// Truy vấn danh sách lịch hẹn của barber
$sql = "SELECT AppointmentId, ClientId, BarberId, AppointmentDate, StartTime, Status, Rating
        FROM appointments
        WHERE BarberId = ? 
        ORDER BY AppointmentDate DESC, StartTime DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $barberId);
$stmt->execute();
$result = $stmt->get_result();

$appointments = [];
while ($row = $result->fetch_assoc()) {
    $appointmentId = $row['AppointmentId'];

    // Lấy danh sách services cho appointment này
    $serviceSql = "SELECT s.ServiceId, s.Name as ServiceName, s.Price, s.ServiceTime
                   FROM appointment_services aps
                   JOIN service s ON aps.ServiceId = s.ServiceId
                   WHERE aps.AppointmentId = ?";
    $serviceStmt = $conn->prepare($serviceSql);
    $serviceStmt->bind_param("i", $appointmentId);
    $serviceStmt->execute();
    $serviceResult = $serviceStmt->get_result();

    $services = [];
    while ($serviceRow = $serviceResult->fetch_assoc()) {
        // Chuyển ServiceTime từ chuỗi sang số phút
        $serviceTimeStr = $serviceRow['ServiceTime']; // "00:30:00"
        list($h, $m, $s) = explode(':', $serviceTimeStr);
        $minutes = intval($h) * 60 + intval($m);
        $services[] = [
            "ServiceId" => $serviceRow['ServiceId'],
            "ServiceName" => $serviceRow['ServiceName'],
            "Price" => $serviceRow['Price'],
            "ServiceTime" => $minutes // Trả về số phút
        ];
    }
    $row['services'] = $services;
    $appointments[] = $row;

    $serviceStmt->close();
}

echo json_encode($appointments);

$stmt->close();
$conn->close();
?>