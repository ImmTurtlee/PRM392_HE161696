<?php
header("Content-Type: application/json");

// Kết nối CSDL
$conn = new mysqli("localhost", "root", "", "barber_shop");
$conn->set_charset("utf8");

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Connection failed: " . $conn->connect_error]));
}

// Truy vấn lấy danh sách barber
$sql = "SELECT BarberId, FullName, Email, AverageRating, RatingCount, Image_barber FROM barbers";
$result = $conn->query($sql);

$barbers = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $barbers[] = $row;
    }
}

echo json_encode([
    "success" => true,
    "barbers" => $barbers
]);
?>
