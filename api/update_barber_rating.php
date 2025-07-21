<?php
header('Content-Type: application/json');
if (!isset($_POST['barberId']) || !isset($_POST['rating'])) {
    echo json_encode(["success" => false, "message" => "Thiếu tham số"]);
    exit;
}
$barberId = $_POST['barberId'];
$rating = floatval($_POST['rating']);

$conn = new mysqli("localhost", "root", "", "barber_shop");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Lỗi kết nối CSDL"]);
    exit;
}

// Lấy rating hiện tại
$sql = "SELECT AverageRating, RatingCount FROM barbers WHERE BarberId = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $barberId);
$stmt->execute();
$stmt->bind_result($avg, $count);
if ($stmt->fetch()) {
    $newCount = $count + 1;
    $newAvg = ($avg * $count + $rating) / $newCount;
    $stmt->close();

    // Update lại DB
    $update = $conn->prepare("UPDATE barbers SET AverageRating = ?, RatingCount = ? WHERE BarberId = ?");
    $update->bind_param("dii", $newAvg, $newCount, $barberId);
    if ($update->execute()) {
        echo json_encode(["success" => true, "message" => "Đánh giá thành công"]);
    } else {
        echo json_encode(["success" => false, "message" => "Lỗi cập nhật"]);
    }
    $update->close();
} else {
    echo json_encode(["success" => false, "message" => "Không tìm thấy barber"]);
}
$conn->close();
?>