<?php
header('Content-Type: application/json');

// Kiểm tra tham số clientId có tồn tại không
if (!isset($_GET['clientId'])) {
    http_response_code(400);
    echo json_encode(["error" => "Thiếu tham số clientId"]);
    exit;
}

$clientId = $_GET['clientId'];

// Kết nối CSDL
$conn = new mysqli("localhost", "root", "", "barber_shop");

// Kiểm tra lỗi kết nối
if ($conn->connect_error) {
    echo json_encode(["error" => "Lỗi kết nối CSDL: " . $conn->connect_error]);
    exit;
}

// Lấy danh sách appointments của client
$sql = "SELECT * FROM appointments WHERE clientId = ?";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode(["error" => "Lỗi prepare: " . $conn->error]);
    exit;
}

$stmt->bind_param("i", $clientId);
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