<?php
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');

// Kết nối database
$host = "localhost";
$user = "root";
$pass = "";
$dbname = "barber_shop";

$conn = new mysqli($host, $user, $pass, $dbname);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["error" => "Kết nối thất bại: " . $conn->connect_error]);
    exit;
}

// Kiểm tra nếu là POST (đăng ký)
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Lấy dữ liệu từ app gửi lên
    $fullname = $_POST['fullname'] ?? '';
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';
    $role = $_POST['role'] ?? '';

    // Kiểm tra dữ liệu hợp lệ
    if (empty($fullname) || empty($email) || empty($password) || empty($role)) {
        http_response_code(400);
        echo json_encode(["error" => "Thiếu dữ liệu"]);
        exit;
    }

    // Kiểm tra email đã tồn tại chưa
    $table = ($role === "client") ? "clients" : (($role === "barber") ? "barbers" : null);
    if (!$table) {
        http_response_code(400);
        echo json_encode(["error" => "Role không hợp lệ"]);
        exit;
    }

    $stmt = $conn->prepare("SELECT * FROM $table WHERE Email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        http_response_code(409);
        echo json_encode(["error" => "Email đã tồn tại"]);
        exit;
    }
    $stmt->close();

    // Thêm vào bảng đúng
    $stmt = $conn->prepare("INSERT INTO $table (FullName, Email, Password) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $fullname, $email, $password); // Lưu ý: nên hash password thực tế!
    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Đăng ký thành công"]);
    } else {
        http_response_code(500);
        echo json_encode(["error" => "Lỗi khi lưu dữ liệu"]);
    }
    $stmt->close();
    $conn->close();
    exit;
}

// Nếu là GET thì trả về dữ liệu như cũ
function getTableData($conn, $table) {
    $sql = "SELECT * FROM $table";
    $result = $conn->query($sql);
    $data = [];

    if ($result && $result->num_rows > 0) {
        while($row = $result->fetch_assoc()) {
            $data[] = $row;
        }
    }

    return $data;
}

$response = [
    "clients"      => getTableData($conn, "clients"),
    "barbers"      => getTableData($conn, "barbers"),
    "services"     => getTableData($conn, "services"),
    "appointments" => getTableData($conn, "appointments")
];

echo json_encode($response, JSON_PRETTY_PRINT);

$conn->close();
?>