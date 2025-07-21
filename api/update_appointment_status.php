<?php
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "barber_shop");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit();
}

$appointmentId = isset($_POST['appointmentId']) ? intval($_POST['appointmentId']) : 0;
$status = isset($_POST['status']) ? $_POST['status'] : '';
$autoCancelPending = isset($_POST['autoCancelPending']) ? $_POST['autoCancelPending'] : 'false';

if ($appointmentId <= 0 || empty($status)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Thiếu appointmentId hoặc status"]);
    exit();
}

// Chỉ cho phép các trạng thái hợp lệ
$valid_status = ["Pending", "Confirmed", "Cancelled", "Completed"];
if (!in_array($status, $valid_status)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Trạng thái không hợp lệ"]);
    exit();
}

// Bắt đầu transaction
$conn->begin_transaction();

try {
    // Cập nhật appointment hiện tại
    $stmt = $conn->prepare("UPDATE appointments SET Status = ? WHERE AppointmentId = ?");
    $stmt->bind_param("si", $status, $appointmentId);
    
    if (!$stmt->execute()) {
        throw new Exception("Cập nhật appointment thất bại");
    }
    
    // Nếu chuyển sang Confirmed và có yêu cầu auto cancel
    if ($status == "Confirmed" && $autoCancelPending == "true") {
        // Lấy ClientId của appointment hiện tại
        $stmt = $conn->prepare("SELECT ClientId FROM appointments WHERE AppointmentId = ?");
        $stmt->bind_param("i", $appointmentId);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($row = $result->fetch_assoc()) {
            $clientId = $row['ClientId'];
            
            // Cancel tất cả appointment pending khác của cùng client
            $stmt = $conn->prepare("UPDATE appointments SET Status = 'Cancelled' 
                                   WHERE ClientId = ? AND Status = 'Pending' AND AppointmentId != ?");
            $stmt->bind_param("ii", $clientId, $appointmentId);
            
            if (!$stmt->execute()) {
                throw new Exception("Auto cancel appointments thất bại");
            }
            
            $affectedRows = $stmt->affected_rows;
            echo json_encode([
                "success" => true, 
                "message" => "Cập nhật thành công. Đã tự động cancel " . $affectedRows . " appointment(s) khác."
            ]);
        } else {
            throw new Exception("Không tìm thấy ClientId");
        }
    } else {
        echo json_encode(["success" => true, "message" => "Cập nhật thành công"]);
    }
    
    // Commit transaction
    $conn->commit();
    
} catch (Exception $e) {
    // Rollback nếu có lỗi
    $conn->rollback();
    http_response_code(500);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}

$stmt->close();
$conn->close();
?> 