<?php
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');

$host = "localhost";
$user = "root";
$pass = "";
$dbname = "barber_shop";

$conn = new mysqli($host, $user, $pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Kết nối thất bại"]);
    exit;
}

// Lấy dữ liệu từ Android
$fullname = $_POST['fullname'] ?? '';
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$role = $_POST['role'] ?? '';

if (empty($fullname) || empty($email) || empty($password) || empty($role)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin"]);
    exit;
}
// Kiểm tra email đã tồn tại trong client hoặc barber
$check_sql = "
    SELECT 'client' AS role FROM clients WHERE Email = ?
    UNION
    SELECT 'barber' AS role FROM barbers WHERE Email = ?
";

$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("ss", $email, $email);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Email đã được sử dụng ở vai trò khác"]);
    exit;
}

// Xử lý thêm vào bảng tương ứng
if ($role == 'client') {
    $sql = "INSERT INTO clients (FullName, Email, Password) VALUES (?, ?, ?)";
} elseif ($role == 'barber') {
    $sql = "INSERT INTO barbers (FullName, Email, Password) VALUES (?, ?, ?)";
} else {
    echo json_encode(["success" => false, "message" => "Vai trò không hợp lệ"]);
    exit;
}

$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $fullname, $email, $password);
$success = $stmt->execute();

if ($success) {
    echo json_encode(["success" => true, "message" => "Đăng ký thành công"]);
} else {
    echo json_encode(["success" => false, "message" => "Đăng ký thất bại"]);
}

$stmt->close();
$conn->close();
?>
