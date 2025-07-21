<?php
header('Content-Type: application/json');
$conn = new mysqli("localhost", "root", "", "barber_shop");
if ($conn->connect_error) {
    echo json_encode(["error" => "Lỗi kết nối CSDL"]);
    exit;
}

$sql = "SELECT * FROM barbers ORDER BY AverageRating DESC";
$result = $conn->query($sql);

$barbers = [];
while ($row = $result->fetch_assoc()) {
    $barbers[] = $row;
}

echo json_encode($barbers);
$conn->close();
